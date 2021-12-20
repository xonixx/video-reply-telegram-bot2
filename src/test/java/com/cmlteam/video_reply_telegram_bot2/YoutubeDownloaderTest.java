package com.cmlteam.video_reply_telegram_bot2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YoutubeDownloaderTest {
  private final YoutubeDownloader youtubeDownloader = new YoutubeDownloader(null);

  @Test
  void testPositiveUrl1() {
    assertTrue(youtubeDownloader.isYoutubeLink("https://www.youtube.com/watch?v=_OBlgSz8sSM"));
  }

  @Test
  void testPositiveUrl2() {
    assertTrue(youtubeDownloader.isYoutubeLink("https://youtube.com/watch?v=_OBlgSz8sSM"));
  }

  @Test
  void testPositiveUrl3() {
    assertTrue(youtubeDownloader.isYoutubeLink("https://youtu.be/_OBlgSz8sSM"));
  }

  @Test
  void testPositiveUrl4() {
    assertTrue(youtubeDownloader.isYoutubeLink("http://youtu.be/_OBlgSz8sSM"));
  }

  @Test
  void testNegativeUrl1() {
    assertFalse(youtubeDownloader.isYoutubeLink("https://www.youtu.be/_OBlgSz8sSM"));
  }

  @Test
  void testNegativeUrl2() {
    assertFalse(youtubeDownloader.isYoutubeLink("some-text"));
  }
  @Test
  void testNegativeUrl3() {
    assertFalse(youtubeDownloader.isYoutubeLink("https://example.com/sdf"));
  }
}
