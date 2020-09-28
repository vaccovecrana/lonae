package io.vacco.lonae.maven.impl;

import io.vacco.lonae.maven.schema.MmCoordinates;

public class MmException {

  public static class MmRepositoryInitializationException extends RuntimeException {
    public final String   localRootPath;
    public final String[] remotePaths;
    public MmRepositoryInitializationException(String localRootPath, String[] remotePaths, Exception e) {
      super(e);
      this.localRootPath = localRootPath;
      this.remotePaths = remotePaths;
    }
  }

  public static class MmPomResolutionException extends RuntimeException {
    public final MmCoordinates coordinates;
    public MmPomResolutionException(MmCoordinates coordinates, Exception e) {
      super(e);
      this.coordinates = coordinates;
    }
  }

}
