package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URI;
import java.nio.file.*;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;

/**
 * NOTE: for now, only jar artifacts (the default Maven dependency type) are supported.
 */
public class Artifact implements Comparable<Artifact> {

  private final Coordinates at;
  private final String classifier;
  private final String scope;
  private final boolean optional;

  public Artifact(Coordinates at, String classifier, String scope, boolean optional) {
    this.at = requireNonNull(at);
    this.classifier = classifier;
    this.scope = scope;
    this.optional = optional;
  }

  public Artifact(Match xml) {
    this(new Coordinates(xml), xml.child("classifier").text(),
        xml.child("scope").text(), xml.child("optional").size() > 0);
  }

  public String getBaseArtifactName() {
    return format("%s%s", at.getBaseResourceName(), classifier != null ? format("-%s", classifier) : "");
  }

  public URI getResourceUri(URI origin, String resourceExtension) {
    return at.getBaseUri(origin).resolve(format("%s%s", getBaseArtifactName(), resourceExtension));
  }

  public URI getJarUri(URI origin) { return getResourceUri(origin, ".jar"); }
  public Path getLocalJarPath(Path root) { return Paths.get(getJarUri(root.toUri())); }

  public boolean isRuntime() {
    if (optional) return false;
    if (scope != null && scope.equalsIgnoreCase("test")) return false;
    return scope == null || !scope.equalsIgnoreCase("provided");
  }

  @Override public int compareTo(Artifact o) {
    return getBaseArtifactName().compareTo(o.getBaseArtifactName());
  }

  @Override public int hashCode() { return getBaseArtifactName().hashCode(); }
  @Override public boolean equals(Object o) {
    if (o instanceof Artifact) {
      Artifact a0 = (Artifact) o;
      return getBaseArtifactName().equals(a0.getBaseArtifactName());
    }
    return false;
  }

  public Coordinates getAt() { return at; }
}
