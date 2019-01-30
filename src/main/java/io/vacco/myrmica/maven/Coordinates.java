package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import static java.lang.String.format;

public class Coordinates {

  private final String groupId;
  private final String artifactId;
  private final String version;

  public Coordinates(String groupId, String artifactId, String version) {
    this.groupId = Objects.requireNonNull(groupId);
    this.artifactId = Objects.requireNonNull(artifactId);
    this.version = version;
  }

  public Coordinates(Match xml) {
    this(xml.child("groupId").text(), xml.child("artifactId").text(), xml.child("version").text());
  }

  public URI getBaseUri(URI origin) {
    try {
      return origin.resolve(String.format("%s/",
          Paths.get(groupId.replace(".", "/"), artifactId, version).toString()));
    } catch (Exception e) {
      String msg = String.format("Unable to resolve base url for [%s] at origin [%s]", toString(), origin);
      throw new IllegalStateException(msg, e);
    }
  }

  public String getBaseResourceName() {
    return String.format("%s%s", artifactId,
        version != null ? format("-%s", version) : "");
  }

  public URI getPomUri(URI origin) {
    URI base = getBaseUri(origin);
    String baseResourceName = String.format("%s.pom", getBaseResourceName());
    return base.resolve(baseResourceName);
  }
  public Path getLocalPomPath(Path root) {
    return Paths.get(getPomUri(root.toUri()));
  }

  public String toExternalForm() {
    return String.format("%s:%s%s", groupId, artifactId,
        version == null ? "" : String.format(":%s", version));
  }

  @Override public String toString() { return toExternalForm(); }

  @Override public int hashCode() { return toExternalForm().hashCode(); }
  @Override public boolean equals(Object o) {
    if (o instanceof Coordinates) {
      Coordinates m1 = (Coordinates) o;
      return this.groupId.equals(m1.groupId)
          && this.artifactId.equals(m1.artifactId)
          && (this.version != null && this.version.equals(m1.version));
    }
    return false;
  }

  public boolean matchesGroupAndArtifact(Coordinates mm0) {
    return this.groupId.equalsIgnoreCase(mm0.groupId)
        && this.artifactId.equalsIgnoreCase(mm0.artifactId);
  }

  public String getGroupId() { return groupId; }
  public String getArtifactId() { return artifactId; }
  public String getVersion() { return version; }
}
