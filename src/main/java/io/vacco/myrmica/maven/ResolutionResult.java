package io.vacco.myrmica.maven;

import java.util.*;
import java.util.stream.*;

public class ResolutionResult {

  public final DependencyNode root;
  public final Set<Artifact> artifacts;

  public ResolutionResult(DependencyNode root) {
    this.root = Objects.requireNonNull(root);
    Map<String, List<Artifact>> aGroup = flatten(root).map(dn -> dn.getArtifact())
        .collect(Collectors.toCollection(TreeSet::new)).stream()
        .collect(Collectors.groupingBy(a -> a.getAt().getBaseCoordinates()));
    Map<Artifact, Artifact> replaceTargets = new TreeMap<>();
    aGroup.values().stream().filter(al -> al.size() > 1).forEach(al -> {
      List<Artifact> versions = al.stream()
          .filter(a -> a.getMetadata().classifier == null)
          .sorted().collect(Collectors.toList());
      if (versions.size() > 0) {
        Artifact replacement = versions.remove(versions.size() - 1);
        versions.forEach(ra -> {
          if (!ra.getAt().equals(replacement.getAt())) {
            replaceTargets.put(ra, replacement);
          }
        });
      }
    });
    flatten(root).filter(dn -> replaceTargets.containsKey(dn.getArtifact()))
        .forEach(dn -> dn.replaceWith(replaceTargets.get(dn.getArtifact())));
    this.artifacts = flatten(root)
        .map(DependencyNode::getEfectiveArtifact)
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private void flattenTail(DependencyNode n0, List<DependencyNode> dl) {
    dl.add(n0);
    n0.getChildren().forEach(dnc -> flattenTail(dnc, dl));
  }

  private Stream<DependencyNode> flatten(DependencyNode root0) {
    List<DependencyNode> dl = new ArrayList<>();
    flattenTail(root0, dl);
    return dl.stream();
  }

  private void printTreeTail(DependencyNode n0, StringBuilder out, Set<Integer> visitedHashes,
                             int level, int childIdx, int childCount) {
    for (int i = 0; i < level; i++) { out.append("  "); }
    if (level > 0) {
      if (childIdx == 0) out.append("+-- ");
      else if (childIdx == childCount - 1) out.append("\\-- ");
      else out.append("|-- ");
    }
    int childrenHash = n0.getChildren().stream()
        .map(DependencyNode::getEfectiveArtifact)
        .map(Artifact::toExternalForm)
        .collect(Collectors.joining()).hashCode();
    out.append(String.format("%s%s",
        n0.getArtifact().getMetadata().classifier != null ?
            n0.getArtifact().toExternalForm() : n0.getArtifact().getAt().toExternalForm(),
        n0.getReplacement() != null ? String.format(" -> %s", n0.getReplacement().getAt().getVersion()) : ""));
    if (!visitedHashes.contains(childrenHash)) {
      out.append('\n');
      for (int i = 0; i < n0.getChildren().size(); i++) {
        DependencyNode dnc = n0.getChildren().get(i);
        printTreeTail(dnc, out, visitedHashes, level + 1, i, n0.getChildren().size());
      }
    }
    else if (!n0.getChildren().isEmpty()) { out.append(" (*)\n"); }
    else { out.append('\n'); }
    visitedHashes.add(childrenHash);
  }

  public String printTree() {
    StringBuilder sb = new StringBuilder();
    printTreeTail(root, sb, new HashSet<>(), 0, 0, 1);
    return sb.toString();
  }
}
