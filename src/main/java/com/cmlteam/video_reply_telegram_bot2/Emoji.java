package com.cmlteam.video_reply_telegram_bot2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Emoji {
  SUCCESS("✅"),
  WARN("⚠️"),
  ERROR("❌");

  private final String C;

  String msg(String s) {
    return C + " " + s;
  }
}
