package com.cmlteam.video_reply_telegram_bot2.imp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "videos")
@Getter
@Setter
public class VideosListProperties {
  private List<Video> list;
}
