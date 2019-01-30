package io.vacco.myrmica.maven;

import java.util.Arrays;

public class Constants {

  public enum PomTag {
    id,
    groupId, artifactId, version,
    classifier, packaging, scope, optional, type,

    dependencyManagement, dependencies, dependency, exclusions, exclusion,

    build, description, developers,
    distributionManagement, inceptionYear, issueManagement, licenses, mailingLists,
    modules, organization, parent, pluginRepositories, profile, reporting, repositories,
    scm, url;

    public static String [] exclusionTags() {
      return stringValues(build, description, developers, distributionManagement,
          inceptionYear, issueManagement, licenses, mailingLists, modules, organization,
          parent, pluginRepositories, reporting, repositories, scm, url);
    }
  }

  /**
   * @see <a href="https://maven.apache.org/ref/3.6.0/maven-core/artifact-handlers.html}">artifact-handlers</a>
   */
  public enum PackageType { //
    pom("pom"), jar("jar"), mavenPlugin("maven-plugin"),
    ejb("ejb"), war("war"), ear("ear"), rar("rar");

    private final String label;
    PackageType(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  /**
   * @see <a href="https://maven.apache.org/pom.html#Maven_Coordinates">Maven Coordinates</a>
   */
  public enum Scope {
    compile, provided, runtime, test, system
  }

  public static String [] stringValues(Enum ... args) {
    return Arrays.stream(args).map(Enum::toString).toArray(String[]::new);
  }
}
