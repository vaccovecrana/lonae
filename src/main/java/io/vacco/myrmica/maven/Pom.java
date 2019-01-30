package io.vacco.myrmica.maven;

import org.joox.Match;
import java.util.Set;
import static io.vacco.myrmica.maven.Artifact.*;
import static java.util.Objects.*;

public class Pom {

  private final Match ePom;
  private final Set<Artifact> defaultVersions;
  private final Set<Artifact> dependencies;

  public Pom(Match ePom) {
    this.ePom = requireNonNull(ePom);
    this.defaultVersions = artifactsOf(ePom.child("dependencyManagement").child("dependencies"));
    this.dependencies = artifactsOf(ePom.child("dependencies"));
  }

  public Set<Artifact> getDefaultVersions() { return defaultVersions; }
  public Set<Artifact> getDependencies() { return dependencies; }
}
