package io.vacco.myrmica;

import io.vacco.myrmica.maven.impl.MmRepository;
import io.vacco.myrmica.maven.schema.*;
import io.vacco.myrmica.maven.util.*;
import io.vacco.oriax.core.OxGrph;
import io.vacco.oriax.util.OxTgf;
import io.vacco.shax.logging.ShOption;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.joox.Match;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static j8spec.J8Spec.*;
import static io.vacco.shax.logging.ShArgument.*;
import static org.joox.JOOX.*;

@DefinedOrder @RunWith(J8SpecRunner.class)
public class MmSpec {
  static {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "debug");

    final Logger log = LoggerFactory.getLogger(MmSpec.class);
    final MmCoordinates jacksonDatabind = MmCoordinates.from("com.fasterxml.jackson.core:jackson-databind:2.11.2");
    final MmCoordinates springBootStarterWeb = MmCoordinates.from("org.springframework.boot:spring-boot-starter-web:2.3.4.RELEASE");
    final MmCoordinates springCore = MmCoordinates.from("org.springframework:spring-core:5.2.9.RELEASE");
    final MmCoordinates logbackClassic = MmCoordinates.from("ch.qos.logback:logback-classic:1.2.3");
    final MmCoordinates sparkCore = MmCoordinates.from("org.apache.spark:spark-core_2.12:2.4.0");

    describe(MmComponent.class.getCanonicalName(), () -> {
      it("can load default component definitions", () -> {
        Collection<MmComponent> defaults = MmComponents.defaultComponents;
        log.info("{}", kv("defaultComponents", defaults));
      });
    });

    describe(MmCoordinates.class.getCanonicalName(), () -> it("can parse artifact coordinates",  () -> log.info(springCore.toString())));

    describe(MmArtifact.class.getCanonicalName(), () -> {
      it("can load POM artifact dependencies", () -> {
        URL logbackClassicPom = MmSpec.class.getResource("/logback-classic.pom");
        Match pom = $(logbackClassicPom);
        List<Match> deps = pom.child(MmConstants.PomTag.dependencies.name())
            .children(MmConstants.PomTag.dependency.name()).each();
        List<MmArtifact> artifacts = deps.stream().map(MmArtifacts::fromXml).collect(Collectors.toList());
        for (MmArtifact art : artifacts) {
          log.info(art.toString());
        }
        log.info("====================");
        artifacts.stream().filter(MmArtifacts::isRuntimeClassPath).forEach(d -> log.info("{}", kv("rtDep", d)));
      });
    });

    describe(MmRepository.class.getCanonicalName(), () -> {
      it("can generate path location data for POM artifacts", () -> {
        Path pomRelativePath = MmArtifacts.pomPathOf(springCore);
        log.info(pomRelativePath.toString());
      });
      it("can load sanitized POM data", () -> {
        MmRepository repo = new MmRepository("/tmp/repo", "https://repo1.maven.org/maven2/");
        Match pomXml = MmPoms.computePom(jacksonDatabind, repo.localRoot, repo.remoteRoot);
        log.info(pomXml.toString());
      });
      it("can build a POM dependency graph", () -> {
        MmRepository repo = new MmRepository("/tmp/repo", "https://repo1.maven.org/maven2/");
        OxGrph<String, MmPom> g = repo.buildPomGraph(springBootStarterWeb);
        log.info(OxTgf.apply(g));
      });
    });

    it("Can parse Gradle dependency output trees", () -> {
      OxGrph<String, String> g = MmGradleGraph.parse(
          Stream.of("/org.deeplearning4j^deeplearning4j-core^1.0.0-beta3.deps")
              .map(MmSpec.class::getResource).map(MmUtil::load).toArray(String[]::new)
      );
      System.out.println(OxTgf.apply(g));
    });
  }
}
