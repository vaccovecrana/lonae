package io.vacco.myrmica.maven;

import org.joox.Match;
import org.slf4j.*;

import java.io.File;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.*;
import static org.joox.JOOX.*;
import static io.vacco.myrmica.maven.PropertyAccess.*;
import static io.vacco.myrmica.maven.Constants.*;

public class Repository {

  private static final Logger log = LoggerFactory.getLogger(Repository.class);

  private Path localRoot;
  private URI remoteRoot;
  private final Map<Coordinates, Pom> resolvedPoms = new TreeMap<>();

  public Repository(String localRootPath, String remotePath) {
    this.localRoot = Paths.get(requireNonNull(localRootPath));
    if (!localRoot.toFile().exists()) {
      throw new IllegalArgumentException(String.format("Missing root folder: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!localRoot.toFile().isDirectory()) {
      throw new IllegalArgumentException(String.format("Not a directory: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!requireNonNull(remotePath).endsWith("/")) {
      throw new IllegalArgumentException(String.format("Remote path does not end with a trailing slash: [%s]", remotePath));
    }
    try { this.remoteRoot = new URI(remotePath); }
    catch (URISyntaxException e) { throw new IllegalStateException(e); }
  }

  private Match loadPom(Coordinates c) {
    requireNonNull(c);
    try {
      Path target = c.getLocalPomPath(localRoot);
      if (!target.toFile().getParentFile().exists()) { target.toFile().getParentFile().mkdirs(); }
      if (!target.toFile().exists()) {
        URI remotePom = c.getPomUri(remoteRoot);
        log.info("Fetching [{}]", remotePom);
        Files.copy(remotePom.toURL().openStream(), target);
      } else { log.info("Resolving [{}]", target); }
      return $(target.toFile());
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

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

  private Optional<Coordinates> loadParent(Match pom) {
    Match p = pom.child(PomTag.parent.toString());
    if (p.size() == 0) return Optional.empty();
    return Optional.of(new Coordinates(p));
  }

  private Match computePom(Coordinates coordinates) {
    List<Match> poms = new ArrayList<>();
    Optional<Coordinates> oc = Optional.of(coordinates);
    while (oc.isPresent()) {
      Match pp = loadPom(oc.get());
      poms.add(pp);
      oc = loadParent(pp);
    }

    String rootPackaging = poms.get(0).child(ComponentTag.packaging.toString()).text();
    Optional<Coordinates> parentCoords = loadParent(poms.get(0));

    Collections.reverse(poms);
    poms = poms.stream().map(pom -> NodeUtil.filterTop(pom, PomTag.exclusionTags())).collect(Collectors.toList());
    Optional<Match> ePom = poms.stream().reduce(NodeUtil::merge);

    if (rootPackaging != null) {
      ePom.get().child(ComponentTag.packaging.toString()).text(rootPackaging);
    }

    Map<String, String> rawProps = loadProperties(ePom.get());
    rawProps.put("project.build.directory", new File(".").getAbsolutePath());
    rawProps.put("project.groupId", coordinates.getGroupId());
    rawProps.put("project.artifactId", coordinates.getArtifactId());
    rawProps.put("project.version", coordinates.getVersion());
    if (parentCoords.isPresent()) {
      rawProps.put("project.parent.groupId", parentCoords.get().getGroupId());
      rawProps.put("project.parent.artifactId", parentCoords.get().getArtifactId());
      rawProps.put("project.parent.version", parentCoords.get().getVersion());
    }

    resolvePomKeyReferences(ePom.get(), resolveProperties(rawProps));
    return ePom.get();
  }

  public Pom buildPom(Coordinates c) {
    Pom p = resolvedPoms.computeIfAbsent(c, c0 -> new Pom(computePom(c0)));
    List<Artifact> imports = p.getDefaultVersions().stream()
        .filter(a -> a.getScope() != null)
        .filter(a -> a.getScope().equals(scope_import))
        .map(ai -> buildPom(ai.getAt()))
        .flatMap(p0 -> p0.getDefaultVersions().stream())
        .collect(Collectors.toList());
    Set<Artifact> importedDefaults = new TreeSet<>();
    for (Artifact ia : imports) {
      boolean alreadyImported = importedDefaults.stream()
          .anyMatch(ia0 -> ia0.getAt().matchesGroupAndArtifact(ia.getAt()));
      if (!alreadyImported) {
        importedDefaults.add(ia);
      }
    }
    p.getDefaultVersions().addAll(importedDefaults);
    return p;
  }

  /**
   * @see
   *   <a href="https://maven.apache.org/plugins/maven-dependency-plugin/examples/resolving-conflicts-using-the-dependency-tree.html">
   *     resolving-conflicts-using-the-dependency-tree.html
   *   </a>
   */
  private void loadRtTail(DependencyNode context, Set<Artifact> resolved) {
    if (context.artifact.isRuntimeClassPath()) {
      resolved.add(context.artifact);
      Set<Artifact> deps = context.pom.getDependencies();
      for (Artifact rd : deps) {
        if (!rd.isRuntimeClassPath()) continue;
        if (context.excludes(rd)) continue;
        if (context.isTopLevelOverride(rd)) continue;
        loadRtTail(new DependencyNode(buildPom(rd.getAt()), rd, context), resolved);
      }
    }
  }

  public Set<Artifact> loadRuntimeArtifactsAt(Coordinates root) {
    Set<Artifact> result = new TreeSet<>();
    DependencyNode n0 = new DependencyNode(buildPom(root));
    loadRtTail(n0, result);
    Map<String, List<Artifact>> o = result.stream().collect(Collectors.groupingBy(a -> a.getAt().getBaseCoordinates()));
    o.values().stream().filter(al -> al.size() > 1).forEach(al -> {
      List<Artifact> vDesc = al.stream().sorted().collect(Collectors.toList());
      vDesc.remove(vDesc.size() - 1);
      vDesc.forEach(result::remove);
    });
    return result;
  }

  public Map<Artifact, Path> installRuntimeArtifactsAt(Coordinates root) {
    Set<Artifact> result = loadRuntimeArtifactsAt(root);
    return new TreeMap<>(result.parallelStream()
        .collect(Collectors.toMap(Function.identity(), this::install)));
  }
}
