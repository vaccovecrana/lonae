package io.vacco.myrmica;

import io.vacco.myrmica.maven.impl.MmRepository;
import io.vacco.myrmica.maven.schema.*;
import io.vacco.myrmica.maven.xform.MmPatchLeft;
import io.vacco.myrmica.maven.xform.MmXform;
import io.vacco.shax.logging.ShOption;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.util.*;

import static j8spec.J8Spec.*;
import static io.vacco.shax.logging.ShArgument.*;
import static org.junit.Assert.*;

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
    final MmCoordinates jerseyClient = MmCoordinates.from("org.glassfish.jersey.core:jersey-client:2.22.2");
    final MmCoordinates slf4jSimple = MmCoordinates.from("org.slf4j:slf4j-simple:1.7.30");

    final MmCoordinates[] testCoords = new MmCoordinates[] {
      jacksonDatabind, springBootStarterWeb, springCore, logbackClassic, sparkCore, jerseyClient, slf4jSimple
    };

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
        log.info("{}", kv("comps", components));
      });
      it("can load raw POM data", () -> {
        MmRepository repo = new MmRepository("/tmp/repo", "https://repo1.maven.org/maven2/");
        for (MmCoordinates testCoord : testCoords) {
          MmPom o = repo.computePom(testCoord);
        }
      });
    });
  }
}
