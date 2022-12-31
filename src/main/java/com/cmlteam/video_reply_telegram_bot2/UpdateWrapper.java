package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.RequiredArgsConstructor;

/** TODO move to tg-bot-common */
@RequiredArgsConstructor
public class UpdateWrapper {
  private final Update update;

  /**
   * @return user or null
   */
  public User getUser() {
    if (update == null) {
      return null;
    }

    Message message = update.message();
    User user = null;
    if (message != null) {
      user = message.from();
    } else {
      InlineQuery inlineQuery;
      if ((inlineQuery = update.inlineQuery()) != null) {
        user = inlineQuery.from();
      }
    }

    return user;
  }
}
