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

  public Set<Artifact> getDependencies(boolean onlyRuntime) {
    Set<Artifact> result = new TreeSet<>();
    result.addAll(dependencies.stream()
        .filter(a -> !onlyRuntime || a.isRuntime())
        .map(d0 -> {
          if (d0.getAt().getVersion() != null) return Optional.of(d0);
          return defaultVersions.stream()
              .filter(dv -> dv.getAt().matchesGroupAndArtifact(d0.getAt()))
              .findFirst();
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet()));
    return result;
  }

  public Artifact getRootArtifact() { return rootArtifact; }
  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
