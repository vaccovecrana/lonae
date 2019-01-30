package io.vacco.myrmica.maven;

import org.joox.Match;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Objects.*;

public class Pom {

  private final Match ePom;
  private final Set<Artifact> defaultVersions;

  public Pom(Match ePom) {
    this.ePom = requireNonNull(ePom);
    /*
    super(requireNonNull(ePom).child("groupId").text(),
        ePom.child("artifactId").text(), ePom.child("version").text());
    this.ePom = ePom;
    this.classifier = ePom.child("classifier").text();
    this.scope = ePom.child("scope").text();
    this.optional = ePom.child("optional").isNotEmpty();
//     this.exclusions.addAll(pomXml.child("exclusions").children("exclusion").each().stream().map(el -> new ModuleMetadata(el, resolvedProperties)).collect(Collectors.toSet()));
*/
   this.defaultVersions = new TreeSet<>(
       ePom.child("dependencyManagement").child("dependencies").children().each().stream()
           .map(Artifact::new).collect(Collectors.toSet()));
  }

  public Set<Artifact> getDefaultDependencies() {

    return null;
  }

}
