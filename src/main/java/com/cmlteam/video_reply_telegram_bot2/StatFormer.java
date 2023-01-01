package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.util.StringUtils;

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
          .append(escapeForMarkdownV2(entry.getKey()))
          .append("*: ")
          .append(entry.getValue())
          .append('\n');
    }

    return result.toString();
  }

  /** See https://core.telegram.org/bots/api#markdownv2-style TODO move to tg-bot-common */
  static String escapeForMarkdownV2(String s) {
    if (!StringUtils.hasLength(s)) {
      return "";
    }
    return s.replaceAll("[_*\\[\\]()~`>#+\\-=|{}.!]", "\\\\$0");
  }
}
