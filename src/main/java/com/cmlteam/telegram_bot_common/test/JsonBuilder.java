package com.cmlteam.telegram_bot_common.test;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonBuilder {
  private final Gson gson = new Gson();

  private Map<String, Object> map = new LinkedHashMap<>();

  public JsonBuilder add(String key, Object value) {
    map.put(key, value);
    return this;
  }

  public JsonBuilder add(String key, JsonBuilder value) {
    map.put(key, value.map);
    return this;
  }

  @Override
  public String toString() {
    return gson.toJson(map);
  }

  public static JsonBuilder json() {
    return new JsonBuilder();
  }
}
