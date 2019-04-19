package io.vacco.myrmica.maven.schema;

import org.joox.Match;
import java.net.URI;
import java.nio.file.*;
import java.util.Objects;
import static java.lang.String.format;

public class Coordinates implements Comparable<Coordinates> {

  public String groupId;
  public String artifactId;
  public String version;

  public URI getBaseUri(URI origin) {
    try {
      return origin.resolve(String.format("%s/",
          Paths.get(groupId.replace(".", "/"), artifactId, version).toString()));
    } catch (Exception e) {
      String msg = String.format("Unable to resolve base url for [%s] at origin [%s]", toString(), origin);
      throw new IllegalStateException(msg, e);
    }
  }

  public String getBaseCoordinates() {
    return String.format("%s:%s", groupId, artifactId);
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
    return String.format("%s%s", getBaseCoordinates(),
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

  @Override public int compareTo(Coordinates o) {
    String ef0 = toExternalForm();
    String ef1 = o.toExternalForm();
    return ef0.compareTo(ef1);
  }

  public boolean matchesGroupAndArtifact(Coordinates mm0) {
    return this.groupId.equalsIgnoreCase(mm0.groupId)
        && this.artifactId.equalsIgnoreCase(mm0.artifactId);
  }

  public static Coordinates from(String groupId, String artifactId, String version) {
    Coordinates c = new Coordinates();
    c.groupId = Objects.requireNonNull(groupId);
    c.artifactId = Objects.requireNonNull(artifactId);
    c.version = version;
    return c;
  }

  public static Coordinates from(Match xml) {
    return from(xml.child(Constants.PomTag.groupId.toString()).text(),
        xml.child(Constants.PomTag.artifactId.toString()).text(),
        xml.child(Constants.PomTag.version.toString()).text());
  }
}
