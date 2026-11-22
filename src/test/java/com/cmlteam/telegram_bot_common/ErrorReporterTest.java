package com.cmlteam.telegram_bot_common;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErrorReporterTest {
  private final TelegramBot telegramBot = mock(TelegramBot.class);
  private final ErrorReporter reporter =
      new ErrorReporter(telegramBot, new JsonHelper(), List.of(1L));

  @BeforeEach
  void stubSuccessfulSend() {
    SendResponse response = mock(SendResponse.class);
    when(response.isOk()).thenReturn(true);
    when(telegramBot.execute(any(SendMessage.class))).thenReturn(response);
  }

  @Test
  void summarizesRetryAfterErrorsAndKeepsOtherDetails() {
    reporter.reportError(
        new ErrorData(null, 429, "Too Many Requests: retry after 5", "retry request", null));
    reporter.reportError(new ErrorData(null, 500, "Internal Server Error", "other request", null));
    reporter.reportError(new ErrorData(null, 429, "Too Many Requests: retry after 12", null, null));
    reporter.reportError(new ErrorData(null, 429, "Too Many Requests", null, null));

    reporter.reportJob();

    assertThat(sentMessage())
        .startsWith("Received 4 errors during last ")
        .containsOnlyOnce("\nfrom which 2 are 429 Too Many Requests: retry after X\n")
        .contains(
            "<b>Err #1</b> 500 Internal Server Error\n<b>TG req</b><pre>other request</pre>",
            "<b>Err #2</b> 429 Too Many Requests\n")
        .doesNotContain("retry after 5", "retry after 12", "retry request");
  }

  @Test
  void countsRetriesBeyondTheDetailLimit() {
    for (int i = 0; i < 20; i++) {
      reporter.reportError(
          new ErrorData(null, 500, "Internal Server Error", "x".repeat(300), null));
    }
    reporter.reportError(new ErrorData(null, 429, "Too Many Requests: retry after 5", null, null));

    reporter.reportJob();

    String message = sentMessage();
    assertThat(message)
        .startsWith("Received 21 errors during last ")
        .containsOnlyOnce("\nfrom which 1 are 429 Too Many Requests: retry after X\n")
        .doesNotContain("<b>Err #20</b>");
    assertThat(message.length()).isLessThanOrEqualTo(4096);
  }

  private String sentMessage() {
    ArgumentCaptor<SendMessage> request = ArgumentCaptor.forClass(SendMessage.class);
    verify(telegramBot).execute(request.capture());
    return (String) request.getValue().getParameters().get("text");
  }
}
