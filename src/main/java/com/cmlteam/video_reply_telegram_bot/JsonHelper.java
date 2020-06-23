package com.cmlteam.video_reply_telegram_bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class JsonHelper {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  String toPrettyString(Object obj) {
    return gson.toJson(obj);
  }

  String toPrettyString(String str) {
    return toPrettyString(gson.fromJson(str, Map.class));
  }
}
