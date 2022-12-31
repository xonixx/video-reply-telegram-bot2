package com.cmlteam.video_reply_telegram_bot2.stat;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

public class StatCollectorTests {
  @Test
  void test1() {
    // GIVEN

    TimeProviderForTest timeProviderForTest = new TimeProviderForTest(0);
    long duration5Min = Duration.ofMinutes(5).toMillis();
    StatCollector statCollector = new StatCollector(duration5Min, timeProviderForTest);

    // WHEN

    statCollector.track("user1");
    statCollector.track("user1");
    statCollector.track("user2");

    timeProviderForTest.increment(duration5Min);

    statCollector.track("user1");
    statCollector.track("user2");
    statCollector.track("user3");

    timeProviderForTest.increment(duration5Min);

    statCollector.track("user3");
    statCollector.track("user3");
    statCollector.track("user4");

    // THEN

    Assertions.assertEquals(
        Map.of("user3", 2, "user4", 1), statCollector.reportStat(Duration.ofMinutes(4)));
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
