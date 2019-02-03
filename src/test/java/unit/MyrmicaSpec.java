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
import java.util.Set;

import static j8spec.J8Spec.*;
import static org.joox.JOOX.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class MyrmicaSpec {
  static {

    Logger log = LoggerFactory.getLogger(MyrmicaSpec.class);

    String M2 = "https://repo.maven.apache.org/maven2/";
    String localRepo = "/tmp/repo/";
    Repository repo = new Repository(localRepo, M2);

    Coordinates arrowJdbc = new Coordinates("org.apache.arrow", "arrow-jdbc", "0.12.0");
    Coordinates googleApiClient = new Coordinates("com.google.api-client", "google-api-client", "1.28.0");
    Coordinates hibernateCore = new Coordinates("org.hibernate", "hibernate-core", "5.4.1.Final");
    Coordinates queryDslJpa = new Coordinates("com.querydsl", "querydsl-jpa", "4.2.1");
    Coordinates spark = new Coordinates("org.apache.spark", "spark-core_2.12", "2.4.0");
    Coordinates spring = new Coordinates("org.springframework.boot", "spring-boot-starter-web", "2.1.2.RELEASE");
    Coordinates opencv = new Coordinates("org.bytedeco.javacpp-presets", "opencv-platform", "4.0.1-1.4.4");
    Coordinates dl4j = new Coordinates("org.deeplearning4j", "deeplearning4j-core", "1.0.0-beta3");

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
    it("Can resolve dependencies from a POM definition for a module's coordinates.", () -> {
      Pom pom = repo.buildPom(spring);
      Path p = Paths.get(localRepo);
      pom.getDependencies().forEach(d -> log.info(d.getLocalPackagePath(p).toString()));
    });
    it("Can resolve the dependency hierarchy of a module's coordinates.", () -> {
      Set<Artifact> rtDeps = repo.loadRuntimeArtifactsAt(spark);
      assertFalse(rtDeps.isEmpty());
    });
    it("Can resolve dependencies for a module's coordinates which also specify native dependencies.", () -> {
      Set<Artifact> openCvArt = repo.loadRuntimeArtifactsAt(opencv);
      assertFalse(openCvArt.isEmpty());
    });
    it("Can install target runtime artifacts for large frameworks.", () -> {

      ResolutionStats rsQueryDslJpa = ResolutionStats.installAndMatch(repo, queryDslJpa, "/com.querydsl^querydsl-jpa^4.2.1.grdl");
      ResolutionStats rsArrowJdbc = ResolutionStats.installAndMatch(repo, arrowJdbc, "/org.apache.arrow^arrow-jdbc^0.12.0.grdl");
      ResolutionStats rsGoogleApiClient = ResolutionStats.installAndMatch(repo, googleApiClient, "/com.google.api-client^google-api-client^1.28.0.grdl");
      ResolutionStats rsHibernateCore = ResolutionStats.installAndMatch(repo, hibernateCore, "/org.hibernate^hibernate-core^5.4.1.Final.grdl");
      ResolutionStats rsSpring = ResolutionStats.installAndMatch(repo, spring, "/org.springframework.boot^spring-boot-starter-web^2.1.2.RELEASE.grdl");
      ResolutionStats rsSpark = ResolutionStats.installAndMatch(repo, spark, "/org.apache.spark^spark-core_2.12^2.4.0.grdl");
      ResolutionStats rsDl4j = ResolutionStats.installAndMatch(repo, dl4j, "/org.deeplearning4j^deeplearning4j-core^1.0.0-beta3.grdl");

      log.info(rsQueryDslJpa.toString());
      log.info(rsArrowJdbc.toString());
      log.info(rsGoogleApiClient.toString());
      log.info(rsHibernateCore.toString());
      log.info(rsSpring.toString());
      log.info(rsSpark.toString());
      log.info(rsDl4j.toString());
    });
  }
}
