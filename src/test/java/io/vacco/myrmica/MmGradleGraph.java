package io.vacco.myrmica;

import io.vacco.oriax.core.*;
import java.util.*;
import java.util.stream.Collectors;

public class MmGradleGraph {

  private static final String depRegex = "((\\+\\-\\-\\- )|(\\|    )|(\\\\\\-\\-\\- )|(     ))";

  public static OxVtx<String, String> fromRaw(String[] data) {
    String coordsStr = data[data.length - 1].trim();
    String[] coords = coordsStr.split(":");
    String groupId = coords[0].trim();
    String artId = coords[1].replace(" (*)", "");
    if (artId.contains("->")) {
      artId = artId.split("->")[0].trim();
    }
    String nodeId = String.format("%s:%s", groupId, artId);
    return new OxVtx<>(nodeId, coordsStr).withLabel(artId);
  }

  public static void processDeps(OxGrph<String, String> g, OxVtx<String, String> root, String data) {
    try {
      List<String[]> deps = Arrays.stream(data.split("\n"))
          .map(s -> s.split(depRegex))
          .filter(sArr -> sArr.length > 1)
          .collect(Collectors.toList());
      Map<Integer, OxVtx<String, String>> lParents = new HashMap<>();
      lParents.put(1, root);
      for (String[] curr : deps) {
        OxVtx<String, String> cv = fromRaw(curr);
        lParents.put(curr.length, cv);
        OxVtx<String, String> pv = lParents.get(curr.length - 1);
        if (pv != null) {
          g.addEdge(pv, cv);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static OxGrph<String, String> parse(String ... gradleDepSources) {
    OxGrph<String, String> g = new OxGrph<>();
    String rootLabel = UUID.randomUUID().toString();
    String rootId = String.format("io.gopher.root:%s", rootLabel);

    for (String s : gradleDepSources) {
      processDeps(g, new OxVtx<>(rootId, rootId).withLabel(rootLabel), s);
    }
    return g;
  }

}
