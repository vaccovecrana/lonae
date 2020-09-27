package io.vacco.lonae.maven.schema;

import java.nio.file.Path;

public class MmResolutionResult {

  public MmCoordinates  coordinates;
  public String         artifactName;
  public Path           location;
  public Exception      error;

  public static MmResolutionResult of(MmArtifact artifact, Path location, Exception error) {
    MmResolutionResult r = new MmResolutionResult();
    r.coordinates = artifact.at;
    r.artifactName = artifact.baseArtifactName();
    r.location = location;
    r.error = error;
    return r;
  }

  @Override public String toString() {
    return String.format("%s -> %s", artifactName,
        location != null ? location.toAbsolutePath() : error.getMessage()
    );
  }
}
