package io.vacco.myrmica.util;

import org.slf4j.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class PropertyAccess {

  private static final Logger log = LoggerFactory.getLogger(PropertyAccess.class);
  private static final Pattern propertyRegex = Pattern.compile(".*?\\$\\{(.*?)\\}.*?");

  public static String removeVarTokens(String key) {
    if (key == null) {
      return null;
    }
    return key
        .replace("$", "")
        .replace("{", "")
        .replace("}", "");
  }

  public static List<String> scanProperties(String raw) {
    List<String> result = new ArrayList<>();
    if (raw == null) return result;
    Matcher matchPattern = propertyRegex.matcher(raw);
    while (matchPattern.find()) { result.add(matchPattern.group(1)); }
    return result;
  }

  public static String dereference(String property, Map<String, String> resolvedKeys) {
    for (String keyRef : scanProperties(property)) {
      String propVal = resolvedKeys.get(keyRef);
      if (propVal == null) {
        propVal = System.getProperty(keyRef);
        if (propVal == null) {
          if (log.isDebugEnabled()) { log.debug("Unresolved property usage: [{}]", property); }
          propVal = "?";
        }
      }
      property = property.replace(keyRef, propVal);
    }
    return removeVarTokens(property);
  }

  public static void resolvePomKeyReferences(Map<String, Object> pom, Map<String, String> resolvedKeys) {
    pom.keySet().forEach(k -> {
      Object o = pom.get(k);
      if (o instanceof String) {
        String keyVal = (String) pom.get(k);
        if (keyVal.contains("${")) {
          pom.put(k, dereference(keyVal, resolvedKeys));
        }
      } else if (o instanceof Map) {
        Map<String, Object> m0 = (Map<String, Object>) o;
        resolvePomKeyReferences(m0, resolvedKeys);
      } else if (o instanceof List) {
        pom.put(k, ((List) o).stream().map(it -> {
          if (it instanceof Map) {
            Map m0 = (Map) it;
            resolvePomKeyReferences(m0, resolvedKeys);
          }
          return it;
        }).collect(Collectors.toList()));
      }
    });
  }

  public static String resolveKeyReferences(String key, Map<String, String> raw) {
    String rawValue = raw.get(key);
    List<String> keyRefs = scanProperties(rawValue);
    if (keyRefs.isEmpty()) {
      if (rawValue == null) {
        rawValue = System.getProperty(key);
        if (rawValue == null) {
          if (log.isDebugEnabled()) { log.warn("Unresolved property: [{}]", key); }
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

  public static Map<String, String> resolveProperties(Map<String, String> raw) {
    Map<String, String> resolved = new TreeMap<>();
    raw.keySet().forEach(key -> resolved.put(key, resolveKeyReferences(key, raw)));
    return resolved;
  }

  public static Map<String, String> loadProperties(Map<String, Object> raw) {
    requireNonNull(raw);
    Map<String, String> result = new TreeMap<>();
    Map<String, Object> proj = (Map<String, Object>) raw.get("project");
    Map<String, Object> props = (Map<String, Object>) proj.get("properties");
    props.keySet().forEach(k -> {
      Object o = props.get(k);
      if (o instanceof String) result.put(k, (String) props.get(k));
      else result.put(k, "");
    });
    return result;
  }
}
