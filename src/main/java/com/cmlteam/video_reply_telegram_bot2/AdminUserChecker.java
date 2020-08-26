package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.model.User;

public interface AdminUserChecker {
  boolean isAdmin(long userId);

  boolean isAdmin(User user);

  /** The primary admin user to get notified in case of errors, etc. */
  long getAdminUser();
}
