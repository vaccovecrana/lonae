plugins {
  id("io.vacco.common-build") version "0.5.3"
}

group = "io.vacco.lonae"
version = "0.9.9"

configure<io.vacco.common.CbPluginProfileExtension> {
  addJ8Spec(); addPmd(); addSpotBugs(); addClasspathHell()
  setPublishingUrlTransform { repo -> "${repo.url}/${project.name}" }
  sharedLibrary()
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

configure<io.vacco.cphell.ChPluginExtension> {
  resourceExclusions.add("module-info.class")
}

val api by configurations

dependencies {
  api("org.slf4j:slf4j-api:1.7.30")
  api("org.jooq:joox-java-6:1.6.0")
  api("com.fasterxml.jackson.core:jackson-databind:2.11.2")
  api("io.vacco.oriax:oriax:0.1.0")
  testImplementation("io.vacco.shax:shax:1.7.30.0.0.6")
}

/*
tasks.withType<Test> {
  extensions.configure(JacocoTaskExtension::class) {
    output = JacocoTaskExtension.Output.TCP_CLIENT
    address = "localhost"
    port = 6300
    sessionId = "lonae-manual@io.vacco.lonae:lonae:0.9.6"
  }
}
*/
