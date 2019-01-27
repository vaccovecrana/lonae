package io.vacco.myrica.core;

import org.joox.Match;

import java.util.Objects;

public class Module {

  private final ModuleMetadata metadata;
  private final Match pom;

  public Module(Match pom, ModuleMetadata m) {
    this.pom = Objects.requireNonNull(pom);
    this.metadata = Objects.requireNonNull(m);
  }

  public Match getPom() { return pom; }
  public ModuleMetadata getMetadata() { return metadata; }

  @Override public String toString() { return metadata.toString(); }
}
