package unit;

import io.vacco.myrmica.maven.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class ResolutionStats {

  public final Set<Coordinates> hit = new TreeSet<>();
  public final Set<Coordinates> miss = new TreeSet<>();

  public static Set<Coordinates> loadRef(String classPathLocation) throws IOException {
    String [] lines = new Scanner(
        MyrmicaSpec.class.getResource(classPathLocation).openStream(), "UTF-8"
    ).useDelimiter("\\A").next().split("\n");
    return new TreeSet<>(
        Arrays.stream(lines)
            .map(l0 -> l0.split(":"))
            .map(l0 -> new Coordinates(l0[0], l0[1], l0[3]))
            .collect(Collectors.toSet()));
  }

  public static ResolutionStats installAndMatch(Repository repo, Coordinates target, String mvnReference) throws IOException {
    ResolutionStats result = new ResolutionStats();
    Set<Coordinates> mvnRef = ResolutionStats.loadRef(mvnReference);
    Map<Artifact, Path> binaries = repo.installRuntimeArtifactsAt(target);
    Set<Coordinates> binaryCoords = new TreeSet<>(binaries.keySet().stream().map(Artifact::getAt).collect(Collectors.toSet()));
    assertFalse(binaries.isEmpty());

    mvnRef.forEach(refCoord -> {
      if (binaryCoords.contains(refCoord)) {
        result.hit.add(refCoord);
      } else {
        result.miss.add(refCoord);
      }
    });

    /*
    binaries.forEach((artifact, path) -> {
      assertTrue(path.toFile().exists() && path.toFile().isFile());
      if (mvnRef.contains(artifact.getAt())) { result.hit.add(artifact.getAt()); }
      else { result.miss.add(artifact.getAt()); }
    });
    */

    return result;
  }

  @Override public String toString() {
    return String.format("Hit: %s, Miss: %s", hit.size(), miss.size());
  }
}
