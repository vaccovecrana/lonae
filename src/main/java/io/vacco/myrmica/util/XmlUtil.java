package io.vacco.myrmica.util;

import java.util.List;
import java.util.Map;

public class XmlUtil {

  public static Map<String, Object> mapMerge(Map<String, Object> mapLeft, Map<String, Object> mapRight) {
    for (String key : mapRight.keySet()) {
      if (mapLeft.containsKey(key) && mapLeft.get(key) instanceof Map) {
        Object ol = mapLeft.get(key);
        Object or = mapRight.get(key);
        if (or instanceof List) { // TODO omit merging of dependencies section.
          List lr = (List) or;
          lr.add(ol);
          mapLeft.put(key, lr);
        } else { mapMerge((Map) ol, (Map) or); }

      } else {
        mapLeft.put(key, mapRight.get(key));
      }
    }
    return mapLeft;
  }

}
