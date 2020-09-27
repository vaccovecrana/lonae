package io.vacco.lonae.maven.schema;

import java.util.*;

public class MmPom {

  public MmCoordinates parent;
  public MmCoordinates at;
  public Map<String, String> properties = new LinkedHashMap<>();

  public Set<MmArtifact> dependencies = new TreeSet<>();
  public Set<MmArtifact> dependencyManagement = new TreeSet<>();
  public Set<MmArtifact> extraVersions = new TreeSet<>();

  @Override public String toString() {
    return String.format("pom[%s]", at);
  }
}
