package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;

public interface TelegramSender {
  <T extends BaseRequest<T, R>, R extends BaseResponse> R executeEx(BaseRequest<T, R> request);

  <T extends BaseRequest<T, R>, R extends BaseResponse> R executeEx(
      Update userRequest, BaseRequest<T, R> request);

  <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request);

  <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(
      Update userRequest, BaseRequest<T, R> request);

  void sendMarkdownV2(long chatId, String text);

  void sendText(long chatId, String text);
}
