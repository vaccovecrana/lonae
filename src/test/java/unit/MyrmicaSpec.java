package unit;

import io.vacco.myrmica.maven.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.joox.Match;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static j8spec.J8Spec.*;
import static org.joox.JOOX.*;
import static org.junit.Assert.*;
import static io.vacco.myrmica.maven.PropertyAccess.*;

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

    Coordinates atomix = new Coordinates("io.atomix", "atomix", "3.1.5");
    Coordinates arrowJdbc = new Coordinates("org.apache.arrow", "arrow-jdbc", "0.12.0");
    Coordinates googleApiClient = new Coordinates("com.google.api-client", "google-api-client", "1.28.0");
    Coordinates hibernateCore = new Coordinates("org.hibernate", "hibernate-core", "5.4.1.Final");
    Coordinates queryDslJpa = new Coordinates("com.querydsl", "querydsl-jpa", "4.2.1");
    Coordinates spark = new Coordinates("org.apache.spark", "spark-core_2.12", "2.4.0");
    Coordinates spring = new Coordinates("org.springframework.boot", "spring-boot-starter-web", "2.1.2.RELEASE");
    Coordinates opencv = new Coordinates("org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4");
    Coordinates dl4j = new Coordinates("org.deeplearning4j", "deeplearning4j-core", "1.0.0-beta3");
    Coordinates keyCloakAdapterSpi = new Coordinates("org.keycloak", "keycloak-adapter-spi", "6.0.0");

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
