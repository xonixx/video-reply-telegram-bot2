package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedVideo;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@RequiredArgsConstructor
@Slf4j
public class BotPollingJob {
  private final TelegramBotWrapper telegramBot;
  private final VideosService videosService;
  private final VideosBackupper videosBackupper;
  private final JsonHelper jsonHelper;
  private final long adminUser;

  private final GetUpdates getUpdates = new GetUpdates();

  @Scheduled(fixedRate = 400)
  public void processUpdates() {
    GetUpdatesResponse updatesResponse = telegramBot.execute(getUpdates);

    if (!updatesResponse.isOk()) {
      return;
    }

    List<Update> updates = updatesResponse.updates();

    for (Update update : updates) {
      log.info("Received:\n" + jsonHelper.toPrettyString(update));

      Message message = update.message();
      //      telegramBot.execute(new SendMessage(message.chat().id(), "" + update.updateId()));

      if (message != null) {
        Long chatId = message.chat().id();
        Integer messageId = message.messageId();

        String text = message.text();
        Video video = message.video();

        if ("/start".equals(text)) {
          telegramBot.execute(new SendMessage(chatId, "Please start from uploading video"));
        } else if (video != null) {
          PersistedVideo persistedVideo = new PersistedVideo(video, messageId);
          videosService.store(persistedVideo);
        }

        if (isAdminUser(message.from())) {
          if ("/backup".equals(text)) {
            videosBackupper.startBackup(adminUser);
          } /*else {
              if (video != null) {
                displayVideoFileIds(chatId, video, messageId);
              }
            }*/
        } else {
          forwardMessageToAdmin(messageId, chatId);
        }
      }

      InlineQuery inlineQuery = update.inlineQuery();

      if (inlineQuery != null) {
        String query = inlineQuery.query();
        String offset = inlineQuery.offset();

        VideosPage videosPage = videosService.searchVideo(query, offset);

        //        log.info("offset: {}, nextOffset: {}", offset, videosPage.getNextOffset());

        List<InlineQueryResultCachedVideo> results =
            new ArrayList<>(videosPage.getPersistedVideos().size());
        ListIterator<PersistedVideo> it = videosPage.getPersistedVideos().listIterator();
        while (it.hasNext()) {
          PersistedVideo v = it.next();
          results.add(
              new InlineQueryResultCachedVideo(
                  v.getFileUniqueId(),
                  v.getFileId(),
                  //                  (isAdminUser(inlineQuery.from())
                  //                          ? (videosPage.getOffsetIdx() + it.nextIndex()) + ". "
                  //                          : "") // enumerate videos for admin
                  //                      +
                  v.getKeywords().get(0)));
        }

        telegramBot.execute(
            update,
            new AnswerInlineQuery(inlineQuery.id(), results.toArray(new InlineQueryResult[0]))
                .nextOffset(videosPage.getNextOffset()));
      }

      getUpdates.offset(update.updateId() + 1);
    }
  }

  private void forwardMessageToAdmin(Integer messageId, Long chatId) {
    telegramBot.execute(new ForwardMessage(adminUser, chatId, messageId));
  }

  private boolean isAdminUser(Long chatId) {
    return adminUser == chatId;
  }

  private boolean isAdminUser(User user) {
    return isAdminUser(user.id().longValue());
  }

  private void displayVideoFileIds(Long chatId, Video video, Integer messageId) {
    String fileId = video.fileId();
    String fileUniqueId = video.fileUniqueId();
    //        telegramBot.execute(new SendVideo(message.chat().id(), fileId).caption(fileId));
    telegramBot.execute(
        new SendMessage(
            chatId,
            "file-id: \""
                + fileId
                + "\"\nfile-unique-id: \""
                + fileUniqueId
                + "\"\nmessage-id: \""
                + messageId
                + "\""));
  }
}
