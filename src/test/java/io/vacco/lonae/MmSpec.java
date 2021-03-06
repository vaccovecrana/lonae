package io.vacco.lonae;

import io.vacco.lonae.maven.impl.MmRepository;
import io.vacco.lonae.maven.schema.MmComponent;
import io.vacco.lonae.maven.schema.MmCoordinates;
import io.vacco.lonae.maven.schema.MmPom;
import io.vacco.lonae.maven.schema.MmResolutionResult;
import io.vacco.lonae.maven.xform.MmXform;
import io.vacco.oriax.core.*;
import io.vacco.oriax.util.OxTgf;
import io.vacco.shax.logging.ShOption;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import static j8spec.J8Spec.*;
import static io.vacco.shax.logging.ShArgument.*;
import static java.lang.String.*;

@DefinedOrder @RunWith(J8SpecRunner.class)
public class MmSpec {

  public static final String[] testCoords = new String[] {
      "jackson-databind:2.11.2", "org.springframework.boot:spring-boot-starter-web:2.3.4.RELEASE",
      "org.springframework:spring-core:5.2.9.RELEASE", "ch.qos.logback:logback-classic:1.2.3",
      "org.apache.spark:spark-core_2.12:2.4.0", "org.glassfish.jersey.core:jersey-client:2.22.2",
      "org.slf4j:slf4j-simple:1.7.30"
  };

  public static final String[] validationCoords = new String[] {
      "org.bytedeco:javacv:1.4.3",
      "org.slf4j:slf4j-api:1.7.30",
      "com.google.guava:guava:29.0-jre", "org.slf4j:slf4j-log4j12:1.7.30",
      "org.scala-lang:scala-library:2.13.2", "org.testng:testng:7.3.0",
      "mysql:mysql-connector-java:8.0.21", "com.h2database:h2:1.4.200",
      "org.apache.httpcomponents:httpclient:4.5.12", "com.google.code.gson:gson:2.8.6",
      "org.apache.arrow:arrow-jdbc:0.12.0", "io.atomix:atomix:3.1.5", "com.querydsl:querydsl-jpa:4.2.1",
      "com.fasterxml.jackson.module:jackson-module-scala_2.12:2.6.7.1",
      "com.google.api-client:google-api-client:1.28.0", "org.hibernate:hibernate-core:5.4.1.Final",
      "org.deeplearning4j:deeplearning4j-core:1.0.0-beta3"
  };

  public static final String[] comparisonCoords = new String [] {
      "com.sun.jersey:jersey-json:1.9",
      "org.apache.zookeeper:zookeeper:3.4.6",
      "org.apache.hadoop:hadoop-auth:2.6.5",
      "org.apache.hadoop:hadoop-common:2.6.5",
      "org.apache.hadoop:hadoop-mapreduce-client-shuffle:2.6.5",
      "org.apache.hadoop:hadoop-client:2.6.5",
      "org.apache.avro:avro:1.8.2",
      "org.apache.spark:spark-core_2.12:2.4.0",

      "org.deeplearning4j:deeplearning4j-core:1.0.0-beta3",

      "org.springframework.boot:spring-boot-starter-web:2.3.4.RELEASE"
  };

  static {
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, "false");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "info");

    boolean n =new File("/tmp", "repo").mkdirs();

    final Logger log = LoggerFactory.getLogger(MmSpec.class);
    final MmRepository repo = new MmRepository("/tmp/repo", "https://repo1.maven.org/maven2/");

    it("Can compare resolved dependency graphs against Gradle references", () -> {
      for (String cc : comparisonCoords) {
        String gradleFile = format("%s.deps", cc.replace(":", "^"));
        List<String> lines =  Files.readAllLines(Paths.get("./src/test/resources", gradleFile));
        OxGrph<String, String> gradleGraph = MmGradleGraph.process(lines);
        OxGrph<String, MmPom> mmGraph = repo.buildPomGraph(MmCoordinates.from(cc));
        log.info("==================================================");
        log.info(cc);
        log.info("--------------------------------------------------");
        int hit = 0, miss = 0, slack = 0;
        for (OxVtx<String, MmPom> v : mmGraph.vtx) {
          if (gradleGraph.vtx.contains(v)) {
            hit = hit + 1;
          } else {
            slack = slack + 1;
            log.info("slack: {}", v);
          }
        }
        for (OxVtx<String, String> v : gradleGraph.vtx) {
          if (!mmGraph.vtx.contains(v)) {
            miss = miss + 1;
            log.info("miss:  {}", v);
          }
        }
        log.info("--------------------------------------------------");
        log.info("hit: {}, miss: {}, slack: {}", hit, miss, slack);
        log.info("--------------------------------------------------");
        log.info(OxTgf.apply(mmGraph));
        log.info("==================================================\n");
      }
    });

    it("Can install resolved artifacts", () -> {
      List<MmResolutionResult> jarAndSources = repo.installFrom(MmCoordinates.from("org.apache.spark:spark-core_2.12:2.4.0"), "", "sources");
      log.info("{}", kv("jarAndSources", jarAndSources));
      jarAndSources.forEach(jar -> log.info(jar.toString()));
    });

    describe(MmXform.class.getCanonicalName(), () -> {
      it("can load component definitions", () -> {
        Map<MmComponent.Type, MmComponent> components = MmXform.forComponents(MmSpec.class.getResource("/io/vacco/lonae/maven/artifact-handlers.xml"));
        components.forEach((k, v) -> log.info(v.toString()));
        log.info("{}", kv("comps", components));
      });
      it("can compute effective POM data", () -> {
        Arrays.stream(validationCoords).map(MmCoordinates::from).forEach(coords -> {
          MmPom pom = repo.computePom(coords);
          log.info(pom.toString());
        });
      });
      it("can render POM dependency graphs", () -> {
        Arrays.stream(validationCoords).map(MmCoordinates::from).forEach(coord -> {
          log.info("---> {}", coord);
          OxGrph<String, MmPom> g = repo.buildPomGraph(coord);
          log.info(OxTgf.apply(g));
        });
      });
    });

  }
}
