package io.vacco.myrmica.maven.schema;

import java.nio.file.Path;

public class MmResolutionResult {

  public MmArtifact artifact;
  public Path       location;
  public Exception  error;

  public static MmResolutionResult of(MmArtifact artifact, Path location, Exception error) {
    MmResolutionResult r = new MmResolutionResult();
    r.artifact = artifact;
    r.location = location;
    r.error = error;
    return r;
  }
}
