package io.vacco.myrmica.maven.schema;

import static java.lang.String.*;

/**
 * @see <a href="https://maven.apache.org/ref/3.6.0/maven-core/artifact-handlers.html">artifact-handlers</a>
 */
public class MmComponent {

  public String roleHint;
  public MmConstants.ArtifactHandler type;
  public String extension, packaging, classifier, language;
  public boolean addedToClasspath, includesDependencies;

  public String toString() {
    return format("%s[%s%s%s%s%s%s%s]", roleHint, format("type: %s", type),
        extension != null ? format(", ext: %s", extension) : "",
        packaging != null ? format(", pkg: %s", packaging) : "",
        classifier != null ? format(", clssfr: %s", classifier) : "",
        language != null ? format(", lang: %s", language) : "",
        format(", addCp: %s", addedToClasspath),
        format(", incDep: %s", includesDependencies)
    );
  }

  @Override public boolean equals(Object o) {
    if (o instanceof MmComponent) {
      MmComponent oc = (MmComponent) o;
      boolean te = type.equals(oc.type);
      boolean xe = extension.equals(oc.extension);
      boolean pe = packaging.equals(oc.packaging);
      boolean ce = classifier != null && classifier.equals(oc.classifier);
      boolean le = language != null && language.equals(oc.language);
      boolean ae = addedToClasspath == oc.addedToClasspath;
      boolean ie = includesDependencies == oc.includesDependencies;
      return te & xe & pe & ce & le & ae & ie;
    }
    return false;
  }
}
