package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.Constants.*;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;

public class Artifact implements Comparable<Artifact> {

  private final Coordinates at;
  private final Component metadata;
  private final boolean optional;
  private String scope;
  private final Set<Artifact> exclusions = new TreeSet<>();

  public Artifact(Match xml) {
    this.at = new Coordinates(requireNonNull(xml));
    this.scope = xml.child(Constants.PomTag.scope.toString()).text();
    this.optional = Boolean.parseBoolean(xml.child(Constants.PomTag.optional.toString()).text());
    this.exclusions.addAll(artifactsOf(xml.child(Constants.PomTag.exclusions.toString())));

    Optional<Component> c = Component.forType(xml.child(ComponentTag.type.toString()).text());
    if (!c.isPresent()) { c = Component.forPackaging(xml.child(ComponentTag.packaging.toString()).text()); }
    if (!c.isPresent()) {
      c = Component.forType(Constants.DEFAULT_ARTIFACT_TYPE);
      c.get().setClassifier(xml);
    }
    this.metadata = c.get();
    if (scope == null) { scope = Scope.compile.toString(); }
  }

  public String toExternalForm() {
    return format("%s/%s.%s (%s)", at.toExternalForm(), getBaseArtifactName(), metadata.type, metadata.packaging);
  }

  public String getBaseArtifactName() {
    return format("%s%s", at.getBaseResourceName(),
        metadata.classifier != null ? format("-%s", metadata.classifier) : "");
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    return at.getBaseUri(origin).resolve(format("%s%s", getBaseArtifactName(), resourceExtension));
  }

  public URI getPackageUri(URI origin) { return getResourceUri(origin, format(".%s", metadata.type)); }
  public Path getLocalPackagePath(Path root) { return Paths.get(getPackageUri(root.toUri())); }
  public boolean isRuntime() {
    return metadata.addedToClasspath
        && (scope.equals(Scope.compile.toString()) || scope.equals(Scope.runtime.toString()))
        && !optional;
  }

  @Override public int compareTo(Artifact o) { return toExternalForm().compareTo(o.toExternalForm()); }
  @Override public int hashCode() { return getBaseArtifactName().hashCode(); }
  @Override public boolean equals(Object o) {
    if (o instanceof Artifact) {
      Artifact a0 = (Artifact) o;
      return getBaseArtifactName().equals(a0.getBaseArtifactName());
    }
    return false;
  }

  public Coordinates getAt() { return at; }
  public Set<Artifact> getExclusions() { return exclusions; }

  @Override public String toString() {
    return format("[%s%s%s]", toExternalForm(),
        scope != null ? format(", %s", scope) : "",
        exclusions.size() > 0 ? format(" {-%s}", exclusions.size()) : "");
  }

  public static Set<Artifact> artifactsOf(Match xmlDepNode) {
    return new TreeSet<>(xmlDepNode.children().each().stream()
        .map(Artifact::new).collect(Collectors.toSet()));
  }
}
