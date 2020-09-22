package io.vacco.myrmica.maven.impl;

import io.vacco.myrmica.maven.schema.*;
import org.joox.Match;
import org.slf4j.*;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static java.util.Objects.*;
import static java.lang.String.*;

public class MmRepository {

  private static final Logger log = LoggerFactory.getLogger(MmRepository.class);

  public final Path localRoot;
  public final URI remoteRoot;
  public final Map<MmCoordinates, Match> resolvedPoms = new TreeMap<>();

  public MmRepository(String localRootPath, String remotePath) {
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


/*

*/
  /*
  private Path install(Artifact a) {
    requireNonNull(a);
    try {
      Path target = a.getLocalPackagePath(localRoot);
      if (!target.toFile().getParentFile().exists()) { target.toFile().getParentFile().mkdirs(); }
      if (!target.toFile().exists()) {
        URI remoteArtifact = a.getPackageUri(remoteRoot);
        log.info("Downloading [{}]", remoteArtifact);
        Files.copy(remoteArtifact.toURL().openStream(), target);
      }
      return target;
    } catch (Exception e) {
      String msg = String.format("Unable to install artifact [%s]", a);
      throw new IllegalStateException(msg, e);
    }
  }

  public Pom buildPom(Coordinates c) {
    Pom p = resolvedPoms.computeIfAbsent(c, c0 -> new Pom(computePom(c0)));
    List<Artifact> imports = p.getDefaultVersions().stream()
        .filter(a -> a.scope != null)
        .filter(a -> a.scope.equals(scope_import))
        .map(ai -> buildPom(ai.at))
        .flatMap(p0 -> p0.getDefaultVersions().stream())
        .collect(Collectors.toList());
    Set<Artifact> importedDefaults = new TreeSet<>();
    for (Artifact ia : imports) {
      boolean alreadyImported = importedDefaults.stream()
          .anyMatch(ia0 -> ia0.at.matchesGroupAndArtifact(ia.at));
      if (!alreadyImported) {
        importedDefaults.add(ia);
      }
    }
    p.getDefaultVersions().addAll(importedDefaults);
    p.computeEffectiveDependencies();
    return p;
  }

  /**
   * @see
   *   <a href="https://maven.apache.org/plugins/maven-dependency-plugin/examples/resolving-conflicts-using-the-dependency-tree.html">
   *     resolving-conflicts-using-the-dependency-tree.html
   *   </a>
   *
  private void loadRtTail(DependencyNode context) {
    if (context.getArtifact().isRuntimeClassPath()) {
      Set<Artifact> deps = context.getPom().getDependencies();
      for (Artifact rd : deps) {
        if (!rd.isRuntimeClassPath()) continue;
        if (context.excludes(rd)) continue;
        if (context.isTopLevelOverride(rd)) continue;
        DependencyNode child = new DependencyNode(buildPom(rd.at), rd, context);
        context.getChildren().add(child);
        loadRtTail(child);
      }
    }
  }

  public ResolutionResult loadRuntimeArtifactsAt(Coordinates root) {
    DependencyNode n0 = new DependencyNode(buildPom(root));
    loadRtTail(n0);
    return new ResolutionResult(n0);
  }

  public Map<Artifact, Path> installLoadedArtifacts(ResolutionResult r) {
    return r.artifacts.parallelStream()
        .collect(Collectors.toMap(Function.identity(), this::install));
  }

  public Map<Artifact, Path> installRuntimeArtifactsAt(Coordinates root) {
    return new TreeMap<>(installLoadedArtifacts(loadRuntimeArtifactsAt(root)));
  }
*/
}
