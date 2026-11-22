package com.cmlteam.telegram_bot_common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonHelper {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public String toPrettyString(Object obj) {
    return gson.toJson(obj);
  }

  public String toPrettyString(String str) {
    return toPrettyString(gson.fromJson(str, Map.class));
  }
}
