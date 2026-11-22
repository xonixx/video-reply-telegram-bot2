package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;

public abstract class TelegramSenderBase implements TelegramSender {
  @Override
  public <T extends BaseRequest<T, R>, R extends BaseResponse> R executeEx(
      BaseRequest<T, R> request) {
    return checkErrorResp(execute(request));
  }

  @Override
  public <T extends BaseRequest<T, R>, R extends BaseResponse> R executeEx(
      Update userRequest, BaseRequest<T, R> request) {
    return checkErrorResp(execute(userRequest, request));
  }

  private static <R extends BaseResponse> R checkErrorResp(R res) {
    if (!res.isOk()) {
      throw new RuntimeException(res.errorCode() + " " + res.description());
    }
    return res;
  }

  @Override
  public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(
      BaseRequest<T, R> request) {
    return execute(null, request);
  }

  @Override
  public abstract <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(
      Update userRequest, BaseRequest<T, R> request);

  public void sendMarkdownV2(long chatId, String text) {
    execute(
        new SendMessage(chatId, text.replaceAll("([.!])", "\\\\$1"))
            .parseMode(ParseMode.MarkdownV2));
  }

  @Override
  public void sendText(long chatId, String text) {
    execute(new SendMessage(chatId, text));
  }
}
