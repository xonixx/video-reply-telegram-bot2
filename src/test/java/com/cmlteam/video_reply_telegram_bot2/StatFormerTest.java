package com.cmlteam.video_reply_telegram_bot2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatFormerTest {
  @Test
  void test1() {
    // GIVEN

    StatFormer statFormer = new StatFormer();

    Map<String, Integer> stat = new LinkedHashMap<>();
    stat.put("user1", 10);
    stat.put("user2", 3);
    stat.put("user3", 1);

    // WHEN
    String markdown = statFormer.formStatMarkdown(stat);

    // THEN
    Assertions.assertEquals("Stats:\n1. *user1*: 10\n2. *user2*: 3\n3. *user3*: 1\n", markdown);
  }

  @Test
  void testMarkdownV2Escaping() {
    Assertions.assertEquals(
        "aaa\\_\\*\\[\\]\\(\\)\\~\\`\\>\\#\\+\\-\\=\\|\\{\\}\\.\\!bbb",
        StatFormer.escapeForMarkdownV2("aaa_*[]()~`>#+-=|{}.!bbb"));
  }
}
