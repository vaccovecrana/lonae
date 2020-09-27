package io.vacco.lonae.maven.xform;

import com.fasterxml.jackson.databind.*;
import io.vacco.lonae.maven.impl.MmJsonLog;
import io.vacco.lonae.maven.schema.*;
import org.joox.Match;
import org.slf4j.*;

import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static io.vacco.lonae.maven.xform.MmXmlUtil.*;
import static java.util.stream.Collectors.*;
import static org.joox.JOOX.*;

public class MmXform {

  private static final Logger log = LoggerFactory.getLogger(MmXform.class);
  private static final ObjectMapper om = new ObjectMapper();

  static {
    om.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
  }

  public static String
      at = "at", parent = "parent",
      groupId = "groupId", artifactId = "artifactId", version = "version",
      dependencyManagement = "dependencyManagement", dependencies = "dependencies",
      properties = "properties";

  public static MmPom forPom(URL src) {
    try {
      Match x = $(src);
      renameNamespaceRecursive(x.document(), "");
      Map<String, Object> pom = mapOf(
          nodeRef(parent, "/project/parent", x, p -> ofFlatNode(p, null)),
          kv(at, mapOf(
              kv(groupId, x.xpath("/project/groupId").text()),
              kv(artifactId, x.xpath("/project/artifactId").text()),
              kv(version, x.xpath("/project/version").text())
          )),
          nodeRef(properties, "/project/properties", x, pr -> ofFlatNode(pr, null)),
          nodeRef(dependencyManagement, "/project/dependencyManagement/dependencies", x,
              dl -> ofNodeList(dl, exl -> ofNodeList(exl, exList ->
                  ofNodeList(exList, null)))
          ),
          nodeRef(dependencies, "/project/dependencies", x, depList ->
              ofNodeList(depList, exList -> ofNodeList(exList, null)))
      );
      if (log.isTraceEnabled()) {
        log.trace(MmJsonLog.jsonLogOf(pom));
      }
      return om.convertValue(pom, MmPom.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static Map<MmComponent.Type, MmComponent> forComponents(URL src) {
    try {
      Match x = $(src);
      return x.xpath("//configuration").each().stream()
          .map(cfg -> ofFlatNode(cfg, null))
          .map(m -> om.convertValue(m, MmComponent.class))
          .collect(toMap(cmp -> cmp.type, Function.identity()));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
