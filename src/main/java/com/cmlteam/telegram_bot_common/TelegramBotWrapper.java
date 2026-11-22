package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TelegramBotWrapper extends TelegramSenderBase {
  private final TelegramBot telegramBot;
  private final JsonHelper jsonHelper;
  private final ErrorReporter errorReporter;

  @Override
  public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(
      Update userRequest, BaseRequest<T, R> request) {
    R response = telegramBot.execute(request);
    if (!response.isOk()) {
      String requestStr = jsonHelper.toPrettyString(request.toWebhookResponse());
      String description = response.description();
      int errorCode = response.errorCode();
      log.error("ERROR #{}: {} for request:\n{}", errorCode, description, requestStr);
      if (!(errorCode == 502 && "Bad Gateway".equals(description)))
        errorReporter.reportError(
            new ErrorData(userRequest, errorCode, description, requestStr, new Exception()));
    }
    return response;
  }
}
