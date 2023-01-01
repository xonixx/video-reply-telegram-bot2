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
    long intervalNo = timeProvider.getCurrentTimeMillis() / intervalMillis;
    StatInterval statInterval;
    if (statIntervals.isEmpty()
        || (statInterval = statIntervals.get(statIntervals.size() - 1)).intervalNo != intervalNo) {
      statIntervals.add(statInterval = new StatInterval(intervalNo));
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
    long fromNo = from / intervalMillis;
    if (from < 0) { // 2 -> 0; we want -2 -> -1 (not -2 -> 0)
      fromNo--;
    }

    ListIterator<StatInterval> statBackIterator = statIntervals.listIterator(statIntervals.size());

    StatInterval statInterval;
    Map<String, Integer> summed = new HashMap<>();
    while (statBackIterator.hasPrevious()
        && (statInterval = statBackIterator.previous()).intervalNo > fromNo) {
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
