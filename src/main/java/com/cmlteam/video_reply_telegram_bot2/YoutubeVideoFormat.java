package com.cmlteam.video_reply_telegram_bot2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YoutubeVideoFormat {
  private String url;
  private long filesize;
  private String format_id;
  private String ext;
}
