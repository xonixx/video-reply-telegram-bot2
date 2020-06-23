package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {
  @Bean
  TelegramBot getTelegramBot(BotProperties botProperties) {
    TelegramBot telegramBot = new TelegramBot(botProperties.getToken());
    GetMeResponse response = telegramBot.execute(new GetMe());
    if (response.user() == null) {
      throw new IllegalArgumentException("bot token is incorrect");
    }
    return telegramBot;
  }

  @Bean
  ErrorReporter errorReporter(
      TelegramBot telegramBot, JsonHelper jsonHelper, BotProperties botProperties) {
    return new ErrorReporter(telegramBot, jsonHelper, botProperties.getAdminUser());
  }

  @Bean
  TelegramBotWrapper telegramBotWrapper(
      TelegramBot telegramBot, JsonHelper jsonHelper, ErrorReporter errorReporter) {
    return new TelegramBotWrapper(telegramBot, jsonHelper, errorReporter);
  }

  @Bean
  VideosBackupper videosBackupper(
      BotProperties botProperties,
      TelegramBotWrapper telegramBotWrapper,
      VideosListProperties videosListProperties) {
    return new VideosBackupper(
        botProperties.getBackupFolder(),
        botProperties.getToken(),
        telegramBotWrapper,
        videosListProperties);
  }

  @Bean
  public BotPollingJob botPollingJob(
      BotProperties botProperties,
      TelegramBotWrapper telegramBotWrapper,
      VideosListService videosListService,
      VideosBackupper videosBackupper,
      JsonHelper jsonHelper) {
    return new BotPollingJob(
        telegramBotWrapper,
        videosListService,
        videosBackupper,
        jsonHelper,
        botProperties.getAdminUser());
  }
}
