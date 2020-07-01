package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TelegramBotWrapper {
  private final TelegramBot telegramBot;
  private final JsonHelper jsonHelper;
  private final ErrorReporter errorReporter;

  public <T extends BaseRequest, R extends BaseResponse> R executeEx(BaseRequest<T, R> request) {
    return checkErrorResp(execute(request));
  }

  public <T extends BaseRequest, R extends BaseResponse> R executeEx(
      Update userRequest, BaseRequest<T, R> request) {
    return checkErrorResp(execute(userRequest, request));
  }

  private <R extends BaseResponse> R checkErrorResp(R res) {
    if (!res.isOk()) {
      throw new RuntimeException(res.errorCode() + " " + res.description());
    }
    return res;
  }

  public <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
    return execute(null, request);
  }

  public <T extends BaseRequest, R extends BaseResponse> R execute(
      Update userRequest, BaseRequest<T, R> request) {
    R response = telegramBot.execute(request);
    if (!response.isOk()) {
      String requestStr = jsonHelper.toPrettyString(request.toWebhookResponse());
      errorReporter.reportError(
          new ErrorData(
              userRequest,
              response.errorCode(),
              response.description(),
              requestStr,
              new Exception()));
      log.error(
          "ERROR #{}: {} for request:\n{}",
          response.errorCode(),
          response.description(),
          requestStr);
    }
    return response;
  }

  public void sendMarkdownV2(long chatId, String text) {
    execute(
        new SendMessage(chatId, text.replaceAll("([.!])", "\\\\$1"))
            .parseMode(ParseMode.MarkdownV2));
  }

  public void sendText(long chatId, String text) {
    execute(new SendMessage(chatId, text));
  }
}
