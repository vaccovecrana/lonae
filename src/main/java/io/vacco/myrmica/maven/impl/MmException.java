package io.vacco.myrmica.maven.impl;

import io.vacco.myrmica.maven.schema.MmCoordinates;

import java.net.URI;

public class MmException {

  public static class MmCoordinateResolutionException extends RuntimeException {
    public final URI origin;
    public final MmCoordinates coordinates;
    public MmCoordinateResolutionException(URI origin, MmCoordinates coordinates, Exception e) {
      super(e);
      this.origin = origin;
      this.coordinates = coordinates;
    }
  }

  public static class MmRepositoryInitializationException extends RuntimeException {
    public final String localRootPath;
    public final String remotePath;
    public MmRepositoryInitializationException(String localRootPath, String remotePath, Exception e) {
      super(e);
      this.localRootPath = localRootPath;
      this.remotePath = remotePath;
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
