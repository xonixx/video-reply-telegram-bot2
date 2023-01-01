package com.cmlteam.video_reply_telegram_bot2.stat;

public class TimeProviderDefault implements TimeProvider {
  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }
}
