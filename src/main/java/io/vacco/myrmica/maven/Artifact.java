package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URI;
import java.nio.file.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.lang.String.format;

public class Artifact implements Comparable<Artifact> {

  private final Coordinates at;
  private final String classifier;
  private final String packaging;
  private final String scope;
  private final boolean optional;

  private final Set<Artifact> exclusions = new TreeSet<>();

  public Artifact(Coordinates at, String classifier, String packaging, String scope, boolean optional) {
    this.at = requireNonNull(at);
    this.packaging = packaging != null ? packaging : "jar";
    this.classifier = classifier;
    this.scope = scope;
    this.optional = optional;
  }

  public String toExternalForm() {
    return format("%s/%s.%s", at.toExternalForm(), getBaseArtifactName(), packaging);
  }

  public String getBaseArtifactName() {
    return format("%s%s", at.getBaseResourceName(), classifier != null ? format("-%s", classifier) : "");
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    return at.getBaseUri(origin).resolve(format("%s%s", getBaseArtifactName(), resourceExtension));
  }

  public URI getPackageUri(URI origin) { return getResourceUri(origin, format(".%s", packaging)); }
  public Path getLocalPackagePath(Path root) { return Paths.get(getPackageUri(root.toUri())); }

  public boolean isRuntime() {
    if (optional) return false;
    if (scope != null && scope.equalsIgnoreCase("test")) return false;
    return scope == null || !scope.equalsIgnoreCase("provided");
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

  @Override public String toString() {
    return format("[%s%s%s]", toExternalForm(),
        scope != null ? format(", %s", scope) : "",
        exclusions.size() > 0 ? format(" {-%s}", exclusions.size()) : "");
  }

  public static Artifact fromXml(Match xml) {
    Artifact a = new Artifact(new Coordinates(xml),
        xml.child("classifier").text(), xml.child("packaging").text(),
        xml.child("scope").text(), xml.child("optional").size() > 0);
    a.exclusions.addAll(artifactsOf(xml.child("exclusions")));
    return a;
  }

  public static Set<Artifact> artifactsOf(Match xmlDepNode) {
    return new TreeSet<>(xmlDepNode.children().each().stream()
        .map(Artifact::fromXml).collect(Collectors.toSet()));
  }
}
