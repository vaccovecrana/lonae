package io.vacco.myrmica.maven.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class MmCoordinates implements Comparable<MmCoordinates> {

  public String groupId;
  public String artifactId;
  public String version;

  public String getArtifactFormat() { return format("%s:%s", groupId, artifactId); }
  public String getVersionFormat() { return format("%s%s", getArtifactFormat(), version == null ? "" : format(":%s", version)); }

  @Override public String toString() { return getVersionFormat(); }

  @Override public int hashCode() { return getVersionFormat().hashCode(); }
  @Override public boolean equals(Object o) {
    return o instanceof MmCoordinates && ((MmCoordinates) o).getVersionFormat().equals(getVersionFormat());
  }
  @Override public int compareTo(MmCoordinates o) {
    return getVersionFormat().compareTo(o.getVersionFormat());
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
