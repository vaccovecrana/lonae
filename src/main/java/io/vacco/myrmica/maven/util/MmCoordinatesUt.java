package io.vacco.myrmica.maven.util;

import io.vacco.myrmica.maven.schema.MmConstants;
import io.vacco.myrmica.maven.schema.MmCoordinates;
import org.joox.Match;

import java.nio.file.Path;
import java.nio.file.Paths;


public class MmCoordinatesUt {

  public static MmCoordinates fromXml(Match xml) {
    return MmCoordinates.from(xml.child(MmConstants.PomTag.groupId.toString()).text(),
        xml.child(MmConstants.PomTag.artifactId.toString()).text(),
        xml.child(MmConstants.PomTag.version.toString()).text());
  }

  public static Path getResourcePath(MmCoordinates coordinates) {
    return Paths.get(
        coordinates.groupId.replace(".", "/"),
        coordinates.artifactId, coordinates.version
    );
  }
}
