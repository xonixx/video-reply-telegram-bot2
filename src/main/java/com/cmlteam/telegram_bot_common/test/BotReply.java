package com.cmlteam.telegram_bot_common.test;

import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.List;

public class BotReply {
  private final List<BaseRequest> botReplies = new ArrayList<>();

  void add(BaseRequest request) {
    botReplies.add(request);
  }

  public int count() {
    return botReplies.size();
  }

  public String getText() {
    return getText(0);
  }

  public String getText(int idx) {
    return getText(botReplies.get(idx));
  }

  private String getText(BaseRequest request) {
    if (request instanceof SendMessage) {
      SendMessage sendMessage = (SendMessage) request;
      return (String) sendMessage.getParameters().get("text");
    }
    return null;
  }
}
