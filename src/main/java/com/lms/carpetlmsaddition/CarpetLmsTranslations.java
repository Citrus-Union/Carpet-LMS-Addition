package com.lms.carpetlmsaddition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetLmsTranslations {
  private static final Gson GSON = new Gson();

  public static Map<String, String> getTranslation(String lang) {
    String path = "assets/carpet_lms_addition/lang/%s.json".formatted(lang);
    InputStream stream = CarpetLmsTranslations.class.getClassLoader().getResourceAsStream(path);
    if (stream == null) {
      return Collections.emptyMap();
    }

    try {
      String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      return GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }
}
