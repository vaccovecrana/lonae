package io.vacco.myrmica.maven.schema;

import java.util.Arrays;
import java.util.Optional;

public class MmConstants {

  public enum PomTag {
    project, id, groupId, artifactId, version, scope, optional,

    dependencyManagement, dependencies, dependency, exclusions, exclusion, properties,

    build, ciManagement, contributors, description, developers,
    distributionManagement, inceptionYear, issueManagement, licenses, mailingLists,
    modules, organization, parent, pluginRepositories, profile, profiles, reporting, repositories,
    scm, url;

    public static String [] exclusionTags() {
      return stringValues(
          build, ciManagement, contributors, description, developers, distributionManagement,
          inceptionYear, issueManagement, licenses, mailingLists, modules, organization,
          pluginRepositories, profiles, reporting, repositories, scm, url
      );
    }
  }

  public enum ArtifactHandler {
    Pom("pom"), Jar("jar"), TestJar("test-jar"), MavenPlugin("maven-plugin"),
    Ejb("ejb"), EjbClient("ejb-client"), War("war"), Ear("ear"), Rar("rar"),
    JavaSource("java-source"), JavaDoc("javadoc");
    public String label;
    ArtifactHandler(String label) { this.label = label; }
    public static Optional<ArtifactHandler> fromLabel(String label) {
      return Arrays.stream(values()).filter(ah -> ah.label.equals(label)).findFirst();
    }
  }

  public enum ComponentTag {
    component("component"), configuration("configuration"),
    roleHint("role-hint"),
    type("type"), extension("extension"), packaging("packaging"),
    classifier("classifier"), language("language"),
    addedToClasspath("addedToClasspath"), includesDependencies("includesDependencies");
    public String label;
    ComponentTag(String label) { this.label = label; }
  }

  public enum Scope {
    Compile("compile"), Import("import"),
    Provided("provided"), Runtime("runtime"), Test("test");
    public String label;
    Scope(String label) { this.label = label; }
    public static Optional<Scope> fromLabel(String label) {
      return Arrays.stream(values()).filter(sc -> sc.label.equals(label)).findFirst();
    }
  }

  public static String [] stringValues(Enum<?> ... args) {
    return Arrays.stream(args).map(Enum::toString).toArray(String[]::new);
  }
}
