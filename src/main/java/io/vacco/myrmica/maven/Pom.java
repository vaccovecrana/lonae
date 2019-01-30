package io.vacco.myrmica.maven;

import org.joox.Match;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.Artifact.*;
import static java.util.Objects.*;

public class Pom {

  private final Match ePom;
  private final Artifact rootArtifact;
  private final Set<Artifact> defaultVersions;
  private final Set<Artifact> dependencies;

  public Pom(Match ePom) {
    this.ePom = requireNonNull(ePom);
    this.rootArtifact = Artifact.fromXml(ePom);
    this.defaultVersions = artifactsOf(ePom.child("dependencyManagement").child("dependencies"));
    this.dependencies = artifactsOf(ePom.child("dependencies"));
  }

  public Set<Artifact> getRuntimeDependencies() {
    Set<Artifact> result = new TreeSet<>();
    result.addAll(dependencies.stream().filter(Artifact::isRuntime)
        .map(d0 -> {
          if (d0.getAt().getVersion() == null) {
            Optional<Artifact> defaultV = defaultVersions.stream()
                .filter(dv -> dv.getAt().matchesGroupAndArtifact(d0.getAt()))
                .findFirst();
            if (defaultV.isPresent()) return defaultV.get();
          }
          return d0;
        }).collect(Collectors.toSet()));
    return result;
  }

  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
