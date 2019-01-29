package io.vacco.myrica.core;

import org.joox.Match;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.*;
import static org.joox.JOOX.*;
import static io.vacco.myrmica.util.PropertyAccess.*;

public class MavenRepository {
/*



  public Optional<Module> loadParent(Module m) { // TODO assuming that no parent reference can use property access.
    requireNonNull(m);
    Match parent = m.getPom().child("parent");
    if (parent.size() == 0) return Optional.empty();
    return Optional.of(loadPom(new ModuleMetadata(parent, new HashMap<>())));
  }

  private Set<ModuleMetadata> loadDependencyManagement(Module root, Map<String, String> moduleProperties) {
    Set<ModuleMetadata> result = new TreeSet<>();
    Optional<Module> om = Optional.of(root);
    while (om.isPresent()) {
      Match depMgmt = om.get().getPom().child("dependencyManagement").child("dependencies");
      result.addAll(depMgmt.children().each().stream()
          .map(dm -> new ModuleMetadata(dm, moduleProperties)) // TODO account for dependency exclusions too
          .collect(Collectors.toSet()));
      om = loadParent(om.get());
    }
    return result;
  }

  public Map<String, String> collectProperties(Module m) {
    requireNonNull(m);
    Map<String, String> result = new TreeMap<>();
    List<Module> ancestorModules = new ArrayList<>();
    Optional<Module> op = Optional.of(m);
    while (op.isPresent()) {
      ancestorModules.add(op.get());
      op = loadParent(op.get());
    }
    Collections.reverse(ancestorModules);
    for (Module ancestorModule : ancestorModules) {
      result.putAll(loadProperties(ancestorModule));
    }
    return resolveProperties(result, new TreeMap<>());
  }

  private Collection<Module> resolveTail(Module root, Set<Module> resolved) {
    if (!root.getMetadata().isRuntime() || resolved.contains(root)) {
      return Collections.emptySet();
    }
    System.out.println(root);
    resolved.add(root);
    Map<String, String> moduleProps = collectProperties(root);
    Set<ModuleMetadata> defaultModules = loadDependencyManagement(root, moduleProps);
    root.getPom().child("dependencies").children().each().stream()
        .map(el -> new ModuleMetadata(el, moduleProps))
        .filter(ModuleMetadata::isRuntime)
        .map(mm0 -> {
          if (mm0.getVersion() == null) {
            return defaultModules.stream().filter(mm0::matchesGroupAndArtifact).findFirst();
          }
          return Optional.of(mm0);
        }).filter(Optional::isPresent).map(Optional::get).forEach(mm0 -> {
          Module pom0 = loadPom(mm0);
          Collection<Module> pom0Resolved = resolveTail(pom0, resolved);
          pom0Resolved = pom0Resolved.stream().filter(resolvedMod ->
              mm0.getExclusions().stream().noneMatch(ex0 -> ex0.matchesGroupAndArtifact(resolvedMod.getMetadata()))
          ).collect(Collectors.toSet());
          resolved.addAll(pom0Resolved);
        });
    return resolved;
  }

  public Collection<Module> resolveDependencies(Module root) {
    return resolveTail(root, new TreeSet<>());
  }

  */
}
