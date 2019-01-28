package io.vacco.myrica.core;

import org.joox.Match;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.*;
import static org.joox.JOOX.*;

public class Repository {

  private static final Pattern propertyRegex = Pattern.compile(".*?\\$\\{(.*?)\\}.*?");

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

  public Optional<Module> getParent(Module m) {
    requireNonNull(m);
    Match parent = m.getPom().child("parent");
    if (parent.size() == 0) return Optional.empty();
    return Optional.of(loadPom(new ModuleMetadata(
        parent.child("groupId").text(),
        parent.child("artifactId").text(),
        parent.child("version").text(), null)));
  }

  private String removeVarTokens(String key) {
    if (key == null) {
      return null;
    }
    return key
        .replace("$", "")
        .replace("{", "")
        .replace("}", "");
  }

  private List<String> scanProperties(String raw) {
    List<String> result = new ArrayList<>();
    if (raw == null) return result;
    Matcher matchPattern = propertyRegex.matcher(raw);
    while (matchPattern.find()) { result.add(matchPattern.group(1)); }
    return result;
  }

  private String dereference(String property, Map<String, String> resolvedKeys) {
    for (String keyRef : scanProperties(property)) {
      String propVal = resolvedKeys.get(keyRef);
      property = property.replace(keyRef, propVal);
    }
    return removeVarTokens(property);
  }

  private String resolveKeyReferences(String key, Map<String, String> raw) {
    String rawValue = raw.get(key);
    List<String> keyRefs = scanProperties(rawValue);
    if (keyRefs.isEmpty()) return rawValue == null ? "???" : rawValue;
    for (String keyRef : keyRefs) {
      String keyRefVal = resolveKeyReferences(keyRef, raw);
      rawValue = rawValue.replace(keyRef, keyRefVal);
    }
    return removeVarTokens(rawValue);
  }

  private Map<String, String> resolveProperties(Map<String, String> raw, Map<String, String> resolved) {
    raw.keySet().forEach(key -> resolved.put(key, resolveKeyReferences(key, raw)));
    return resolved;
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

  private ModuleMetadata loadMetadata(Match moduleXml, Map<String, String> moduleProperties) {
    String groupId = moduleXml.child("groupId").text();
    String artifactId = moduleXml.child("artifactId").text();
    String version = moduleXml.child("version").text();
    String scope = moduleXml.child("scope").text();
    return new ModuleMetadata(
        dereference(groupId, moduleProperties),
        dereference(artifactId, moduleProperties),
        dereference(version, moduleProperties), scope);
  }

  private Set<ModuleMetadata> loadDependencyManagement(Module root, Map<String, String> moduleProperties) {
    Set<ModuleMetadata> result = new HashSet<>();
    Optional<Module> om = Optional.of(root);
    while (om.isPresent()) {
      Match depMgmt = om.get().getPom().child("dependencyManagement").child("dependencies");
      result.addAll(depMgmt.children().each().stream()
          .map(dm -> loadMetadata(dm, moduleProperties)) // TODO account for dependency exclusions too
          .collect(Collectors.toSet()));
      om = getParent(om.get());
    }
    return result;
  }

  private Collection<Module> resolveTail(Module root, Set<Module> resolved) {
    System.out.println(root);
    Map<String, String> moduleProps = collectProperties(root);
    Set<ModuleMetadata> defaultModules = loadDependencyManagement(root, moduleProps);
    resolved.add(root);
    root.getPom().child("dependencies").children().each().stream()
        .filter(el -> el.child("classifier").size() == 0) // TODO account for native dependencies too.
        .filter(el -> {
          String scope = el.child("scope").text();
          boolean isOptional = el.child("optional").isNotEmpty();
          boolean isTest = scope != null && scope.equalsIgnoreCase("test");
          boolean isProvided = scope != null && scope.equalsIgnoreCase("provided");
          return !(isTest || isProvided || isOptional);
        }).map(el -> {
          ModuleMetadata mm = loadMetadata(el, moduleProps);
          if (mm.getVersion() == null) {
            return defaultModules.stream().filter(mm::matchesGroupAndArtifact).findFirst();
          }
          return Optional.of(mm);
        }).filter(Optional::isPresent).map(Optional::get).forEach(mm0 -> {
          resolved.addAll(resolveTail(loadPom(mm0), resolved));
        });
    return resolved;
  }

  public Collection<Module> resolveDependencies(Module root) {
    return resolveTail(root, new TreeSet<>());
  }
}
  /*
  public Module createModule(String groupId, String artifactId, String artifactVersion) throws IOException {
    Module module = new Module(localRoot, groupId, artifactId, artifactVersion);
    buildDependencyGraph(module);
    return module;
  }

  protected void buildDependencyGraph(Module module) {

    List<Dependency> dependencies = readDependenciesForModule(module);

    List<Module> moduleDependencies = new ArrayList<Module>();

    for (Dependency d ependency : dependencies) {
      if (dependency.isRuntimeDependency()) {
        moduleDependencies.add(createModule(dependency.groupId, dependency.artifactId, dependency.version));
      }
    }

    module.setDependencies(moduleDependencies);

    for (Module moduleDepdendency : moduleDependencies) {
      buildDependencyGraph(moduleDepdendency);
    }
  }

  protected List<Module> readDependenciesForModule(Module module) {
    try {
      File modulePomPath = getModulePomPath(module);
      Match pom = $(modulePomPath);
      return ModuleDependencyReader.readDependencies(pom);
    } catch (Exception e) {
      throw new IllegalStateException(
          String.format("Unable to read dependencies of [%s]", module), e);
    }
  }

  private File getModulePomPath(Module module) {
    String pomName = String.format("%s-%s.pom", module.getArtifactId(), module.getVersion());
    File f = Paths.get(this.rootDir, module.getFullName(), pomName).toFile();
    return f;
  }

  public void installModule(String remoteRepositoryBaseUrl, String groupId, String artifactId, String artifactVersion) throws IOException {
    ModuleDownloader moduleDownloader = new ModuleDownloader(remoteRepositoryBaseUrl, this.rootDir);
    moduleDownloader.download(groupId, artifactId, artifactVersion);
  }

  public void installModuleAndDependencies(String remoteRepositoryBaseUrl, String groupId, String artifactId, String artifactVersion) throws Exception {
    installModule(remoteRepositoryBaseUrl, groupId, artifactId, artifactVersion);
    Module module = new Module(groupId, artifactId, artifactVersion);
    List<Dependency> dependencies = readDependenciesForModule(module);
    for (Dependency dependency : dependencies) {
      if (!"test".equals(dependency.scope)) {
        installModuleAndDependencies(remoteRepositoryBaseUrl, dependency.groupId, dependency.artifactId, dependency.version);
      }
    }
  }

}

*/
