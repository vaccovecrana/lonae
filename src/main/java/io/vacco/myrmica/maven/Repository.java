package io.vacco.myrmica.maven;

import com.github.underscore.lodash.U;
import com.github.underscore.lodash.Xml;
import io.vacco.myrmica.util.XmlUtil;
import org.joox.Match;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static java.util.Objects.*;
import static org.joox.JOOX.*;

public class Repository {

  private Path localRoot;
  private URI remoteRoot;

  public Repository(String localRootPath, String remotePath) {
    this.localRoot = Paths.get(requireNonNull(localRootPath));
    if (!localRoot.toFile().exists()) {
      throw new IllegalArgumentException(
          String.format("Missing root folder: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!localRoot.toFile().isDirectory()) {
      throw new IllegalArgumentException(
          String.format("Not a directory: [%s]", localRoot.toAbsolutePath().toString()));
    }
    if (!requireNonNull(remotePath).endsWith("/")) {
      throw new IllegalArgumentException(
          String.format("Remote path does not end with a trailing slash: [%s]", remotePath));
    }
    try { this.remoteRoot = new URI(remotePath); }
    catch (URISyntaxException e) { throw new IllegalStateException(e); }
  }

  public Match loadPom(Coordinates c) {
    requireNonNull(c);
    try {
      Path target = c.getLocalPomPath(localRoot);
      if (!target.toFile().getParentFile().exists()) { target.toFile().getParentFile().mkdirs(); }
      if (!target.toFile().exists()) { Files.copy(c.getPomUri(remoteRoot).toURL().openStream(), target); }
      return $(target.toFile());
    }
    catch (Exception e) { throw new IllegalStateException(e); }
  }

  public Optional<Coordinates> loadParent(Match pom) {
    Match p = pom.child("parent");
    if (p.size() == 0) return Optional.empty();
    return Optional.of(new Coordinates(
        p.child("groupId").text(),
        p.child("artifactId").text(),
        p.child("version").text()
    ));
  }

  public Match buildPom(Coordinates root) {
    List<Match> poms = new ArrayList<>();
    Optional<Coordinates> oc = Optional.of(root);
    while (oc.isPresent()) {
      Match pp = loadPom(oc.get());
      poms.add(pp);
      oc = loadParent(pp);
    }
    // Collections.reverse(poms);
    Map<String, Object> o = poms.stream()
        .map(pom -> Xml.fromXml(pom.toString()))
        .map(pom -> (Map<String, Object>) pom)
        .map(pom -> {
          pom.remove("#omit-xml-declaration");
          //((Map) pom.get("project")).remove("build");
          //((Map) pom.get("project")).remove("developers");
          return pom;
        }).reduce((pom0, pom1) -> XmlUtil.mapMerge(pom1, pom0)).get();
    return $(Xml.toXml(o));
  }

}
