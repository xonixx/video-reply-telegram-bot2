package com.cmlteam.video_reply_telegram_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cmlteam")
public class VideoReplyBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(VideoReplyBotApplication.class, args);
  }
}
