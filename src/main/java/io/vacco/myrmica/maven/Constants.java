package io.vacco.myrmica.maven;

import java.util.Arrays;

public class Constants {

  public static final String DEFAULT_ARTIFACT_TYPE = "jar";

  public enum PomTag {
    id,
    groupId, artifactId, version, scope, optional,

    dependencyManagement, dependencies, dependency, exclusions, exclusion, properties,

    build, ciManagement, contributors, description, developers,
    distributionManagement, inceptionYear, issueManagement, licenses, mailingLists,
    modules, organization, parent, pluginRepositories, profile, profiles, reporting, repositories,
    scm, url;

    public static String [] exclusionTags() {
      return stringValues(build, ciManagement, contributors, description, developers, distributionManagement,
          inceptionYear, issueManagement, licenses, mailingLists, modules, organization,
          ComponentTag.packaging, parent, pluginRepositories, profiles, reporting, repositories, scm, url);
    }
  }

  public enum ComponentTag {
    type, extension, packaging, classifier, language, addedToClasspath
  }

  public static final String scope_compile = "compile";
  public static final String scope_runtime = "runtime";
  public static final String scope_test = "test";
  public static final String scope_import = "import";

  public static String [] stringValues(Enum ... args) {
    return Arrays.stream(args).map(Enum::toString).toArray(String[]::new);
  }
}
