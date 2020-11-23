package io.vacco.lonae.maven.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.vacco.lonae.maven.impl.MmArtifactDeserializer;

import static java.lang.String.*;

@JsonDeserialize(using = MmArtifactDeserializer.class)
public class MmArtifact implements Comparable<MmArtifact> {

  public MmCoordinates at;
  public MmComponent comp;
  public MmArtifactMeta meta;
  public MmArtifact upstream;

  public String baseArtifactName() {
    return format("%s-%s%s.%s",
        at != null ? at.artifactId : "", at != null ? at.version : "",
        comp != null && comp.classifier != null && comp.classifier.length() > 0 ? format("-%s", comp.classifier) : "",
        comp != null ? comp.type : ""
    );
  }

  public boolean inRuntimeClasspath() {
    if (meta.optional) return false;
    if (meta.scopeType == MmArtifactMeta.Scope.Test) return false;
    if (meta.scopeType == MmArtifactMeta.Scope.Provided) return false;
    return true;
  }

  public MmArtifact withUpstream(MmArtifact upstream) {
    this.upstream = upstream;
    return this;
  }

  public MmArtifact copy() {
    MmArtifact a0 = new MmArtifact();
    a0.comp = new MmComponent();
    a0.comp.type = this.comp.type;
    a0.comp.packaging = this.comp.packaging;
    a0.comp.language = this.comp.language;
    a0.comp.includesDependencies = this.comp.includesDependencies;
    a0.comp.classifier = this.comp.classifier;
    a0.comp.addedToClasspath = this.comp.addedToClasspath;
    a0.at = this.at;
    a0.meta = this.meta;
    a0.upstream = this.upstream;
    return a0;
  }

  @Override public int compareTo(MmArtifact o) { return o.baseArtifactName().compareTo(baseArtifactName()); }
  @Override public int hashCode() { return baseArtifactName().hashCode(); }
  @Override public boolean equals(Object o) {
    return o instanceof MmArtifact && ((MmArtifact) o).baseArtifactName().equals(baseArtifactName());
  }

  @Override public String toString() {
    return String.format("%s %s", at, meta);
  }
}
