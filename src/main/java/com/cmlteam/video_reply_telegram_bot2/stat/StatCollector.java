package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;

@RequiredArgsConstructor
public class StatCollector {
  private final List<StatInterval> statIntervals = new ArrayList<>();
  private final long intervalMillis;
  private final TimeProvider timeProvider;

  public synchronized void track(String key) {
    long intervalStartMillis = timeProvider.getCurrentTimeMillis();
    StatInterval statInterval;
    if (statIntervals.isEmpty()
        || (statInterval = statIntervals.get(statIntervals.size() - 1)).intervalStartMillis
            < intervalStartMillis - intervalMillis) {
      statIntervals.add(statInterval = new StatInterval(intervalStartMillis));
    }
    statInterval.track(key);
  }

  /**
   * @param duration time interval to report stat for
   * @return stat ordered by count desc
   */
  public Map<String, Integer> reportStat(Duration duration) {
    long now = timeProvider.getCurrentTimeMillis();
    long from = now - duration.toMillis();

    ListIterator<StatInterval> statBackIterator = statIntervals.listIterator(statIntervals.size());

    Map<String, Integer> summed = new HashMap<>();
    StatInterval statInterval;

    while (statBackIterator.hasPrevious()
        && (statInterval = statBackIterator.previous()).intervalStartMillis > from) {
      for (Entry<String, Integer> entry : statInterval.counts.entrySet()) {
        String key = entry.getKey();
        int value = entry.getValue();
        summed.put(key, summed.getOrDefault(key, 0) + value);
      }
    }

    return sortMapByValueDesc(summed);
  }

  private static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDesc(Map<K, V> map) {
    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

    Map<K, V> result = new LinkedHashMap<>();
    for (Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
