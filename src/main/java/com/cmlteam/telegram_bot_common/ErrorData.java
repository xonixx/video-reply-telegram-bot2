package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.model.Update;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class ErrorData {
  private final Update userRequest;
  private final int errorCode;
  private final String description;
  private final String request;
  private final Exception exception;
}
