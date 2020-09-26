package io.vacco.myrmica.maven.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MmArtifactMeta {

  public enum Scope { Compile, Import, Provided, Runtime, Test }

  public boolean optional;
  public String scope;
  public Scope scopeType;
  public final Set<MmCoordinates> exclusions = new TreeSet<>();

}
