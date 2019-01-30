package io.vacco.myrmica.maven;

import java.util.Objects;

public class DependencyNode {

  public final Pom pom;
  public final Artifact artifact;
  private final DependencyNode parent;

  public DependencyNode(Pom p, Artifact a, DependencyNode parent) {
    this.pom = Objects.requireNonNull(p);
    this.artifact = Objects.requireNonNull(a);
    this.parent = parent;
  }

  public DependencyNode(Pom p) { this(p, p.getRootArtifact(), null); }

  public boolean excludes(Artifact a) {
    DependencyNode n0 = this;
    while (n0 != null) {
      if (n0.artifact.excludes(a)) return true;
      n0 = n0.parent;
    }
    return false;
  }

  public boolean isTopLevelOverride(Artifact a) {
    DependencyNode n0 = this;
    DependencyNode top = this;
    while (n0 != null) {
      if (n0.parent != null) top = n0.parent;
      n0 = n0.parent;
    }
    boolean overrides = top.pom.getDependencies(false).stream().anyMatch(ta -> {
      boolean sameCoords = ta.getAt().matchesGroupAndArtifact(a.getAt());
      boolean diffVer = !ta.getAt().getVersion().equals(a.getAt().getVersion());
      return sameCoords && diffVer;
    });
    return overrides;
  }

  @Override public String toString() {
    return String.format("[%s%s]",
        parent != null ? String.format("%s <-- ", parent.pom) : "", pom);
  }
}
