package io.vacco.myrmica.maven.util;

import io.vacco.myrmica.maven.schema.*;
import org.joox.Match;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.joox.JOOX.*;

public class MmComponents {

  public static final Collection<MmComponent> defaultComponents = new ArrayList<>();

  static {
    try {
      URL handlers = MmComponent.class.getResource("/io/vacco/myrmica/maven/artifact-handlers.xml");
      Match xml = $(handlers);
      defaultComponents.addAll(
          xml.find(MmConstants.ComponentTag.component.label).each().stream()
              .map(MmComponents::fromXml).collect(Collectors.toList())
      );
    } catch (Exception e) {
      throw new IllegalStateException("Cannot resolve default component artifact handlers.");
    }
  }

  public static MmComponent forType(MmConstants.ArtifactHandler t) {
    return defaultComponents.stream()
        .filter(cmp -> cmp.type.equals(t)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException(t.label));
  }

  public static MmComponent fromXml(Match component) {
    Match conf = component.child(MmConstants.ComponentTag.configuration.label);
    MmComponent c = new MmComponent();
    MmConstants.ArtifactHandler.fromLabel(conf.child(MmConstants.ComponentTag.type.label).text()).ifPresent(ah -> c.type = ah);
    c.roleHint = component.child(MmConstants.ComponentTag.roleHint.label).text();
    c.extension = conf.child(MmConstants.ComponentTag.extension.label).text();
    c.packaging = conf.child(MmConstants.ComponentTag.packaging.label).text();
    c.classifier = conf.child(MmConstants.ComponentTag.classifier.label).text();
    c.language = conf.child(MmConstants.ComponentTag.language.label).text();
    c.addedToClasspath = Boolean.parseBoolean(conf.child(MmConstants.ComponentTag.addedToClasspath.label).text());
    c.includesDependencies = Boolean.parseBoolean(conf.child(MmConstants.ComponentTag.includesDependencies.label).text());
    if (c.extension == null) c.extension = c.type.label;
    if (c.packaging == null) c.packaging = c.type.label;
    return c;
  }
}
