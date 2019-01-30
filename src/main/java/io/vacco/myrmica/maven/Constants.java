package io.vacco.myrmica.maven;

import java.util.Arrays;

public class Constants {

  public static final String DEFAULT_ARTIFACT_TYPE = "jar";

  public enum PomTag {
    id,
    groupId, artifactId, version, scope, optional,

    dependencyManagement, dependencies, dependency, exclusions, exclusion, properties,

    build, description, developers,
    distributionManagement, inceptionYear, issueManagement, licenses, mailingLists,
    modules, organization, parent, pluginRepositories, profile, reporting, repositories,
    scm, url;

    public static String [] exclusionTags() {
      return stringValues(build, description, developers, distributionManagement,
          inceptionYear, issueManagement, licenses, mailingLists, modules, organization,
          ComponentTag.packaging, parent, pluginRepositories, reporting, repositories, scm, url);
    }
  }

  public enum ComponentTag {
    type, extension, packaging, classifier, language, addedToClasspath
  }

  public enum Scope { compile, runtime }

  public static String [] stringValues(Enum ... args) {
    return Arrays.stream(args).map(Enum::toString).toArray(String[]::new);
  }
}
