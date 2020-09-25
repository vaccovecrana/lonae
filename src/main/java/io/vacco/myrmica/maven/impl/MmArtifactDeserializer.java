package io.vacco.myrmica.maven.impl;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.vacco.myrmica.maven.schema.*;

import java.io.IOException;

public class MmArtifactDeserializer extends StdDeserializer<MmArtifact> {

  public MmArtifactDeserializer() {
    super(MmArtifact.class);
  }

  @Override
  public MmArtifact deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode n = p.getCodec().readTree(p);
    MmArtifact art = new MmArtifact();
    art.at = p.getCodec().treeToValue(n, MmCoordinates.class);
    art.comp = p.getCodec().treeToValue(n, MmComponent.class);
    art.meta = p.getCodec().treeToValue(n, MmArtifactMeta.class);
    return art;
  }
}
