package io.vacco.myrmica.maven.schema;

import io.vacco.myrmica.maven.util.MmArtifacts;
import org.joox.Match;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static io.vacco.myrmica.maven.schema.MmConstants.*;
import static java.util.stream.Collectors.*;

public class MmPom {

  public final Match effectiveXml;
  public final MmCoordinates coordinates;
  public final Set<MmCoordinates> extraVersions = new TreeSet<>();

  public MmPom(MmCoordinates coords, Match data) {
    this.coordinates = coords;
    this.effectiveXml = data;
  }

  public List<MmArtifact> getRuntimeDependencies() {
    return effectiveXml.child(PomTag.dependencies.name())
        .children(PomTag.dependency.name())
        .each().stream().map(MmArtifacts::fromXml)
        .filter(MmArtifacts::isRuntimeClassPath)
        .collect(toList());
  }
}
