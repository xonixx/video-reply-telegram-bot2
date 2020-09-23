package com.cmlteam.video_reply_telegram_bot2;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class YoutubeVideoInfo {
  private String id; // YT ID
  private String fulltitle;
  private String title;
  private int duration; // sec
  private String description;
  private String uploader;
  private String uploader_id;
  private long view_count;
  private List<YoutubeVideoFormat> formats = new ArrayList<>();

  private static final String FORMAT_ID = "18";

  Optional<YoutubeVideoFormat> getAppropriateFormat() {
    return formats.stream().filter(f -> FORMAT_ID.equals(f.getFormat_id())).findAny();
  }
}
