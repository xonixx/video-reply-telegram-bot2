package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.model.Update;

public interface BotUpdateHandler {
  void processUpdate(TelegramSender telegramSender, Update update);
}
