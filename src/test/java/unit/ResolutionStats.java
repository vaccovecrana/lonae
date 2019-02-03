package unit;

import io.vacco.myrmica.maven.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class ResolutionStats {

  public final Coordinates coordinates;
  public final Set<Coordinates> hit = new TreeSet<>();
  public final Set<Coordinates> miss = new TreeSet<>();
  public final Set<Coordinates> slack = new TreeSet<>();

  public ResolutionStats(Coordinates c) {
    this.coordinates = Objects.requireNonNull(c);
  }

  public static Set<Coordinates> loadRef(String classPathLocation) throws IOException {
    String [] lines = new Scanner(
        MyrmicaSpec.class.getResource(classPathLocation).openStream(), "UTF-8"
    ).useDelimiter("\\A").next().split("\n");
    return new TreeSet<>(
        Arrays.stream(lines)
            .map(l0 -> l0.split(":"))
            .map(l0 -> new Coordinates(l0[0], l0[1],
                l0[2].contains("->") ? l0[2].split("->")[1] : l0[2])
            ).collect(Collectors.toSet()));
  }

  public static ResolutionStats installAndMatch(Repository repo, Coordinates target, String gradleRef) throws IOException {

    ResolutionStats result = new ResolutionStats(target);
    Set<Coordinates> mvnRef = ResolutionStats.loadRef(gradleRef);
    Map<Artifact, Path> binaries = repo.installRuntimeArtifactsAt(target);
    assertFalse(binaries.isEmpty());

    mvnRef.forEach(refCoord -> {
      Optional<Coordinates> hit = binaries.keySet().stream()
          .filter(a -> a.getAt().equals(refCoord))
          .map(Artifact::getAt).findFirst();
      if (hit.isPresent()) { result.hit.add(refCoord); }
      else { result.miss.add(refCoord); }
    });
    binaries.keySet().forEach(a -> {
      if (!mvnRef.contains(a.getAt())) {
        result.slack.add(a.getAt());
      }
    });

    return result;
  }

  @Override public String toString() {
    return String.format("Hit: %03d, Miss: %03d, Slack: %03d -> %s",
        hit.size(), miss.size(), slack.size(), coordinates);
  }
}
