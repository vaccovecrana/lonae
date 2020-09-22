package io.vacco.myrmica.maven.util;

import io.vacco.myrmica.maven.schema.*;
import org.joox.Match;
import java.nio.file.Path;
import java.util.*;

import static io.vacco.myrmica.maven.schema.MmConstants.*;
import static java.util.stream.Collectors.*;

public class MmArtifacts {

  public static boolean isRuntimeClassPath(MmArtifact a) {
    MmComponent metadata = a.metadata;
    if (metadata.type == ArtifactHandler.TestJar) return false;
    if (metadata.classifier != null && metadata.classifier.contains(Scope.Test.label)) return false;
    if (a.optional) return false;
    if (Scope.Provided.equals(a.scope)) return false;
    boolean isRtScope = Scope.Compile.equals(a.scope) || Scope.Runtime.equals(a.scope);
    return metadata.addedToClasspath && isRtScope;
  }

  public static Set<MmArtifact> artifactsOf(Match xmlDepNode) {
    return xmlDepNode.children().each().stream()
        .map(MmArtifacts::fromXml).collect(toCollection(LinkedHashSet::new));
  }

  private static MmComponent metaOf(Match xml) {
    Optional<ArtifactHandler> oah = ArtifactHandler.fromLabel(xml.child(ComponentTag.type.label).text());
    if (!oah.isPresent()) {
      return MmComponents.forType(ArtifactHandler.Jar);
    } else {
      return MmComponents.forType(oah.get());
    }
  }

  public static MmArtifact fromXml(Match xml) {
    MmArtifact a = new MmArtifact();
    a.at = MmCoordinatesUt.fromXml(xml);
    a.optional = Boolean.parseBoolean(xml.child(PomTag.optional.toString()).text());
    a.exclusions.addAll(artifactsOf(xml.child(PomTag.exclusions.toString())));
    a.metadata = metaOf(xml);
    Optional<Scope> os = Scope.fromLabel(xml.child(PomTag.scope.toString()).text());
    a.scope = os.orElse(Scope.Compile);
    return a;
  }

  public static Path pomPathOf(MmCoordinates coordinates) {
    MmArtifact art = new MmArtifact();
    art.at = coordinates;
    art.metadata = MmComponents.forType(ArtifactHandler.Pom);
    art.optional = false;
    return MmCoordinatesUt.getResourcePath(coordinates).resolve(art.getBaseArtifactName());
  }
}
