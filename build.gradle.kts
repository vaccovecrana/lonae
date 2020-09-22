buildscript {
  repositories {
    jcenter(); gradlePluginPortal()
    maven { name = "VaccoOss"; setUrl("https://dl.bintray.com/vaccovecrana/vacco-oss") }
  }
  dependencies { classpath("io.vacco:common-build-gradle-plugin:0.5.0") }
}

apply{plugin(io.vacco.common.CbPlugin::class.java)}

group = "io.vacco.myrmica"
version = "0.8.0"

configure<io.vacco.common.CbPluginProfileExtension> {
  addJ8Spec(); addPmd(); addSpotBugs(); addClasspathHell()
  setPublishingUrlTransform { repo -> "${repo.url}/${project.name}" }
  sharedLibrary()
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val api by configurations
val implementation by configurations
val testImplementation by configurations

dependencies {
  api("org.slf4j:slf4j-api:1.7.30")
  implementation("org.jooq:joox-java-6:1.6.0")
  implementation("io.vacco.oriax:oriax:0.1.0")
  testImplementation("io.vacco.shax:shax:1.7.30.0.0.4")
  testImplementation("com.esotericsoftware.yamlbeans:yamlbeans:1.13")
}
