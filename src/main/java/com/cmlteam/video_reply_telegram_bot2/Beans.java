package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.telegram_bot_common.ErrorReporter;
import com.cmlteam.telegram_bot_common.JsonHelper;
import com.cmlteam.telegram_bot_common.LogHelper;
import com.cmlteam.telegram_bot_common.TelegramBotWrapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    return new ErrorReporter(telegramBot, jsonHelper, List.of(botProperties.getAdminUser()));
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
      VideosService videosService) {
    return new VideosBackupper(
        botProperties.getBackupFolder(),
        botProperties.getToken(),
        telegramBotWrapper,
        videosService);
  }

  @Bean
  public BotPollingJob botPollingJob(
      BotProperties botProperties,
      TelegramBotWrapper telegramBotWrapper,
      VideosService videosService,
      VideosBackupper videosBackupper,
      JsonHelper jsonHelper,
      LogHelper logHelper) {
    return new BotPollingJob(
        telegramBotWrapper, videosService, videosBackupper, jsonHelper, logHelper, botProperties);
  }

  @Bean
  public VideosService videosService(
      BotProperties botProperties,
      PersistedVideoRepository persistedVideoRepository,
      SearchStringMatcher searchStringMatcher) {
    return new VideosService(botProperties, persistedVideoRepository, searchStringMatcher);
  }
}
