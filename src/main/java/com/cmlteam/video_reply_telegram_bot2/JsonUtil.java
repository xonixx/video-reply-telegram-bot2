package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class JsonUtil {
  private JsonUtil() {}

  private static final ObjectMapper OBJECT_MAPPER = prepareObjectMapper();

  private static ObjectMapper prepareObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    mapper.setDateFormat(df);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  public static String toJsonString(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<?> parseList(String json) {
    if (StringUtils.isBlank(json)) {
      return Collections.emptyList();
    }
    try {
      return OBJECT_MAPPER.readValue(json, List.class);
    } catch (IOException e) {
      throw new RuntimeException("Unable to parse json to list: " + Util.trim(json, 100), e);
    }
  }

  public static Map parseJson(String json) {
    if (StringUtils.isBlank(json)) {
      return Collections.emptyMap();
    }
    try {
      return OBJECT_MAPPER.readValue(json, Map.class);
    } catch (IOException e) {
      throw new RuntimeException("Unable to parse json to map: " + Util.trim(json, 100), e);
    }
  }

  public static <T> T parseJson(String json, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("Unable to parse json to class: " + Util.trim(json, 100), e);
    }
  }

  public static <T> List<T> parseJsonList(String json, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(
          json, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    } catch (IOException e) {
      throw new RuntimeException("Unable to parse json to class: " + Util.trim(json, 100), e);
    }
  }

  public static String prettyPrintJson(String json) {
    if (json == null) return null;
    try {
      Object parsed = OBJECT_MAPPER.readValue(json, Object.class);
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
    } catch (IOException ignore) {
      return json;
    }
  }
}
