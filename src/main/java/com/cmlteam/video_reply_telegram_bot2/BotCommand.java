package com.cmlteam.video_reply_telegram_bot2;

import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum BotCommand {
  START("start", false),
  DELETE("delete", false),
  BACKUP("backup", true),
  REVIVE("revive", true);

  private final String cmd;
  private final boolean isAdminCommand;

  boolean is(String commandCandidate) {
    return ("/" + cmd).equals(commandCandidate);
  }

  boolean isAdminCommand() {
    return isAdminCommand;
  }

  static boolean isAdminCommand(String commandCandidate) {
    return Stream.of(values())
        .anyMatch(botCommand -> botCommand.is(commandCandidate) && botCommand.isAdminCommand());
  }
}
