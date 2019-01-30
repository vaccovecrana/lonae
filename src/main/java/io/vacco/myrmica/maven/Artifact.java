package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URI;
import java.nio.file.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static io.vacco.myrmica.maven.Constants.*;

public class Artifact implements Comparable<Artifact> {

  public static final String DEFAULT_PACKAGE_TYPE = Constants.PackageType.jar.toString();

  private final Coordinates at;
  private final String type;
  private final String classifier;
  private final String packaging;
  private final String scope;
  private final boolean optional;

  private final Set<Artifact> exclusions = new TreeSet<>();

  public Artifact(Coordinates at, String type, String classifier, String packaging, String scope, boolean optional) {
    this.at = requireNonNull(at);
    this.type = type != null ? type : DEFAULT_PACKAGE_TYPE;
    this.packaging = packaging != null ? packaging : DEFAULT_PACKAGE_TYPE;
    this.classifier = classifier;
    this.scope = scope;
    this.optional = optional;
  }

  public String toExternalForm() {
    return format("%s/%s.%s (%s)", at.toExternalForm(), getBaseArtifactName(), type, packaging);
  }

  public String getBaseArtifactName() {
    return format("%s%s", at.getBaseResourceName(), classifier != null ? format("-%s", classifier) : "");
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    return at.getBaseUri(origin).resolve(format("%s%s", getBaseArtifactName(), resourceExtension));
  }

  public URI getPackageUri(URI origin) { return getResourceUri(origin, format(".%s", type)); }
  public Path getLocalPackagePath(Path root) { return Paths.get(getPackageUri(root.toUri())); }

  public boolean isRuntime() {
    if (!DEFAULT_PACKAGE_TYPE.equals(type)) return false; // TODO possibly enhance this decision.
    if (optional) return false;
    if (scope != null && scope.equalsIgnoreCase(Scope.test.toString())) return false;
    return scope == null || !scope.equalsIgnoreCase(Scope.provided.toString());
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

  public static Artifact fromXml(Match xml) {
    Artifact a = new Artifact(new Coordinates(xml),
        xml.child(Constants.PomTag.type.toString()).text(),
        xml.child(Constants.PomTag.classifier.toString()).text(),
        xml.child(Constants.PomTag.packaging.toString()).text(),
        xml.child(Constants.PomTag.scope.toString()).text(),
        xml.child(Constants.PomTag.optional.toString()).size() > 0);
    a.exclusions.addAll(artifactsOf(xml.child(Constants.PomTag.exclusions.toString())));
    return a;
  }

  public static Set<Artifact> artifactsOf(Match xmlDepNode) {
    return new TreeSet<>(xmlDepNode.children().each().stream()
        .map(Artifact::fromXml).collect(Collectors.toSet()));
  }
}
