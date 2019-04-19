package unit;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import io.vacco.myrmica.maven.impl.NodeUtil;
import io.vacco.myrmica.maven.impl.Repository;
import io.vacco.myrmica.maven.impl.ResolutionResult;
import io.vacco.myrmica.maven.schema.Artifact;
import io.vacco.myrmica.maven.schema.Coordinates;
import io.vacco.myrmica.maven.schema.Pom;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.joox.Match;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static j8spec.J8Spec.*;
import static org.joox.JOOX.*;
import static org.junit.Assert.*;
import static io.vacco.myrmica.maven.impl.PropertyAccess.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MyrmicaSpec {
  static {

    Logger log = LoggerFactory.getLogger(MyrmicaSpec.class);

    String M2 = "https://repo.maven.apache.org/maven2/";
    String M2b4d = "https://repo.maven.apache.org/maven2";
    String localRepo = "/tmp/repo/";
    String badRepo = "/tmp/b4dr3p0";
    Repository repo = new Repository(localRepo, M2);

    Coordinates atomix = Coordinates.from("io.atomix", "atomix", "3.1.5");
    Coordinates arrowJdbc = Coordinates.from("org.apache.arrow", "arrow-jdbc", "0.12.0");
    Coordinates googleApiClient = Coordinates.from("com.google.api-client", "google-api-client", "1.28.0");
    Coordinates hibernateCore = Coordinates.from("org.hibernate", "hibernate-core", "5.4.1.Final");
    Coordinates queryDslJpa = Coordinates.from("com.querydsl", "querydsl-jpa", "4.2.1");
    Coordinates spark = Coordinates.from("org.apache.spark", "spark-core_2.12", "2.4.0");
    Coordinates spring = Coordinates.from("org.springframework.boot", "spring-boot-starter-web", "2.1.2.RELEASE");
    Coordinates opencv = Coordinates.from("org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4");
    Coordinates dl4j = Coordinates.from("org.deeplearning4j", "deeplearning4j-core", "1.0.0-beta3");
    Coordinates keyCloakAdapterSpi = Coordinates.from("org.keycloak", "keycloak-adapter-spi", "6.0.0");

    it("Resolves a null key property to a null value.", () -> assertNull(removeVarTokens(null)));
    it("Can merge two XML documents.", () -> {
      Match logbackParent = $(MyrmicaSpec.class.getResourceAsStream("/logback-parent.pom"));
      Match logbackClassic = $(MyrmicaSpec.class.getResourceAsStream("/logback-classic.pom"));
      Match merged = NodeUtil.merge(logbackParent, logbackClassic);
      log.info(merged.toString());
    });
    it("Can build local/remote paths for Maven coordinates.", () -> {
      URI remotePom = spark.getPomUri(new URI(M2));
      Path localPom = spark.getLocalPomPath(Paths.get(localRepo));
      assertEquals("https://repo.maven.apache.org/maven2/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", remotePom.toString());
      assertEquals("/tmp/repo/org/apache/spark/spark-core_2.12/2.4.0/spark-core_2.12-2.4.0.pom", localPom.toString());
    });
    it("Cannot initialize a repository on an invalid system path.",
        c -> c.expected(IllegalArgumentException.class), () -> new Repository(badRepo, M2));
    it("Cannot initialize a repository on an invalid remote path",
        c -> c.expected(IllegalArgumentException.class), () -> new Repository(localRepo, M2b4d));
    it("Can resolve dependencies from a POM definition for a module's coordinates.", () -> {
      Pom pom = repo.buildPom(spring);
      Path p = Paths.get(localRepo);
      log.info(p.toString());
      pom.getDependencies().forEach(d -> log.info(d.getLocalPackagePath(p).toString()));
    });
    it("Can resolve the dependency hierarchy of a module's coordinates.", () -> {
      ResolutionResult rtDeps = repo.loadRuntimeArtifactsAt(spark);
      assertFalse(rtDeps.artifacts.isEmpty());
      log.info(rtDeps.printTree());
    });
    it("Can resolve dependencies for a module's coordinates which also specify native dependencies.", () -> {
      ResolutionResult openCvArt = repo.loadRuntimeArtifactsAt(opencv);
      assertFalse(openCvArt.artifacts.isEmpty());
    });
    it("Can install dependencies for a single artifact.", () -> {
      Map<Artifact, Path> dependencies = repo.installRuntimeArtifactsAt(keyCloakAdapterSpi);
      assertFalse(dependencies.isEmpty());
    });
    it("Can serialize/deserialize schema objects.", () -> {
      Map<Artifact, Path> dependencies = repo.installRuntimeArtifactsAt(keyCloakAdapterSpi);
      Artifact a0 = dependencies.keySet().iterator().next();
      String path = "/tmp/data.yml";
      YamlWriter w = new YamlWriter(new FileWriter(path));
      w.write(a0);
      w.close();
      YamlReader r = new YamlReader(new FileReader(path));
      Artifact a1 = r.read(Artifact.class);
      log.info(a1.toString());
      assertNotNull(a1);
      assertEquals(a0, a1);
    });
    it("Can install target runtime artifacts for large frameworks.", () -> {
      ResolutionStats rsAtomix = ResolutionStats.installAndMatch(repo, atomix, "/io.atomix^atomix^3.1.5.grdl");
      ResolutionStats rsQueryDslJpa = ResolutionStats.installAndMatch(repo, queryDslJpa, "/com.querydsl^querydsl-jpa^4.2.1.grdl");
      ResolutionStats rsArrowJdbc = ResolutionStats.installAndMatch(repo, arrowJdbc, "/org.apache.arrow^arrow-jdbc^0.12.0.grdl");
      ResolutionStats rsGoogleApiClient = ResolutionStats.installAndMatch(repo, googleApiClient, "/com.google.api-client^google-api-client^1.28.0.grdl");
      ResolutionStats rsHibernateCore = ResolutionStats.installAndMatch(repo, hibernateCore, "/org.hibernate^hibernate-core^5.4.1.Final.grdl");
      ResolutionStats rsSpring = ResolutionStats.installAndMatch(repo, spring, "/org.springframework.boot^spring-boot-starter-web^2.1.2.RELEASE.grdl");
      ResolutionStats rsSpark = ResolutionStats.installAndMatch(repo, spark, "/org.apache.spark^spark-core_2.12^2.4.0.grdl");
      ResolutionStats rsDl4j = ResolutionStats.installAndMatch(repo, dl4j, "/org.deeplearning4j^deeplearning4j-core^1.0.0-beta3.grdl");

      log.info(rsAtomix.resolutionResult.printTree());
      log.info(rsQueryDslJpa.resolutionResult.printTree());
      log.info(rsArrowJdbc.resolutionResult.printTree());
      log.info(rsGoogleApiClient.resolutionResult.printTree());
      log.info(rsHibernateCore.resolutionResult.printTree());
      log.info(rsSpring.resolutionResult.printTree());
      log.info(rsSpark.resolutionResult.printTree());
      log.info(rsDl4j.resolutionResult.printTree());

      log.info(rsAtomix.toString());
      log.info(rsQueryDslJpa.toString());
      log.info(rsArrowJdbc.toString());
      log.info(rsGoogleApiClient.toString());
      log.info(rsHibernateCore.toString());
      log.info(rsSpring.toString());
      log.info(rsSpark.toString());
      log.info(rsDl4j.toString());
      log.info("Done...");
    });
  }
}
