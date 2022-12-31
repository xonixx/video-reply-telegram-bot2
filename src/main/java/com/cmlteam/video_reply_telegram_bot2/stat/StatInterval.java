package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class StatInterval {
  final long intervalNo;
  private final Map<String, Integer> counts = new ConcurrentHashMap<>();

  void track(String key) {
    counts.put(key, counts.getOrDefault(key, 0) + 1);
  }
}
