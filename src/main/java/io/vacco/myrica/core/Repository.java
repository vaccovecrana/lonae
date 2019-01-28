package io.vacco.myrica.core;

import org.joox.Match;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.*;
import static org.joox.JOOX.*;
import static io.vacco.myrica.core.PropertyAccess.*;

public class Repository {

  private Path localRoot;
  private URI remoteRoot;

  public Repository(String localRootPath, String remotePath) {
    this.localRoot = Paths.get(requireNonNull(localRootPath));
    if (!localRoot.toFile().exists()) {
      throw new IllegalArgumentException(
          String.format("Missing root folder: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!localRoot.toFile().isDirectory()) {
      throw new IllegalArgumentException(
          String.format("Not a directory: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!requireNonNull(remotePath).endsWith("/")) {
      throw new IllegalArgumentException(
          String.format("Remote path does not end with a trailing slash: [%s]", remotePath));
    }
    try { this.remoteRoot = new URI(remotePath); }
    catch (URISyntaxException e) { throw new IllegalStateException(e); }
  }

  public Module loadPom(ModuleMetadata m) {
    requireNonNull(m);
    try {
      Path target = m.getLocalPomPath(localRoot);
      if (!target.toFile().getParentFile().exists()) {
        target.toFile().getParentFile().mkdirs();
      }
      Match pom;
      if (!target.toFile().exists()) {
        Files.copy(m.getPomUri(remoteRoot).toURL().openStream(), target);
      }
      pom = $(target.toFile());
      return new Module(pom, m);
    }
    catch (Exception e) { throw new IllegalStateException(e); }
  }

  public Optional<Module> getParent(Module m) { // TODO assuming that no parent reference can use property access.
    requireNonNull(m);
    Match parent = m.getPom().child("parent");
    if (parent.size() == 0) return Optional.empty();
    return Optional.of(loadPom(new ModuleMetadata(parent, new HashMap<>())));
  }

  private Map<String, String> loadProperties(Module m) {
    requireNonNull(m);
    Map<String, String> result = new TreeMap<>();
    Match props = m.getPom().child("properties");
    props.children().forEach(prop -> result.put(prop.getTagName(), prop.getTextContent()));
    return result;
  }

  public Map<String, String> collectProperties(Module m) {
    requireNonNull(m);
    Map<String, String> result = new TreeMap<>();
    Optional<Module> op = Optional.of(m);
    while (op.isPresent()) {
      result.putAll(loadProperties(op.get()));
      op = getParent(op.get());
    }
    result.put("project.groupId", m.getMetadata().getGroupId());
    result.put("project.artifactId", m.getMetadata().getArtifactId());
    result.put("project.version", m.getMetadata().getVersion());
    return resolveProperties(result, new TreeMap<>());
  }

  private Set<ModuleMetadata> loadDependencyManagement(Module root, Map<String, String> moduleProperties) {
    Set<ModuleMetadata> result = new HashSet<>();
    Optional<Module> om = Optional.of(root);
    while (om.isPresent()) {
      Match depMgmt = om.get().getPom().child("dependencyManagement").child("dependencies");
      result.addAll(depMgmt.children().each().stream()
          .map(dm -> new ModuleMetadata(dm, moduleProperties)) // TODO account for dependency exclusions too
          .collect(Collectors.toSet()));
      om = getParent(om.get());
    }
    return result;
  }

  private Collection<Module> resolveTail(Module root, Set<Module> resolved) {
    System.out.println(root);
    if (!root.getMetadata().isRuntime()) {
      return Collections.emptySet();
    }
    resolved.add(root);
    Map<String, String> moduleProps = collectProperties(root);
    Set<ModuleMetadata> defaultModules = loadDependencyManagement(root, moduleProps);
    root.getPom().child("dependencies").children().each().stream()
        .filter(el -> el.child("classifier").size() == 0) // TODO account for native dependencies too.
        .map(el -> new ModuleMetadata(el, moduleProps))
        .filter(ModuleMetadata::isRuntime)
        .map(mm0 -> {
          if (mm0.getVersion() == null) {
            return defaultModules.stream().filter(mm0::matchesGroupAndArtifact).findFirst();
          }
          return Optional.of(mm0);
        }).filter(Optional::isPresent).map(Optional::get).forEach(mm0 -> {
          resolved.addAll(resolveTail(loadPom(mm0), resolved));
        });
    return resolved;
  }

  public Collection<Module> resolveDependencies(Module root) {
    return resolveTail(root, new TreeSet<>());
  }
}
