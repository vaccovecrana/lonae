package io.vacco.myrmica.maven.util;

import io.vacco.myrmica.maven.impl.MmException;
import io.vacco.myrmica.maven.schema.*;
import org.joox.Match;
import org.slf4j.*;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static io.vacco.myrmica.maven.schema.MmConstants.*;
import static io.vacco.myrmica.maven.util.MmProperties.*;
import static org.joox.JOOX.$;

public class MmPoms {

  private static final Logger log = LoggerFactory.getLogger(MmPoms.class);

  public static Optional<MmCoordinates> parentCoordsOf(Match pom) {
    Match p = pom.child(MmConstants.PomTag.parent.toString());
    if (p.size() == 0) return Optional.empty();
    return Optional.of(MmCoordinatesUt.fromXml(p));
  }

  public static Match loadPom(MmCoordinates c, Path localOrigin, URI remoteOrigin) {
    try {
      Path pomPath = MmArtifacts.pomPathOf(c);
      Path target = localOrigin.resolve(pomPath);
      if (!target.toFile().getParentFile().exists()) { target.toFile().getParentFile().mkdirs(); }
      if (!target.toFile().exists()) {
        URI remotePom = remoteOrigin.resolve(pomPath.toString());
        log.info("Fetching [{}]", remotePom);
        Files.copy(remotePom.toURL().openStream(), target);
      } else { log.info("Resolving [{}]", target); }
      return MmXml.filterTop($(target.toFile()), MmConstants.PomTag.exclusionTags());
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static Match computePom(MmCoordinates coordinates, Path localOrigin, URI remoteOrigin) {
    try {
      List<Match> poms = new ArrayList<>();
      List<Match> pomsRev;
      Optional<MmCoordinates> oc = Optional.of(coordinates);

      while (oc.isPresent()) {
        Match pp = loadPom(oc.get(), localOrigin, remoteOrigin);
        poms.add(pp);
        oc = parentCoordsOf(pp);
      }

      pomsRev = new ArrayList<>(poms);
      Collections.reverse(pomsRev);
      Optional<MmCoordinates> parentCoords = parentCoordsOf(poms.get(0));
      String rootPackaging = poms.get(0).child(MmConstants.ComponentTag.packaging.toString()).text();

      MmVarContext varCtx = new MmVarContext();

      System.getProperties().forEach((k, v) -> varCtx.set(k.toString(), v));
      varCtx.push();
      varCtx.set("project.build.directory", new File(".").getAbsolutePath());
      varCtx.set("project.groupId", coordinates.groupId);
      varCtx.set("project.artifactId", coordinates.artifactId);
      varCtx.set("project.version", coordinates.version);

      if (parentCoords.isPresent()) {
        varCtx.set("project.parent.groupId", parentCoords.get().groupId);
        varCtx.set("project.parent.artifactId", parentCoords.get().artifactId);
        varCtx.set("project.parent.version", parentCoords.get().version);
      }

      pomsRev.forEach(pom -> loadProperties(pom, varCtx));
      poms.forEach(pom -> resolveVarReferences(pom, varCtx));

      MmVarContext depCtx = new MmVarContext();
      pomsRev.forEach(pom -> {
        pom.child(PomTag.dependencyManagement.name())
            .child(PomTag.dependencies.name())
            .children(PomTag.dependency.name()).each().forEach(dep -> {
          MmCoordinates c = MmCoordinatesUt.fromXml(dep);
          depCtx.set(c.getArtifactFormat(), c);
        });
        depCtx.push();
      });
      poms.forEach(pom -> pom.child(PomTag.dependencies.name())
          .children(PomTag.dependency.name()).each().forEach(dep -> {
            MmCoordinates c = MmCoordinatesUt.fromXml(dep);
            if (c.version == null) {
              MmCoordinates cDepMgmt = (MmCoordinates) depCtx.get(c.getArtifactFormat());
              dep.append($(PomTag.version.name(), cDepMgmt.version));
            }
          }));

      // TODO does it make sense to implement import scope dependencies?

      Optional<Match> ePom = pomsRev.stream().reduce(MmXml::merge);
      if (ePom.isPresent()) {
        if (rootPackaging != null) {
          ePom.get().child(MmConstants.ComponentTag.packaging.toString()).text(rootPackaging);
        }
        return ePom.get();
      } else {
        throw new IllegalStateException("Unable to resolve POM data");
      }
    } catch (Exception e) {
      throw new MmException.MmPomResolutionException(coordinates, e);
    }
  }
}
