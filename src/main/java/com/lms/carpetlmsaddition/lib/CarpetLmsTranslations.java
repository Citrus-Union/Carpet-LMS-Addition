package com.lms.carpetlmsaddition.lib;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class CarpetLmsTranslations {
  private static final Logger LOGGER = LoggerFactory.getLogger(CarpetLmsTranslations.class);
  private static final Yaml YAML = new Yaml(new SafeConstructor(new LoaderOptions()));
  private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

  public static Map<String, String> getTranslation(String lang) {
    return CACHE.computeIfAbsent(lang, CarpetLmsTranslations::loadTranslation);
  }

  public static String translate(String key) {
    return translate(key, "en_us");
  }

  public static String translate(String key, String lang) {
    String value = getTranslation(lang).get(key);
    if (value != null) {
      return value;
    }
    if (!"en_us".equals(lang)) {
      value = getTranslation("en_us").get(key);
      if (value != null) {
        return value;
      }
    }
    return "";
  }

  private static Map<String, String> loadTranslation(String lang) {
    String path = "assets/carpet_lms_addition/lang/%s.yml".formatted(lang);
    try (InputStream stream =
            CarpetLmsTranslations.class.getClassLoader().getResourceAsStream(path);
        InputStreamReader reader =
            stream == null ? null : new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      if (reader == null) {
        return Collections.emptyMap();
      }

      Object data = YAML.load(reader);
      if (!(data instanceof Map<?, ?> root)) {
        return Collections.emptyMap();
      }

      Map<String, String> flat = new HashMap<>();
      flatten(root, "", flat);
      return flat;
    } catch (Exception e) {
      LOGGER.warn("Failed to load translations for {}", lang, e);
      return Collections.emptyMap();
    }
  }

  private static void flatten(Object node, String prefix, Map<String, String> out) {
    if (node instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!(entry.getKey() instanceof String key)) {
          continue;
        }
        String childPrefix = prefix.isEmpty() ? key : prefix + "." + key;
        flatten(entry.getValue(), childPrefix, out);
      }
      return;
    }

    if (node instanceof List<?> list) {
      for (int i = 0; i < list.size(); i++) {
        String childPrefix = prefix.isEmpty() ? String.valueOf(i) : prefix + "." + i;
        flatten(list.get(i), childPrefix, out);
      }
      return;
    }

    if (!prefix.isEmpty() && node != null) {
      out.put(prefix, node.toString());
    }
  }
}
