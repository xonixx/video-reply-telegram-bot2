package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cmlteam")
public class VideoReplyBot2Application {

  public static void main(String[] args) {
    SpringApplication.run(VideoReplyBot2Application.class, args);
  }
}
