package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.telegram_bot_common.Emoji;
import com.cmlteam.telegram_bot_common.LogHelper;
import com.cmlteam.telegram_bot_common.TelegramBotWrapper;
import com.cmlteam.telegram_bot_common.JsonHelper;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedVideo;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class BotPollingJob {
  private final TelegramBotWrapper telegramBot;
  private final VideosService videosService;
  private final VideosBackupper videosBackupper;
  private final JsonHelper jsonHelper;
  private final LogHelper logHelper;
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
      logHelper.captureLogParams(update);

      log.info("Received:\n" + jsonHelper.toPrettyString(update));

      Message message = update.message();

      if (message != null) {
        Long chatId = message.chat().id();
        Integer messageId = message.messageId();

        String text = message.text();
        Video video = message.video();
        User user = message.from();
        Integer userId = user.id();
        Message replyToMessage = message.replyToMessage();
        Video replyToVideo = replyToMessage == null ? null : replyToMessage.video();

        if (BotCommand.START.is(text)) {
          telegramBot.sendText(chatId, "Please start from uploading video");
        } else if (BotCommand.DELETE.is(text)) {
          if (replyToVideo != null) {
            Optional<PersistedVideo> storedVideo =
                videosService.getStoredVideo(userId, replyToVideo.fileUniqueId());

            if (storedVideo.isPresent()) {
              videosService.deleteVideo(storedVideo.get().getId());
              telegramBot.sendText(
                  chatId, Emoji.SUCCESS.msg("The video has been removed successfully!"));
            } else {
              telegramBot.sendText(
                  chatId,
                  Emoji.ERROR.msg("The video doesn't exist or you don't have access to it!"));
            }
          } else {
            telegramBot.sendText(
                chatId,
                "To use this command - reply to your own video in this chat that you want to delete with the /delete command");
          }
        } else if (video != null) {
          if (!"video/mp4".equals(video.mimeType())) {
            telegramBot.sendText(
                chatId,
                Emoji.WARN.msg(
                    "Sorry, only .MP4 videos are supported! "
                        + "Please try again with other file."));
          } else {
            Optional<PersistedVideo> storedVideo =
                videosService.getStoredVideo(video.fileUniqueId());
            if (storedVideo.isPresent()) {
              telegramBot.sendText(
                  chatId,
                  Emoji.WARN.msg(
                      " The same video already exists with keywords: "
                          + String.join("; ", storedVideo.get().getKeywords())));
            } else {
              PersistedVideo persistedVideo = new PersistedVideo(video, userId, messageId);
              videosService.store(persistedVideo);
              telegramBot.sendMarkdownV2(
                  chatId,
                  Emoji.SUCCESS.msg(
                      "Ok received video! "
                          + "Please add keywords for it. To do this please *REPLY* to your own video with a text. "
                          + "Use \";\" as separator. Only the first string before \";\" will show as title."));
            }
          }
        } else if (StringUtils.isNotBlank(text)) {
          if (replyToVideo != null) {
            List<String> keywords =
                Stream.of(text.split(";")).map(String::trim).collect(Collectors.toList());
            videosService
                .getStoredVideo(userId, replyToVideo.fileUniqueId())
                .ifPresentOrElse(
                    persistedVideo -> {
                      List<String> prevKeywords = persistedVideo.getKeywords();
                      persistedVideo.setKeywords(keywords);
                      videosService.store(persistedVideo);
                      telegramBot.sendText(
                          chatId,
                          Emoji.SUCCESS.msg(
                              "Cool, the keywords "
                                  + (prevKeywords.isEmpty() ? "saved" : "updated")
                                  + ". Video is ready for inline search!"));
                    },
                    () ->
                        telegramBot.sendText(
                            chatId,
                            Emoji.ERROR.msg(
                                "The video you are trying to set keywords for doesn't exist or you don't have access to it!")));
          } else if (!isAdminUser(user) || !BotCommand.isAdminCommand(text)) {
            telegramBot.sendMarkdownV2(
                chatId,
                Emoji.WARN.msg(
                    ("If you want to update keywords please *REPLY* to your own video with a text. "
                        + "Use \";\" as separator. Only the first string before \";\" will show as title.")));
          }
        } else {
          telegramBot.sendText(
              chatId,
              Emoji.WARN.msg(
                  "The uploaded document doesn't look like .MP4 video. "
                      + "Please try again with other file."));
        }

        if (isAdminUser(user)) {
          if (BotCommand.BACKUP.is(text)) {
            videosBackupper.startBackup(adminUser);
          }
        } else {
          forwardMessageToAdmin(messageId, chatId);
        }
      }

      InlineQuery inlineQuery = update.inlineQuery();

      if (inlineQuery != null) {
        String query = inlineQuery.query();
        String offset = inlineQuery.offset();

        VideosPage videosPage = videosService.searchVideo(inlineQuery.from().id(), query, offset);

        //        log.info("offset: {}, nextOffset: {}", offset, videosPage.getNextOffset());

        List<InlineQueryResultCachedVideo> results =
            new ArrayList<>(videosPage.getPersistedVideos().size());
        for (PersistedVideo v : videosPage.getPersistedVideos()) {
          results.add(
              new InlineQueryResultCachedVideo(
                  v.getFileUniqueId(), v.getFileId(), v.getKeywords().get(0)));
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

  private boolean isAdminUser(User user) {
    return adminUser == user.id().longValue();
  }
}
