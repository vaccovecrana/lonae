package io.vacco.myrmica;

import java.net.URL;
import java.util.Scanner;

public class MmUtil {

  public static String load(URL resource) {
    try {
      return new Scanner(resource.openStream(), "UTF-8").useDelimiter("\\A").next();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
