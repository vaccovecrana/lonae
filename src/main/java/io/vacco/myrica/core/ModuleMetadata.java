package io.vacco.myrica.core;

import org.joox.Match;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.util.PropertyAccess.*;
/*
public class ModuleMetadata implements Comparable<ModuleMetadata> {

  private static final String MOD_FMT = "(@[%s], scope: [%s], classifier: [%s])";

  private final String classifier;
  private final String scope;
  private final boolean optional;
  private final Set<ModuleMetadata> exclusions = new TreeSet<>();

  public ModuleMetadata(Match pomXml, Map<String, String> resolvedProperties) {
    Objects.requireNonNull(pomXml);
    Objects.requireNonNull(resolvedProperties);
    this.groupId = dereference(pomXml.child("groupId").text(), resolvedProperties);
    this.artifactId = dereference(pomXml.child("artifactId").text(), resolvedProperties);
    this.version = dereference(pomXml.child("version").text(), resolvedProperties);
    this.classifier = dereference(pomXml.child("classifier").text(), resolvedProperties);
    this.scope = pomXml.child("scope").text();
    this.optional = pomXml.child("optional").isNotEmpty();
    this.exclusions.addAll(pomXml.child("exclusions").children("exclusion")
        .each().stream().map(el -> new ModuleMetadata(el, resolvedProperties))
        .collect(Collectors.toSet()));
  }

  public ModuleMetadata(String groupId, String artifactId, String version) {
    this.groupId = Objects.requireNonNull(groupId);
    this.artifactId = Objects.requireNonNull(artifactId);
    this.version = Objects.requireNonNull(version);
    this.classifier = null;
    this.scope = null;
    this.optional = false;
  }


  public URI getResourceUri(URI origin, String resourceExtension, boolean includeClassifier) {


  }

  public URI getJarUri(URI origin) { return getResourceUri(origin, ".jar", true); }
  public Path getLocalJarPath(Path root) { return Paths.get(getJarUri(root.toUri())); }






  public Set<ModuleMetadata> getExclusions() { return exclusions; }
  public String getGroupId() { return groupId; }
  public String getArtifactId() { return artifactId; }
  public String getVersion() { return version; }

  public String toString() {
    return String.format(MOD_FMT, getCoordinates(), scope, classifier);
  }

  @Override public int compareTo(ModuleMetadata other) {
    return getCoordinates().compareTo(other.getCoordinates());
  }






  public boolean isRuntime() {
    if (optional) return false;
    if (scope != null && scope.equalsIgnoreCase("test")) return false;
    return scope == null || !scope.equalsIgnoreCase("provided");
  }
}
*/
