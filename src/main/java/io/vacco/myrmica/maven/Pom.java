package io.vacco.myrmica.maven;

import org.joox.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.Artifact.*;
import static io.vacco.myrmica.maven.Constants.*;

public class Pom {

  private static final Logger log = LoggerFactory.getLogger(Pom.class);

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

  public Set<Artifact> getDependencies() {
    Set<Artifact> result = new TreeSet<>();
    result.addAll(dependencies.stream().map(d0 -> {
      if (d0.getAt().getVersion() != null) return d0;
      Optional<Artifact> oda = defaultVersions.stream()
          .filter(dv -> dv.getAt().matchesGroupAndArtifact(d0.getAt())).findFirst();
      if (oda.isPresent()) {
        d0.getAt().setVersion(oda.get().getAt().getVersion());
        return d0;
      }
      log.warn("Unable to resolve version metadata for {}", d0);
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toSet()));
    return result;
  }

  public Artifact getRootArtifact() { return rootArtifact; }
  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
