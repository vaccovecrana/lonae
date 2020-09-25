package io.vacco.myrmica.maven.xform;

import org.joox.Match;
import org.w3c.dom.*;

import java.util.*;
import java.util.function.Function;

import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.of;

public class MmXmlUtil {

  public static <K, V> Map.Entry<K, V> kv(K key, V value) { return new AbstractMap.SimpleEntry<>(key, value); }

  @SafeVarargs @SuppressWarnings({"varargs"})
  public static <K, V> Map<K, V> mapOf(Map.Entry<K, V> ... entries) {
    return synchronizedMap(of(entries)
        .filter(Objects::nonNull)
        .filter(e -> e.getValue() != null)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  public static void renameNamespaceRecursive(Node node, String namespace) {
    Document document = node.getOwnerDocument();
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      document.renameNode(node, namespace, node.getNodeName());
    }
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); ++i) {
      renameNamespaceRecursive(list.item(i), namespace);
    }
  }

  public static Map<String, Object> ofFlatNode(Match x, Function<Match, Object> xFormFn) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Match c : x.children().each()) {
      Object val;
      if (c.children().size() > 0 && xFormFn != null) {
        val = xFormFn.apply(c);
      } else {
        val = c.text();
      }
      out.put(c.tag(), val);
    }
    return out;
  }

  public static List<Map<String, Object>> ofNodeList(Match x, Function<Match, Object> xFormFn) {
    return x.children().each().stream()
        .map(c -> ofFlatNode(c, xFormFn)).collect(toList());
  }

  public static Map.Entry<String, Object> nodeRef(String key, String xPath, Match x, Function<Match, Object> xFormFn) {
    Match xp = x.xpath(xPath);
    if (xp.isEmpty()) return null;
    return kv(key, xFormFn.apply(xp));
  }
}
