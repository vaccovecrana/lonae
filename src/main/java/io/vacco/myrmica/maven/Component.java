package io.vacco.myrmica.maven;

import org.joox.Match;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.myrmica.maven.Constants.*;
import static org.joox.JOOX.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @see <a href="https://maven.apache.org/ref/3.6.0/maven-core/artifact-handlers.html}">artifact-handlers</a>
 */
public class Component implements Cloneable {

  public String type;
  public String extension;
  public String packaging;
  public String classifier;
  public final String language;
  public final boolean addedToClasspath;

  public Component(Match src) {
    this.type = requireNonNull(src.child(ComponentTag.type.toString()).text());
    this.extension = src.child(ComponentTag.extension.toString()).text();
    this.packaging = src.child(ComponentTag.packaging.toString()).text();
    setClassifier(src);
    this.language = src.child(ComponentTag.language.toString()).text();
    this.addedToClasspath = Boolean.parseBoolean(src.child(ComponentTag.addedToClasspath.toString()).text());
    if (extension == null) extension = type;
    if (packaging == null) packaging = type;
  }

  public void setClassifier(Match n) {
    String cl = n.child(ComponentTag.classifier.toString()).text();
    this.classifier = cl != null ? cl : this.classifier;
  }

  private static final Collection<Component> defaults = new ArrayList<>();

  static {
    try {
      URL handlers = Component.class.getResource("/io/vacco/myrmica/maven/artifact-handlers.xml");
      Match xml = $(handlers);
      defaults.addAll(xml.find("configuration").each().stream().map(Component::new).collect(Collectors.toList()));
    } catch (Exception e) {
      throw new IllegalStateException("Cannot resolve default component artifact handlers.");
    }
  }

  private static Component cloneOf(Component c) {
    if (c == null) return null;
    try { return (Component) c.clone(); }
    catch (CloneNotSupportedException e) { throw new IllegalStateException(e); }
  }

  public static Optional<Component> forType(String t) {
    Optional<Component> result = defaults.stream().filter(cmp -> cmp.type.equals(t)).findFirst();
    return result.map(Component::cloneOf);
  }
  public static Optional<Component> forPackaging(String p) {
    Optional<Component> result = defaults.stream().filter(cmp -> cmp.packaging.equals(p)).findFirst();
    return result.map(Component::cloneOf);
  }

  @Override public String toString() {
    return format("comp[%s%s%s%s%s%s]", type,
        extension != null ? format(", ext: %s", extension) : "",
        packaging != null ? format(", pkg: %s", packaging) : "",
        classifier != null ? format(", cls: %s", classifier) : "",
        language != null ? format(", lng: %s", language) : "",
        format(", rt: %s", addedToClasspath)
    );
  }
}
