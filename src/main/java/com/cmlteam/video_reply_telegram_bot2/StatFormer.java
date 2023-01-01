package com.cmlteam.video_reply_telegram_bot2;

import java.util.Map;
import java.util.Map.Entry;

public class StatFormer {
  public String formStatMarkdown(Map<String, Integer> stats) {
    StringBuilder result = new StringBuilder("Stats:\n");

    int i = 0;
    for (Entry<String, Integer> entry : stats.entrySet()) {
      result
          .append(++i)
          .append(". *")
          .append(entry.getKey())
          .append("*: ")
          .append(entry.getValue())
          .append('\n');
    }

    return result.toString();
  }
}
