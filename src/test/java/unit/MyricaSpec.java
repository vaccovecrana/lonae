package unit;

import io.vacco.myrica.core.Module;
import io.vacco.myrica.core.ModuleMetadata;
import io.vacco.myrica.core.Repository;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MyricaSpec {
  static {

    String M2 = "http://central.maven.org/maven2/";
    String localRepo = "/tmp/repo/";
    ModuleMetadata module = new ModuleMetadata("org.apache.spark", "spark-core_2.12", "2.4.0");
    Repository repo = new Repository(localRepo, M2);

    it("Can build local/remote paths for Maven coordinates.", () -> {
      URI remotePom = module.getPomUri(new URI(M2));
      Path localPom = module.getLocalPomPath(Paths.get(localRepo));
      assertEquals("http://central.maven.org/maven2/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", remotePom.toString());
      assertEquals("/tmp/repo/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", localPom.toString());
    });

    it("Can resolve properties from a POM definition for a module's coordinates.", () -> {
      Module pom = repo.loadPom(new ModuleMetadata(
          "com.fasterxml.jackson.core", "jackson-core", "2.9.6"));
      Map<String, String> fullProps = repo.collectProperties(pom);
      assertFalse(fullProps.isEmpty());
    });

    it("Can resolve dependencies for a module's coordinates.", () -> {
      Module spark = repo.loadPom(new ModuleMetadata(
         // "com.fasterxml.jackson.core", "jackson-databind", "2.9.6"));
      "org.apache.spark", "spark-core_2.12", "2.4.0"));
      Collection<Module> all = repo.resolveDependencies(spark).collect(Collectors.toList());
      System.out.println();
    });
  }
}
