package io.vacco.myrmica.maven.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.vacco.myrmica.maven.impl.MmArtifactDeserializer;
import static java.lang.String.*;

@JsonDeserialize(using = MmArtifactDeserializer.class)
public class MmArtifact implements Comparable<MmArtifact> {

  public MmCoordinates at;
  public MmComponent comp;
  public MmArtifactMeta meta;
  private MmArtifact upstream;

  public String baseArtifactName() {
    return format("%s-%s%s.%s",
        at != null ? at.artifactId : "", at != null ? at.version : "",
        comp != null && comp.classifier != null ? format("-%s", comp.classifier) : "",
        comp != null ? comp.type : ""
    );
  }

  public boolean excludes(MmArtifact a) {
    MmArtifact c = this;
    boolean excluded = false;
    while (c != null) {
      excluded = meta.exclusions.stream().anyMatch(e -> e.artifactFormat().equals(a.at.artifactFormat()));
      if (excluded) { break; }
      else { c = c.upstream; }
    }
    return excluded;
  }

  public boolean inRuntime() {
    boolean notRt = meta.optional
        || meta.scopeType == MmArtifactMeta.Scope.Test
        || meta.scopeType == MmArtifactMeta.Scope.Provided;
    return !notRt;
  }

  public MmArtifact withUpstream(MmArtifact up) {
    this.upstream = up;
    return this;
  }

  @Override public int compareTo(MmArtifact o) { return baseArtifactName().compareTo(o.baseArtifactName()); }
  @Override public int hashCode() { return baseArtifactName().hashCode(); }
  @Override public boolean equals(Object o) {
    return o instanceof MmArtifact && ((MmArtifact) o).baseArtifactName().equals(baseArtifactName());
  }

  @Override public String toString() {
    return String.format("%s %s", at, comp);
  }
}
