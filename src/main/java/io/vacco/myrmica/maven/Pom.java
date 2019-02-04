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

  private final Match ePom;
  private final Artifact rootArtifact;
  private final Set<Artifact> defaultVersions;
  private final Set<Artifact> dependencies = new TreeSet<>();
  private String sourceUrl;

  public Pom(Match ePom) {
    this.ePom = Objects.requireNonNull(ePom);
    this.rootArtifact = new Artifact(ePom);
    this.defaultVersions = artifactsOf(
        ePom.child(PomTag.dependencyManagement.toString())
            .child(PomTag.dependencies.toString()));
  }

  private void setDefaults(Artifact dep, Artifact def) {
    dep.getAt().setVersion(def.getAt().getVersion());
    dep.getExclusions().addAll(def.getExclusions());
    if (dep.getScope().equals(Constants.scope_compile) && (!dep.getScope().equals(def.getScope()))) {
      dep.setScope(def.getScope());
    }
  }

  public void computeEffectiveDependencies() {
    dependencies.addAll(artifactsOf(ePom.child(PomTag.dependencies.toString())).stream().map(d0 -> {
      if (d0.getAt().getVersion() != null) return d0;
      List<Artifact> defaults = defaultVersions.stream()
          .filter(dv -> dv.getAt().matchesGroupAndArtifact(d0.getAt()))
          .collect(Collectors.toList());
      Optional<Artifact> oda = defaults.size() == 1 ?
          Optional.of(defaults.get(0)) :
          defaults.stream().filter(dv -> dv.getMetadata().equals(d0.getMetadata())).findFirst();
      if (oda.isPresent()) {
        setDefaults(d0, oda.get());
        return d0;
      }
      log.warn("Unable to resolve version metadata for {}", d0);
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toSet()));
  }

  public Set<Artifact> getDependencies() { return dependencies; }
  public Artifact getRootArtifact() { return rootArtifact; }
  public Set<Artifact> getDefaultVersions() { return defaultVersions; }
  public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
