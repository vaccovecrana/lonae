package io.vacco.lonae.maven.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class MmJsonLog {

  private static final ObjectMapper om = new ObjectMapper();
  private static final ObjectWriter pw = om.writerWithDefaultPrettyPrinter();

  public static String jsonLogOf(Object o) {
    try {
      return pw.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
