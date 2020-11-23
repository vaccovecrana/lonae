package io.vacco.lonae.maven.schema;

import com.fasterxml.jackson.annotation.*;
import static java.lang.String.*;

/**
 * @see <a href="https://maven.apache.org/ref/3.6.0/maven-core/artifact-handlers.html">artifact-handlers</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MmComponent {

  public enum Type {
    pom,
    jar, @JsonProperty("test-jar") testJar, @JsonProperty("java-source") javaSource, javadoc,
    ejb, @JsonProperty("ejb-client") ejbClient, ear,
    @JsonProperty("maven-plugin") mavenPlugin,
    war, rar,
    @JsonEnumDefaultValue unknown
  }

  public Type type, packaging;
  public String classifier;
  public String language;
  public boolean addedToClasspath, includesDependencies;

  public String toString() {
    return format("[%s%s%s%s%s%s]",
        format("type: %s", type),
        packaging != null ? format(", pkg: %s", packaging) : "",
        classifier != null ? format(", cls: %s", classifier) : "",
        language != null ? format(", lng: %s", language) : "",
        format(", addClp: %s", addedToClasspath),
        format(", incDep: %s", includesDependencies)
    );
  }

  public MmComponent copy() {
    MmComponent c0 = new MmComponent();
    c0.addedToClasspath = this.addedToClasspath;
    c0.classifier = this.classifier;
    c0.includesDependencies = this.includesDependencies;
    c0.language = this.language;
    c0.packaging = this.packaging;
    c0.type = this.type;
    return c0;
  }
}
