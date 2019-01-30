package io.vacco.myrmica.maven;

import org.joox.Match;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.Artifact.*;
import static io.vacco.myrmica.maven.Constants.*;

public class Pom {

  private final Artifact rootArtifact;
  private final Set<Artifact> defaultVersions;
  private final Set<Artifact> dependencies;

  public Pom(Match ePom) {
    this.rootArtifact = new Artifact(ePom);
    this.defaultVersions = artifactsOf(
        ePom.child(PomTag.dependencyManagement.toString())
            .child(PomTag.dependencies.toString()));
    this.dependencies = artifactsOf(ePom.child(PomTag.dependencies.toString()));
  }

  public Set<Artifact> getRuntimeDependencies() {
    Set<Artifact> result = new TreeSet<>();
    result.addAll(dependencies.stream().filter(Artifact::isRuntime)
        .map(d0 -> {
          Optional<Artifact> defaultV = defaultVersions.stream()
              .filter(dv -> dv.getAt().matchesGroupAndArtifact(d0.getAt()))
              .findFirst();
          return defaultV.orElse(d0);
        }).collect(Collectors.toSet()));
    return result;
  }

  public Artifact getRootArtifact() { return rootArtifact; }
  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
