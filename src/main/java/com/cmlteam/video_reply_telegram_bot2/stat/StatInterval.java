package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class StatInterval {
  final long intervalStartMillis;
  final Map<String, Integer> counts = new HashMap<>();

  void track(String key) {
    counts.put(key, counts.getOrDefault(key, 0) + 1);
  }
}
