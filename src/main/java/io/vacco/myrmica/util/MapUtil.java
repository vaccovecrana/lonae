package io.vacco.myrmica.util;

import java.util.*;
import java.util.stream.Collectors;

public class MapUtil {

  public static Map<String, Object> mapMerge(Map<String, Object> mapLeft, Map<String, Object> mapRight) {
    for (String key : mapRight.keySet()) {
      if (mapLeft.containsKey(key) && mapLeft.get(key) instanceof Map) {
        Object ol = mapLeft.get(key);
        Object or = mapRight.get(key);
        if (or instanceof List) {
          List lr = (List) or;
          lr.add(ol);
          mapLeft.put(key, lr);
        } else { mapMerge((Map) ol, (Map) or); }
      } else { mapLeft.put(key, mapRight.get(key)); }
    }
    return mapLeft;
  }

  private static void keyFilter(Map<String, Object> in, Map<String, Object> result,
                               Set<String> excludeKeyPrefixes) {
    for (String key : in.keySet()) {
      Optional<String> keyMatch = excludeKeyPrefixes.stream().filter(key::startsWith).findFirst();
      if (!keyMatch.isPresent()) {
        if (in.get(key) instanceof Map) {
          result.put(key, new TreeMap<>());
          Map m0 = (Map) in.get(key);
          Map m1 = (Map) result.get(key);
          keyFilter(m0, m1, excludeKeyPrefixes);
        } else if (in.get(key) instanceof List) {
          result.put(key, ((List) in.get(key)).stream().map(o -> {
            if (o instanceof Map) {
              Map m0 = new TreeMap();
              keyFilter((Map) o, m0, excludeKeyPrefixes);
              return m0;
            }
            return o;
          }).filter(o -> o instanceof Map ? !((TreeMap) o).isEmpty() : true).collect(Collectors.toList()));
        }
        else { result.put(key, in.get(key)); }
      }
    }
  }

  public static Map<String, Object> keyFilter(Map<String, Object> in, String ... excludeKeyPrefixes) {
    Map<String, Object> result = new TreeMap<>();
    keyFilter(in, result, Arrays.stream(excludeKeyPrefixes).collect(Collectors.toSet()));
    return result;
  }
}
