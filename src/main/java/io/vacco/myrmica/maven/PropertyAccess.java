package io.vacco.myrmica.maven;

import org.joox.Match;
import org.slf4j.*;
import java.util.*;
import java.util.regex.*;

import static java.util.Objects.requireNonNull;
import static io.vacco.myrmica.maven.Constants.*;

class PropertyAccess {

  private static final Logger log = LoggerFactory.getLogger(PropertyAccess.class);
  private static final Pattern propertyRegex = Pattern.compile(".*?\\$\\{(.*?)\\}.*?");

  private static String removeVarTokens(String key) {
    if (key == null) {
      return null;
    }
    return key
        .replace("$", "")
        .replace("{", "")
        .replace("}", "");
  }

  private static List<String> scanProperties(String raw) {
    List<String> result = new ArrayList<>();
    if (raw == null) return result;
    Matcher matchPattern = propertyRegex.matcher(raw);
    while (matchPattern.find()) { result.add(matchPattern.group(1)); }
    return result;
  }

  private static String dereference(String property, Map<String, String> resolvedKeys) {
    for (String keyRef : scanProperties(property)) {
      String propVal = resolvedKeys.get(keyRef);
      if (propVal == null) {
        propVal = System.getProperty(keyRef);
        if (propVal == null) {
          if (log.isTraceEnabled()) { log.trace("Unresolved property usage: [{}]", property); }
          propVal = "?";
        }
      }
      property = property.replace(keyRef, propVal);
    }
    return removeVarTokens(property);
  }

  static void resolvePomKeyReferences(Match root, Map<String, String> resolvedKeys) {
    if (!NodeUtil.isTextContent(root)) {
      String keyVal = root.text();
      if (keyVal.contains("${")) {
        root.text(dereference(keyVal, resolvedKeys));
      }
    } else { root.children().each().forEach(c -> resolvePomKeyReferences(c, resolvedKeys)); }
  }

  private static String resolveKeyReferences(String key, Map<String, String> raw) {
    String rawValue = raw.get(key);
    List<String> keyRefs = scanProperties(rawValue);
    if (keyRefs.isEmpty()) {
      if (rawValue == null) {
        rawValue = System.getProperty(key);
        if (rawValue == null) {
          if (log.isDebugEnabled()) { log.debug("Unresolved property: [{}]", key); }
          return "?";
        }
      }
      return rawValue;
    }
    for (String keyRef : keyRefs) {
      String keyRefVal = resolveKeyReferences(keyRef, raw);
      rawValue = rawValue.replace(keyRef, keyRefVal);
    }
    return removeVarTokens(rawValue);
  }

  static Map<String, String> resolveProperties(Map<String, String> raw) {
    Map<String, String> resolved = new TreeMap<>();
    raw.keySet().forEach(key -> resolved.put(key, resolveKeyReferences(key, raw)));
    return resolved;
  }

  static Map<String, String> loadProperties(Match rootPom) {
    requireNonNull(rootPom);
    Map<String, String> result = new TreeMap<>();
    for (Match prop : rootPom.child(PomTag.properties.toString()).children().each()) {
      result.put(prop.tag(), prop.text());
    }
    return result;
  }
}
