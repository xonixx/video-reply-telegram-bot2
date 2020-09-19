package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class YoutubeDownloader {
  Pattern youtubeLinkRegex =
      Pattern.compile("^(https?://)?((www\\.)?youtube\\.com|youtu\\.?be)/.+$");

  boolean isYoutubeLink(String candidate) {
    return youtubeLinkRegex.matcher(candidate).matches();
  }
}
