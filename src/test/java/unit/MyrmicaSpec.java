package unit;

import io.vacco.myrmica.maven.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MyrmicaSpec {
  static {

    Logger log = LoggerFactory.getLogger(MyrmicaSpec.class);

    String M2 = "http://central.maven.org/maven2/";
    String localRepo = "/tmp/repo/";
    Repository repo = new Repository(localRepo, M2);

    Coordinates spark = new Coordinates("org.apache.spark", "spark-core_2.12", "2.4.0");
    Coordinates spring = new Coordinates("org.springframework.boot", "spring-boot-starter-web", "2.1.2.RELEASE");
    Coordinates opencv = new Coordinates("org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4");

    it("Can build local/remote paths for Maven coordinates.", () -> {
      URI remotePom = spark.getPomUri(new URI(M2));
      Path localPom = spark.getLocalPomPath(Paths.get(localRepo));
      assertEquals("http://central.maven.org/maven2/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", remotePom.toString());
      assertEquals("/tmp/repo/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", localPom.toString());
    });
    it("Can resolve properties from a POM definition for a module's coordinates.", () -> {
      Pom pom = repo.buildPom(spring);
      Path p = Paths.get(localRepo);
      pom.getDependencies(true)
          .forEach(d -> log.info(d.getLocalPackagePath(p).toString()));
    });
    it("Can resolve the dependency hierarchy of a module's coordinates.", () -> {
      Set<Artifact> rtDeps = repo.loadRuntimeArtifactsAt(spark);
      assertFalse(rtDeps.isEmpty());
    });
    it("Can resolve dependencies for a module's coordinates which also specify native dependencies.", () -> {
      Set<Artifact> openCvArt = repo.loadRuntimeArtifactsAt(opencv);
      assertFalse(openCvArt.isEmpty());
    });
  }
}
