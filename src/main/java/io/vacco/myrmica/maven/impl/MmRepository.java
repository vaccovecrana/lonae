package io.vacco.myrmica.maven.impl;

import io.vacco.myrmica.maven.schema.*;
import io.vacco.myrmica.maven.xform.*;
import io.vacco.oriax.core.*;
import org.slf4j.*;

import java.io.File;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static java.util.Objects.*;
import static java.lang.String.*;
import static io.vacco.myrmica.maven.impl.MmProperties.*;
import static java.util.stream.Collectors.*;

public class MmRepository {

  private static final Logger log = LoggerFactory.getLogger(MmRepository.class);
  private static final URL compXml = MmRepository.class.getResource("/io/vacco/myrmica/maven/artifact-handlers.xml");

  public static final Map<MmComponent.Type, MmComponent> defaultComps = MmXform.forComponents(compXml);

  public final Path localRoot;
  public final URI remoteRoot;

  public MmRepository(String localRootPath, String remotePath) { // TODO add session ID here (for concurrent processing).
    try {
      this.localRoot = Paths.get(requireNonNull(localRootPath));
      if (!localRoot.toFile().exists()) {
        throw new IllegalArgumentException(format("Missing root folder: [%s]", localRoot.toAbsolutePath().toString()));
      }
      if (!localRoot.toFile().isDirectory()) {
        throw new IllegalArgumentException(format("Not a directory: [%s]", localRoot.toAbsolutePath().toString()));
      }
      if (!requireNonNull(remotePath).endsWith("/")) {
        throw new IllegalArgumentException(String.format("Remote path does not end with a trailing slash: [%s]", remotePath));
      }
      this.remoteRoot = new URI(remotePath);
    } catch (Exception e) {
      throw new MmException.MmRepositoryInitializationException(localRootPath, remotePath, e);
    }
  }

  public static Path getResourcePath(MmCoordinates coordinates) {
    return Paths.get(
        coordinates.groupId.replace(".", "/"),
        coordinates.artifactId, coordinates.version
    );
  }

  public MmArtifact artifactOf(MmCoordinates coordinates, MmComponent.Type type) {
    MmArtifact art = new MmArtifact();
    art.at = coordinates;
    art.comp = defaultComps.get(type);
    return art;
  }

  public Path resourcePathOf(MmArtifact art) {
    return getResourcePath(art.at).resolve(art.baseArtifactName());
  }

  private Path resolveOrFetch(Path resourcePath) {
    try {
      Path target = localRoot.resolve(resourcePath);
      if (!target.toFile().getParentFile().exists()) { target.toFile().getParentFile().mkdirs(); }
      if (!target.toFile().exists()) {
        URI remoteRes = remoteRoot.resolve(resourcePath.toString());
        log.info("Fetching [{}]", remoteRes);
        Files.copy(remoteRes.toURL().openStream(), target);
      } else if (log.isDebugEnabled()) { log.debug("Resolved [{}]", target); }
      return target;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public MmPom loadPom(MmCoordinates c) {
    try {
      Path pomPath = resolveOrFetch(resourcePathOf(artifactOf(c, MmComponent.Type.pom)));
      MmPom pom = MmXform.forPom(pomPath.toUri().toURL());
      if (pom.at.groupId == null) { pom.at.groupId = pom.parent.groupId; }
      if (pom.at.version == null) { pom.at.version = pom.parent.version; }
      return pom;
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public MmPom computePom(MmCoordinates c) {
    try {
      List<MmPom> poms = new ArrayList<>();
      List<MmPom> pomsRev;
      Optional<MmCoordinates> oc = Optional.of(c);

      while (oc.isPresent()) {
        MmPom p = loadPom(oc.get());
        poms.add(p);
        oc = p.parent != null ? Optional.of(p.parent) : Optional.empty();
      }

      pomsRev = new ArrayList<>(poms);
      Collections.reverse(pomsRev);
      MmVarContext varCtx = new MmVarContext();
      File pwd = new File(".");

      System.getProperties().forEach((k, v) -> varCtx.set(k.toString(), v));
      varCtx.push();
      varCtx.set("project.basedir", pwd.getAbsolutePath());
      varCtx.set("project.build.directory", pwd.getAbsolutePath());
      varCtx.set("project.groupId", c.groupId);
      varCtx.set("project.artifactId", c.artifactId);
      varCtx.set("project.version", c.version);

      if (poms.size() > 1) {
        varCtx.set("project.parent.basedir", pwd.getAbsolutePath());
        varCtx.set("project.parent.groupId", poms.get(1).at.groupId);
        varCtx.set("project.parent.artifactId", poms.get(1).at.artifactId);
        varCtx.set("project.parent.version", poms.get(1).at.version);
      }

      pomsRev.forEach(pom -> pom.properties.forEach(varCtx::set));
      poms.forEach(pom -> resolveVarReferences(pom, varCtx));

      MmVarContext depCtx = new MmVarContext();

      pomsRev.forEach(pom -> {
        Set<MmArtifact> imports = pom.dependencyManagement.stream()
            .filter(art -> art.meta.scopeType == MmArtifactMeta.Scope.Import)
            .flatMap(art -> computePom(art.at).dependencyManagement.stream()).collect(toSet());
        pom.dependencyManagement.addAll(imports);
        pom.dependencyManagement.forEach(art -> depCtx.set(art.at.artifactFormat(), art));
        depCtx.push();
      });

      MmPom pom = poms.get(0);

      for (MmArtifact dep : pom.dependencies) {
        if (dep.at.version == null) {
          MmArtifact mDep = (MmArtifact) depCtx.get(dep.at.artifactFormat());
          dep.at.version = mDep.at.version;
        }
        if (dep.meta.scopeType == MmArtifactMeta.Scope.Import) {
          log.warn("Unresolved dependency with import scope in <dependencies> section: {}", dep.toString());
        }
      }

      if (log.isDebugEnabled()) {
        log.debug(MmJsonLog.jsonLogOf(pom));
      }

      return pom;
    } catch (Exception e) {
      throw new MmException.MmPomResolutionException(c, e);
    }
  }

  private OxVtx<String, MmPom> asVtx(MmCoordinates c) {
    return new OxVtx<>(c.artifactFormat(), computePom(c));
  }

  private boolean upstreamExcludes(MmArtifact art) {
    MmArtifact c = art.upstream;
    while (c != null) {
      Set<String> xIds = c.meta.exclusionIds();
      for (String x : xIds) {
        if (x.contains(art.at.groupId) && x.contains("*")) {
          return false;
        } else if (xIds.contains(art.at.artifactFormat())) {
          return false;
        }
      }
      c = c.upstream;
    }
    return true;
  }

  private Set<MmArtifact> runtimeDependencies(MmPom pom, MmArtifact upstream) {
    List<MmArtifact> rta = pom.dependencies.stream()
        .filter(MmArtifact::inRuntimeClasspath)
        .map(art -> art.withUpstream(upstream)).collect(toList());
    return rta.stream().filter(this::upstreamExcludes).collect(toSet());
  }

  private void buildGraphTail(MmCoordinates c, MmArtifact upstream, OxGrph<String, MmPom> g) {
    OxVtx<String, MmPom> vtx = asVtx(c);
    if (g.vtx.contains(vtx)) { return; }
    g.vtx.add(vtx);

    MmPom pom = vtx.data;
    Set<MmArtifact> rtDeps = runtimeDependencies(pom, upstream);

    for (MmArtifact ra : rtDeps) {
      buildGraphTail(ra.at, ra, g);
      String artId = ra.at.artifactFormat();
      Optional<OxVtx<String, MmPom>> rtPom = g.vtx.stream().filter(v0 -> v0.id.equals(artId)).findFirst();

      if (rtPom.isPresent()) {
        g.addEdge(vtx, rtPom.get());
        String artVer = ra.at.versionFormat();
        String depVer = rtPom.get().data.at.versionFormat();
        if (!artVer.equals(depVer)) {
          rtPom.get().data.extraVersions.add(ra);
        }
      }
    }
  }

  public OxGrph<String, MmPom> buildPomGraph(MmCoordinates c) {
    OxGrph<String, MmPom> graph = new OxGrph<>();
    buildGraphTail(c, null, graph);
    return graph;
  }

  public List<MmResolutionResult> installFrom(MmCoordinates root, String classifier) {
    List<MmResolutionResult> out = new ArrayList<>();
    for (OxVtx<String, MmPom> vtx : buildPomGraph(root).vtx) {
      MmPom pom = vtx.data;
      MmArtifact jar = artifactOf(pom.at, MmComponent.Type.jar); // Gradle's latest version wins strategy.
      if (!pom.extraVersions.isEmpty()) {
        Set<MmArtifact> allArts = new TreeSet<>(pom.extraVersions);
        allArts.add(jar);
        jar = allArts.iterator().next();
      }
      jar.comp.classifier = classifier;
      try {
        out.add(MmResolutionResult.of(jar, resolveOrFetch(resourcePathOf(jar)), null));
      } catch (Exception e) {
        out.add(MmResolutionResult.of(jar, null, e));
      }
    }
    return out;
  }
}
