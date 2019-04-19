package io.vacco.myrmica.maven.schema;

import org.joox.Match;
import org.slf4j.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.schema.Artifact.*;
import static io.vacco.myrmica.maven.schema.Constants.*;

public class Pom {

  private static final Logger log = LoggerFactory.getLogger(Pom.class);

  private final Match ePom;
  private final Artifact rootArtifact;
  private final Set<Artifact> defaultVersions;
  private final Set<Artifact> dependencies = new TreeSet<>();

  public Pom(Match ePom) {
    this.ePom = Objects.requireNonNull(ePom);
    this.rootArtifact = Artifact.from(ePom);
    this.defaultVersions = artifactsOf(
        ePom.child(PomTag.dependencyManagement.toString())
            .child(PomTag.dependencies.toString()));
  }

  private void setDefaults(Artifact dep, Artifact def) {
    dep.at.version = def.at.version;
    dep.exclusions.addAll(def.exclusions);
    if (dep.scope.equals(Constants.scope_compile) && (!dep.scope.equals(def.scope))) {
      dep.setScope(def.scope);
    }
  }

  public void computeEffectiveDependencies() {
    dependencies.addAll(artifactsOf(ePom.child(PomTag.dependencies.toString())).stream().map(d0 -> {
      if (d0.at.version != null) return d0;
      List<Artifact> defaults = defaultVersions.stream()
          .filter(dv -> dv.at.matchesGroupAndArtifact(d0.at))
          .collect(Collectors.toList());
      Optional<Artifact> oda = defaults.size() == 1 ?
          Optional.of(defaults.get(0)) :
          defaults.stream().filter(dv -> dv.metadata.equals(d0.metadata)).findFirst();
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

  @Override public String toString() { return rootArtifact.toExternalForm(); }
}
