package io.vacco.lonae.maven.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MmArtifactMeta {

  public enum Scope { Compile, Import, Provided, Runtime, Test }

  public boolean optional;
  public String scope;
  public Scope scopeType;
  public final Set<MmCoordinates> exclusions = new TreeSet<>();

  public Set<String> exclusionIds() {
    return exclusions.stream()
        .map(MmCoordinates::artifactFormat)
        .collect(Collectors.toSet());
  }

  @Override
  public String toString() {
    return String.format("{opt: %s, scope: %s, -%s}", optional, scopeType, exclusions.size());
  }
}
