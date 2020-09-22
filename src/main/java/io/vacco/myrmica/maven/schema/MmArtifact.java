package io.vacco.myrmica.maven.schema;

import java.util.*;
import static java.lang.String.*;

public class MmArtifact implements Comparable<MmArtifact> {

  public MmCoordinates at;
  public MmComponent metadata;
  public boolean optional;
  public MmConstants.Scope scope;

  public final Set<MmArtifact> exclusions = new TreeSet<>();

  public String getBaseArtifactName() {
    return format("%s-%s%s.%s",
        at != null ? at.artifactId : "", at != null ? at.version : "",
        metadata != null && metadata.classifier != null ? format("-%s", metadata.classifier) : "",
        metadata != null ? metadata.extension : ""
    );
  }

  public boolean excludes(MmArtifact a) {
    return exclusions.stream().anyMatch(e -> e.at.getArtifactFormat().equals(a.at.getArtifactFormat()));
  }

  @Override public int compareTo(MmArtifact o) { return getBaseArtifactName().compareTo(o.getBaseArtifactName()); }
  @Override public int hashCode() { return getBaseArtifactName().hashCode(); }
  @Override public boolean equals(Object o) {
    return o instanceof MmArtifact && ((MmArtifact) o).getBaseArtifactName().equals(getBaseArtifactName());
  }

  @Override public String toString() {
    return format("%s%s%s%s",
        scope != null ? format(" (%s) ", scope) : "",
        exclusions.size() > 0 ? format("{-%s} ", exclusions.size()) : "",
        getBaseArtifactName(),
        metadata != null ? format(" %s ", metadata) : ""
    );
  }
}
