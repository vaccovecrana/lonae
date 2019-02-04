package io.vacco.myrmica.maven;

import java.util.*;

public class DependencyNode {

  public DependencyNode parent;
  public final Pom pom;
  public final Artifact artifact;
  public final List<DependencyNode> children = new ArrayList<>();

  DependencyNode(Pom p, Artifact a, DependencyNode parent) {
    this.pom = Objects.requireNonNull(p);
    this.artifact = Objects.requireNonNull(a);
    this.parent = parent;
  }

  DependencyNode(Pom p) { this(p, p.getRootArtifact(), null); }

  boolean excludes(Artifact a) {
    DependencyNode n0 = this;
    while (n0 != null) {
      if (n0.artifact.excludes(a)) return true;
      n0 = n0.parent;
    }
    return false;
  }

  boolean isTopLevelOverride(Artifact a) {
    DependencyNode n0 = this;
    while (n0 != null) {
      boolean overrides = n0.pom.getDependencies().stream()
          .filter(Artifact::isRuntimeClassPath).anyMatch(ta -> {
            boolean sameCoords = ta.getAt().matchesGroupAndArtifact(a.getAt());
            boolean diffVer = !ta.getAt().getVersion().equals(a.getAt().getVersion());
            return sameCoords && diffVer;
          });
      if (overrides) return true;
      n0 = n0.parent;
    }
    return false;
  }

  @Override public String toString() {
    return String.format("[%s%s]",
        parent != null ? String.format("%s <-- ", parent.pom.getRootArtifact().getBaseArtifactName()) : "",
        pom.getRootArtifact().getBaseArtifactName());
  }
}
