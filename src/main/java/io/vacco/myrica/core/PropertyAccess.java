package io.vacco.myrica.core;

import java.util.*;
import java.util.regex.*;

public class PropertyAccess {

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
      property = property.replace(keyRef, propVal);
    }
    return removeVarTokens(property);
  }

  public static String resolveKeyReferences(String key, Map<String, String> raw) {
    String rawValue = raw.get(key);
    List<String> keyRefs = scanProperties(rawValue);
    if (keyRefs.isEmpty()) return rawValue == null ? "???" : rawValue;
    for (String keyRef : keyRefs) {
      String keyRefVal = resolveKeyReferences(keyRef, raw);
      rawValue = rawValue.replace(keyRef, keyRefVal);
    }
    return removeVarTokens(rawValue);
  }

  public static Map<String, String> resolveProperties(Map<String, String> raw, Map<String, String> resolved) {
    raw.keySet().forEach(key -> resolved.put(key, resolveKeyReferences(key, raw)));
    return resolved;
  }

}
