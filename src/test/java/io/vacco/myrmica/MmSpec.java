package io.vacco.myrmica;

import io.vacco.myrmica.maven.impl.MmRepository;
import io.vacco.myrmica.maven.schema.*;
import io.vacco.myrmica.maven.xform.*;
import io.vacco.oriax.core.*;
import io.vacco.oriax.util.OxTgf;
import io.vacco.shax.logging.ShOption;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.nio.file.*;
import java.util.*;

import static j8spec.J8Spec.*;
import static io.vacco.shax.logging.ShArgument.*;
import static org.junit.Assert.*;
import static java.lang.String.*;

@DefinedOrder @RunWith(J8SpecRunner.class)
public class MmSpec {

  public static class Flop {
    public int foo;
    public String meep;
    public String[] cats;
    public static Flop of(int foo, String meep, String ... cats) {
      Flop f = new Flop();
      f.foo = foo;
      f.meep = meep;
      f.cats = cats;
      return f;
    }
  }

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
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, "true");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, "info");

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

    it("Can merge objects from right to left", () -> {
      Optional<Flop> f = new MmPatchLeft().onMultiple(
          Flop.of(1, "meep", "fido", "garfield"),
          Flop.of(2, "moop", "momo", "garfield"),
          Flop.of(42, null, "felix")
      );
      assertTrue(f.isPresent());
      log.info("{}", kv("merged", f.get()));
    });

    describe(MmXform.class.getCanonicalName(), () -> {
      it("can load component definitions", () -> {
        Map<MmComponent.Type, MmComponent> components = MmXform.forComponents(MmSpec.class.getResource("/io/vacco/myrmica/maven/artifact-handlers.xml"));
        components.forEach((k, v) -> log.info(v.toString()));
        log.info("{}", kv("comps", components));
      });
      it("can compute effective POM data", () -> {
        Arrays.stream(validationCoords).map(MmCoordinates::from).forEach(coord -> {
          MmPom o = repo.computePom(coord);
        });
      });
      it("can render POM dependency graphs", () -> {
        Arrays.stream(validationCoords).map(MmCoordinates::from).forEach(coord -> {
          OxGrph<String, MmPom> g = repo.buildPomGraph(coord);
          log.info(OxTgf.apply(g));
        });
      });
    });

  }
}
