package io.vacco.lonae.maven.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MmCoordinates implements Comparable<MmCoordinates> {

  public String groupId;
  public String artifactId;
  public String version;

  public String artifactFormat() { return format("%s:%s", groupId, artifactId); }
  public String versionFormat() { return format("%s%s", artifactFormat(), version == null ? "" : format(":%s", version)); }

  @Override public String toString() { return versionFormat(); }

  @Override public int hashCode() { return versionFormat().hashCode(); }
  @Override public boolean equals(Object o) {
    return o instanceof MmCoordinates && ((MmCoordinates) o).versionFormat().equals(versionFormat());
  }
  @Override public int compareTo(MmCoordinates o) {
    return versionFormat().compareTo(o.versionFormat());
  }

  public static MmCoordinates from(String group, String artifact, String version) {
    MmCoordinates c = new MmCoordinates();
    c.groupId = requireNonNull(group);
    c.artifactId = requireNonNull(artifact);
    c.version = version;
    return c;
  }

  public static MmCoordinates from(String colNotation) {
    String[] components = colNotation.split(":");
    return from(components[0], components[1], components[2]);
  }
}
