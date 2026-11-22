package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LogHelper {
  public void captureLogParams(Update update) {
    MDC.clear();

    if (update == null) {
      return;
    }
    Message message = update.message();
    InlineQuery inlineQuery;
    User user = null;
    if (message != null) {
      user = message.from();
    } else if ((inlineQuery = update.inlineQuery()) != null) {
      user = inlineQuery.from();
    }

    if (user != null) {
      MDC.put("username", user.username());
    }
  }
}
