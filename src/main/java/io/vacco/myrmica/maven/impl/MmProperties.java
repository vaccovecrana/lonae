package io.vacco.myrmica.maven.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vacco.myrmica.maven.schema.*;
import org.slf4j.*;
import java.util.*;
import java.util.regex.*;

import static java.lang.String.format;

public class MmProperties {

  private static final Logger log = LoggerFactory.getLogger(MmProperties.class);
  private static final ObjectMapper om = new ObjectMapper();
  private static final Pattern propertyRegex = Pattern.compile(".*?\\$\\{(.*?)\\}.*?");

  private static List<String> scanProperties(String raw) {
    List<String> result = new ArrayList<>();
    if (raw == null) return result;
    Matcher matchPattern = propertyRegex.matcher(raw);
    while (matchPattern.find()) { result.add(matchPattern.group(1)); }
    return result;
  }

  private static String toPropFmt(String in) {
    return format("${%s}", in);
  }

  private static String dereferenceKey(String keyRef, MmVarContext ctx) {
    Object rawVal = ctx.get(keyRef);
    String propVal = rawVal == null ? "???" : rawVal.toString();
    if (propVal.contains("${")) { return dereference(propVal, ctx); }
    return propVal;
  }

  private static String dereference(String property, MmVarContext ctx) {
    List<String> keyRefs = scanProperties(property);
    for (String keyRef : keyRefs) {
      String val = dereferenceKey(keyRef, ctx);
      if (val == null) {
        log.warn("Unresolved property usage: [{}]", property);
        property = property.replace(toPropFmt(keyRef), format("${%s = ???}", keyRef));
      }
      property = property.replace(toPropFmt(keyRef), val);
    }
    return property;
  }

  private static void dereference(MmCoordinates c, MmVarContext ctx) {
    c.groupId = dereference(c.groupId, ctx);
    c.artifactId = dereference(c.artifactId, ctx);
    c.version = dereference(c.version, ctx);
  }

  private static String toPCase(String in) {
    return in.substring(0, 1).toUpperCase() + in.substring(1);
  }

  private static void resolveReferencesOf(MmArtifact art, MmVarContext ctx) {
    if (art.meta.scope != null) {
      art.meta.scope = toPCase(dereference(art.meta.scope, ctx));
      art.meta.scopeType = MmArtifactMeta.Scope.valueOf(art.meta.scope);
    }
    dereference(art.at, ctx);
    for (MmCoordinates ex : art.meta.exclusions) {
      dereference(ex, ctx);
    }
    art.comp = MmRepository.defaultComps.get(art.comp.type == null ? MmComponent.Type.jar : art.comp.type);
  }

  public static void resolveVarReferences(MmPom pom, MmVarContext ctx) {
    pom.properties.entrySet().stream()
        .filter(e -> e.getValue().contains("${"))
        .forEach(e -> e.setValue(dereference(e.getValue(), ctx)));
    if (pom.dependencies != null) {
      pom.dependencies.forEach(art -> resolveReferencesOf(art, ctx));
    }
    if (pom.dependencyManagement != null) {
      pom.dependencyManagement.forEach(art -> resolveReferencesOf(art, ctx));
    }
    if (log.isTraceEnabled()) {
      try {
        log.trace(om.writerWithDefaultPrettyPrinter().writeValueAsString(pom));
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
