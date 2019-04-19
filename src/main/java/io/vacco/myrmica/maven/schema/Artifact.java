package io.vacco.myrmica.maven.schema;

import org.joox.Match;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.schema.Constants.*;
import static java.lang.String.format;

public class Artifact implements Comparable<Artifact> {

  public Coordinates at;
  public Component metadata;
  public boolean optional;
  public String scope;

  public Set<Artifact> exclusions = new TreeSet<>();

  public String getBaseArtifactName() {
    return format("%s%s", at != null ? at.getBaseResourceName() : "",
        metadata != null && metadata.classifier != null ? format("-%s", metadata.classifier) : "");
  }

  public String toExternalForm() {
    return format("%s/%s %s%s", at.toExternalForm(), getBaseArtifactName(),
        metadata != null ? metadata.toExternalForm() : "",
        scope != null ? format(", %s", scope) : "");
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    return at.getBaseUri(origin).resolve(format("%s%s", getBaseArtifactName(), resourceExtension));
  }

  public URI getPackageUri(URI origin) { return getResourceUri(origin, format(".%s", metadata.extension)); }
  public Path getLocalPackagePath(Path root) { return Paths.get(getPackageUri(root.toUri())); }
  public boolean isRuntimeClassPath() {
    if (metadata.type.contains(scope_test)) return false;
    if (metadata.classifier != null && metadata.classifier.contains(scope_test)) return false;
    if (optional) return false;
    if (scope_provided.equals(scope)) return false;
    boolean isRtScope = scope_compile.equals(scope) || scope_runtime.equals(scope);
    return metadata.addedToClasspath && isRtScope;
  }

  @Override public int compareTo(Artifact o) { return toExternalForm().compareTo(o.toExternalForm()); }
  @Override public int hashCode() { return toExternalForm().hashCode(); }
  @Override public boolean equals(Object o) {
    if (o instanceof Artifact) {
      Artifact a0 = (Artifact) o;
      String ef0 = toExternalForm();
      String ef1 = a0.toExternalForm();
      return ef0.equals(ef1);
    }
    return false;
  }

  public void setScope(String scope) { this.scope = scope; }

  public boolean excludes(Artifact a) {
    return exclusions.stream().anyMatch(e -> e.at.matchesGroupAndArtifact(a.at));
  }

  @Override public String toString() {
    return format("[%s%s]", toExternalForm(),
        exclusions.size() > 0 ? format(" {-%s}", exclusions.size()) : "");
  }

  public static Set<Artifact> artifactsOf(Match xmlDepNode) {
    return xmlDepNode.children().each().stream().map(Artifact::from)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public static Artifact from(Match xml) {
    Objects.requireNonNull(xml);
    Artifact a = new Artifact();
    a.at = Coordinates.from(xml);
    a.scope = xml.child(Constants.PomTag.scope.toString()).text();
    a.optional = Boolean.parseBoolean(xml.child(Constants.PomTag.optional.toString()).text());
    a.exclusions.addAll(artifactsOf(xml.child(Constants.PomTag.exclusions.toString())));

    Optional<Component> c = Component.forType(xml.child(ComponentTag.type.toString()).text());
    if (!c.isPresent()) { c = Component.forPackaging(xml.child(ComponentTag.packaging.toString()).text()); }
    if (!c.isPresent()) {
      c = Component.forType(Constants.DEFAULT_ARTIFACT_TYPE);
      c.get().setClassifier(xml);
    }
    a.metadata = c.get();
    if (a.scope == null) { a.scope = scope_compile; }
    return a;
  }
}
