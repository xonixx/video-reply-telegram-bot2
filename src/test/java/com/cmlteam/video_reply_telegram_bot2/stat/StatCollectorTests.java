package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatCollectorTests {

  private static final long D_100_days_in_millis = 864_000_000;

  @ParameterizedTest
  @ValueSource(longs = {0, D_100_days_in_millis, 10_000_000})
  void test1(long initialTime) {
    // GIVEN

    TimeProviderForTest timeProviderForTest = new TimeProviderForTest(initialTime);
    long duration5Min = Duration.ofMinutes(5).toMillis();
    StatCollector statCollector = new StatCollector(duration5Min, timeProviderForTest);

    // WHEN

    statCollector.track("user1");
    statCollector.track("user2");
    statCollector.track("user3");
    statCollector.track("user4");

    timeProviderForTest.increment(duration5Min + 1);

    statCollector.track("user1");
    statCollector.track("user3");
    statCollector.track("user4");

    timeProviderForTest.increment(duration5Min + 1);

    statCollector.track("user3");
    statCollector.track("user3");
    statCollector.track("user4");

    // THEN

    Assertions.assertEquals(
        List.of("user3:2", "user4:1"),
        entriesAsList(statCollector.reportStat(Duration.ofMinutes(4))));

    Assertions.assertEquals(
        List.of("user3:3", "user4:2", "user1:1"),
        entriesAsList(statCollector.reportStat(Duration.ofMinutes(6))));

    Assertions.assertEquals(
        List.of("user3:4", "user4:3", "user1:2", "user2:1"),
        entriesAsList(statCollector.reportStat(Duration.ofMinutes(11))));
  }

  @NotNull
  private static List<String> entriesAsList(Map<String, Integer> stringIntegerMap) {
    return stringIntegerMap.entrySet().stream()
        .map(e -> e.getKey() + ":" + e.getValue())
        .collect(Collectors.toList());
  }

  @AllArgsConstructor
  static class TimeProviderForTest implements TimeProvider {

    private long timeMillis;

    void increment(long millis) {
      timeMillis += millis;
    }

    @Override
    public long getCurrentTimeMillis() {
      return timeMillis;
    }
  }
}
