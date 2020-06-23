package com.cmlteam.video_reply_telegram_bot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "telegram-bot")
@Validated
@Getter
@Setter
public class BotProperties {
  private @Positive long adminUser;
  private @NotBlank String token;
  private @NotBlank String backupFolder;
}
