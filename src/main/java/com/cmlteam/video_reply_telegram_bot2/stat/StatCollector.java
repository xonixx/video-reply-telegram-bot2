package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class StatCollector {
  private final List<StatInterval> statIntervals = new ArrayList<>();
  private final long intervalMillis;
  private final TimeProvider timeProvider;

  public synchronized void track(String key) {
    long intervalNo = timeProvider.getCurrentTimeMillis() / intervalMillis;
    StatInterval statInterval;
    if (statIntervals.isEmpty()
        || (statInterval = statIntervals.get(statIntervals.size() - 1)).intervalNo != intervalNo) {
      statInterval = new StatInterval(intervalNo);
    }
    statInterval.track(key);
  }
}
