package io.vacco.myrmica.maven;

import java.util.*;
import java.util.stream.*;

public class ResolutionResult {

  public final DependencyNode root;
  public final Set<Artifact> artifacts;

  public ResolutionResult(DependencyNode root) {
    this.root = Objects.requireNonNull(root);
    Map<String, List<Artifact>> aGroup = flatten(root).map(dn -> dn.artifact)
        .collect(Collectors.toCollection(TreeSet::new)).stream()
        .collect(Collectors.groupingBy(a -> a.getAt().getBaseCoordinates()));
    Set<Artifact> removeTargets = new TreeSet<>();
    aGroup.values().stream().filter(al -> al.size() > 1).forEach(al -> {
      List<Artifact> versions = al.stream().sorted().collect(Collectors.toList());
      versions.remove(versions.size() - 1);
      removeTargets.addAll(versions);
    });
    flatten(root).filter(dn -> removeTargets.contains(dn.artifact)).forEach(dn -> {
      dn.parent.children.remove(dn); // TODO instead, nodes need their artifact version replaced by the selected default.
      dn.parent = null;
    });
    this.artifacts = flatten(root).map(dn -> dn.artifact)
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private void flattenTail(DependencyNode n0, List<DependencyNode> dl) {
    dl.add(n0);
    n0.children.forEach(dnc -> flattenTail(dnc, dl));
  }

  private Stream<DependencyNode> flatten(DependencyNode root0) {
    List<DependencyNode> dl = new ArrayList<>();
    flattenTail(root0, dl);
    return dl.stream();
  }

  private void printTreeTail(DependencyNode n0, StringBuilder out, Set<String> visitedHashes,
                             int level, int childIdx, int childCount) {
    for (int i = 0; i < level; i++) { out.append("  "); }
    if (level > 0) {
      if (childIdx == 0) out.append("+-- ");
      else if (childIdx == childCount - 1) out.append("\\-- ");
      else out.append("|-- ");
    }
    String childrenHash = n0.children.stream()
        .map(dn -> dn.artifact.getAt().toExternalForm())
        .collect(Collectors.joining());
    out.append(n0.artifact.getAt());
    if (!visitedHashes.contains(childrenHash)) {
      out.append('\n');
      for (int i = 0; i < n0.children.size(); i++) {
        DependencyNode dnc = n0.children.get(i);
        printTreeTail(dnc, out, visitedHashes, level + 1, i, n0.children.size());
      }
    }
    else if (!n0.children.isEmpty()) { out.append(" (*)\n"); }
    else { out.append('\n'); }
    visitedHashes.add(childrenHash);
  }

  public String printTree() {
    StringBuilder sb = new StringBuilder();
    printTreeTail(root, sb, new HashSet<>(), 0, 0, 1);
    return sb.toString();
  }
}
