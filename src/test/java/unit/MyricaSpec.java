package unit;

import com.github.underscore.lodash.U;
import com.github.underscore.lodash.Xml;
import io.vacco.myrica.core.*;
import io.vacco.myrmica.maven.Coordinates;
import io.vacco.myrmica.maven.Repository;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.joox.Match;
import org.junit.runner.RunWith;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MyricaSpec {
  static {

    String M2 = "http://central.maven.org/maven2/";
    String localRepo = "/tmp/repo/";
    Repository repo = new Repository(localRepo, M2);

    it("Can build local/remote paths for Maven coordinates.", () -> {
      Coordinates c = new Coordinates("org.apache.spark", "spark-core_2.12", "2.4.0");
      URI remotePom = c.getPomUri(new URI(M2));
      Path localPom = c.getLocalPomPath(Paths.get(localRepo));
      assertEquals("http://central.maven.org/maven2/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", remotePom.toString());
      assertEquals("/tmp/repo/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", localPom.toString());
    });

    it("Can resolve properties from a POM definition for a module's coordinates.", () -> {
      Match pom = repo.buildPom(new Coordinates(
          // "com.fasterxml.jackson.core", "jackson-databind", "2.9.8"
          "org.apache.spark", "spark-core_2.12", "2.4.0"
          // "org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4"
      ));
      // Map<String, String> fullProps = repo.collectProperties(pom);
      // assertFalse(fullProps.isEmpty());
      System.out.println();
    });
/*

    it("Can resolve dependencies for a module's coordinates which also specify native dependencies.", () -> {
      Module opencvPlatform = repo.loadPom(new ModuleMetadata(
          "org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4"
      ));
      Collection<Module> all = repo.resolveDependencies(opencvPlatform);
      assertFalse(all.isEmpty());
    });
    it("Can resolve dependencies for a module's coordinates.", () -> {
      Module spark = repo.loadPom(new ModuleMetadata(
          "org.apache.spark", "spark-core_2.12", "2.4.0"));
      Collection<Module> all = repo.resolveDependencies(spark);
      assertFalse(all.isEmpty());
    });
    */
  }
}
