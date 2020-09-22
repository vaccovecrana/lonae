package io.vacco.myrmica.maven.util;

import org.joox.Match;
import org.slf4j.*;
import java.util.*;
import java.util.regex.*;

import static io.vacco.myrmica.maven.schema.MmConstants.*;

public class MmProperties {

  private static final Logger log = LoggerFactory.getLogger(MmProperties.class);
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

  private static List<String> scanProperties(String raw) {
    List<String> result = new ArrayList<>();
    if (raw == null) return result;
    Matcher matchPattern = propertyRegex.matcher(raw);
    while (matchPattern.find()) { result.add(matchPattern.group(1)); }
    return result;
  }

  private static String dereference(String property, MmVarContext ctx) {
    for (String keyRef : scanProperties(property)) {
      Object rawVal = ctx.get(keyRef);
      String propVal = rawVal == null ? "?" : rawVal.toString();
      if (rawVal == null && log.isTraceEnabled()) {
        log.trace("Unresolved property usage: [{}]", property);
      }
      property = property.replace(keyRef, propVal);
    }
    return removeVarTokens(property);
  }

  public static void resolveVarReferences(Match root, MmVarContext ctx) {
    if (MmXml.isTextContent(root)) {
      String keyVal = root.text();
      if (keyVal.contains("${")) {
        root.text(dereference(keyVal, ctx));
      }
    } else { root.children().each().forEach(c -> resolveVarReferences(c, ctx)); }
  }

  public static void loadProperties(Match rootPom, MmVarContext ctx) {
    ctx.push();
    for (Match prop : rootPom.child(PomTag.properties.toString()).children().each()) {
      ctx.set(prop.tag(), prop.text()); // TODO maybe do resolution here...
    }
  }
}
