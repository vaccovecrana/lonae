package io.vacco.myrica.core;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

public class ModuleMetadata implements Comparable<ModuleMetadata> {

  private static final String MOD_FMT = "(c: [%s], s: [%s])";

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String scope;

  public ModuleMetadata(String groupId, String artifactId, String version, String scope) {
    this.groupId = Objects.requireNonNull(groupId);
    this.artifactId = Objects.requireNonNull(artifactId);
    this.version = version;
    this.scope = scope;
  }

  public ModuleMetadata(String groupId, String artifactId, String version) {
    this(groupId, artifactId, version, null);
  }

  public String getBaseRelativePath() {
    String bp = Paths.get(groupId.replace(".", "/"), artifactId, version).toString();
    return String.format("%s/", bp);
  }

  public URI getBaseUri(URI origin) {
    try {
      URI target = origin.resolve(getBaseRelativePath());
      return target;
    } catch (Exception e) {
      String msg = String.format("Unable to resolve base url for [%s] at origin [%s]", toString(), origin);
      throw new IllegalStateException(msg, e);
    }
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    URI base = getBaseUri(origin);
    String baseResourceName = String.format("%s-%s%s", artifactId, version, resourceExtension);
    return base.resolve(baseResourceName);
  }

  public URI getJarUri(URI origin) { return getResourceUri(origin, ".jar"); }

  public URI getPomUri(URI origin) { return getResourceUri(origin, ".pom"); }
  public Path getLocalPomPath(Path root) {
    return Paths.get(getPomUri(root.toUri()));
  }

  public String getCoordinates() {
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
  public String getGroupId() { return groupId; }
  public String getArtifactId() { return artifactId; }
  public String getVersion() { return version; }

  public String toString() {
    return String.format(MOD_FMT, getCoordinates(), scope);
  }

  @Override public int compareTo(ModuleMetadata other) {
    return getCoordinates().compareTo(other.getCoordinates());
  }

  @Override public boolean equals(Object o) {
    if (o instanceof ModuleMetadata) {
      ModuleMetadata m1 = (ModuleMetadata) o;
      return this.groupId.equals(m1.groupId)
          && this.artifactId.equals(m1.artifactId)
          && this.version.equalsIgnoreCase(m1.version);
    }
    return false;
  }

  @Override public int hashCode() { return getCoordinates().hashCode(); }

  public boolean matchesGroupAndArtifact(ModuleMetadata mm0) {
    return this.getGroupId().equalsIgnoreCase(mm0.groupId)
        && this.getArtifactId().equalsIgnoreCase(mm0.artifactId);
  }
}
