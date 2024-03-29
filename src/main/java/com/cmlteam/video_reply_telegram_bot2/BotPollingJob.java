package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.telegram_bot_common.Emoji;
import com.cmlteam.telegram_bot_common.JsonHelper;
import com.cmlteam.telegram_bot_common.LogHelper;
import com.cmlteam.telegram_bot_common.TelegramBotWrapper;
import com.cmlteam.util.Util;
import com.cmlteam.video_reply_telegram_bot2.stat.StatCollector;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedVideo;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.sapher.youtubedl.YoutubeDLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Slf4j
public class BotPollingJob {
  private final TelegramBotWrapper telegramBot;
  private final VideosService videosService;
  private final VideosBackupper videosBackupper;
  private final VideosReviver videosReviver;
  private final JsonHelper jsonHelper;
  private final LogHelper logHelper;
  private final AdminUserChecker adminUserChecker;
  private final int maxFileSize;
  private final YoutubeDownloader youtubeDownloader;

  private final StatCollector statCollector;
  private final StatFormer statFormer;

  private final GetUpdates getUpdates = new GetUpdates();

  @Scheduled(fixedRate = 400)
  public void processUpdates() {
    GetUpdatesResponse updatesResponse = telegramBot.execute(getUpdates);

    if (!updatesResponse.isOk()) {
      return;
    }

    List<Update> updates = updatesResponse.updates();

    for (Update update : updates) {
      try {
        logHelper.captureLogParams(update);

        log.info("Received:\n" + jsonHelper.toPrettyString(update));

        trackUser(update);

        Message message = update.message();

        if (message != null) {
          Long chatId = message.chat().id();
          Integer messageId = message.messageId();

          String text = message.text();
          Video video = message.video();
          User user = message.from();
          Long userId = user.id();
          Message replyToMessage = message.replyToMessage();
          Video replyToVideo = replyToMessage == null ? null : replyToMessage.video();

          if (BotCommand.START.is(text)) {
            telegramBot.sendText(
                chatId,
                "This is inline bot to allow reply with video-meme!\n"
                    + "More instructions: https://github.com/xonixx/video-reply-telegram-bot2/blob/master/README.md");
          } else if (BotCommand.DELETE.is(text)) {
            handleDeleteVideo(chatId, userId, replyToVideo);
          } else if (video != null) {
            if (replyToVideo != null) {
              handleReplaceVideo(chatId, userId, messageId, replyToVideo, video);
            } else {
              handleUploadVideo(chatId, userId, messageId, video);
            }
          } else if (StringUtils.isNotBlank(text)) {
            if (youtubeDownloader.isYoutubeLink(text)) {
              handleYoutubeLink(chatId, userId, text);
            } else {
              handleSetKeywords(chatId, userId, replyToVideo, text);
            }
          } else {
            telegramBot.sendText(
                chatId,
                Emoji.WARN.msg(
                    "The uploaded document doesn't look like .MP4 video. "
                        + "Please try again with other file."));
          }

          if (adminUserChecker.isAdmin(user)) {
            if (BotCommand.BACKUP.is(text)) {
              videosBackupper.startBackup(userId);
            } else if (BotCommand.REVIVE.is(text)) {
              videosReviver.revive(userId);
            } else if (BotCommand.STAT.is(text)) {
              // TODO can we make time interval configurable
              telegramBot.sendMarkdownV2(
                  chatId,
                  statFormer.formStatMarkdown(statCollector.reportStat(Duration.ofDays(30))));
            }
          } else {
            forwardMessageToAdmin(messageId, chatId);
          }
        }

        InlineQuery inlineQuery = update.inlineQuery();

        if (inlineQuery != null) {
          handleInlineQuery(update, inlineQuery);
        }
      } catch (Exception ex) {
        log.error("Unhandled exception", ex);
        handleUnhandledException(update, ex);
      }

      getUpdates.offset(update.updateId() + 1);
    }
  }

  private void handleUnhandledException(Update update, Exception ex) {
    try {
      UpdateWrapper updateWrapper = new UpdateWrapper(update);
      User user = updateWrapper.getUser();
      if (user != null) {
        String res =
            "<b>Exc</b> "
                + ex
                + "\n<pre>"
                + Util.trim(ExceptionUtils.getStackTrace(ex), 300)
                + "</pre>";
        Long chatId = update.message().chat().id();
        // notify admin
        telegramBot.execute(
            new SendMessage(adminUserChecker.getAdminUser(), Emoji.ERROR.msg(res))
                .parseMode(ParseMode.HTML));
        if (!adminUserChecker.isAdmin(user)) {
          telegramBot.sendText(
              chatId,
              Emoji.ERROR.msg(
                  "There was an unexpected error processing your request. Please retry later."));
        }
      }
    } catch (Exception ex1) {
      log.error("Unhandled exception #2", ex1);
    }
  }

  private void trackUser(Update update) {
    UpdateWrapper updateWrapper = new UpdateWrapper(update);
    User user = updateWrapper.getUser();
    if (user != null) {
      statCollector.track(user.username());
    }
  }

  private void handleInlineQuery(Update update, InlineQuery inlineQuery) {
    String query = inlineQuery.query();
    String offset = inlineQuery.offset();

    boolean isSize = false;
    if ("!size".equals(query) || query.startsWith("!size ")) {
      isSize = true;
      query = query.replaceFirst("^!size", "").trim();
    }

    VideosPage videosPage = videosService.searchVideo(inlineQuery.from().id(), query, offset);

    //        log.info("offset: {}, nextOffset: {}", offset, videosPage.getNextOffset());

    List<InlineQueryResultCachedVideo> results =
        new ArrayList<>(videosPage.getPersistedVideos().size());
    for (PersistedVideo v : videosPage.getPersistedVideos()) {
      results.add(
          new InlineQueryResultCachedVideo(
              v.getFileUniqueId(),
              v.getFileId(),
              (isSize ? "[" + Util.renderFileSize(v.getSize()) + "] " : "")
                  + v.getKeywords().get(0)));
    }

    telegramBot.execute(
        update,
        new AnswerInlineQuery(inlineQuery.id(), results.toArray(new InlineQueryResult[0]))
            .nextOffset(videosPage.getNextOffset()));
  }

  private void handleYoutubeLink(Long chatId, Long userId, String youtubeLink) {
    try {
      telegramBot.sendText(chatId, "Requesting Youtube...");
      YoutubeVideoInfo videoInfo = youtubeDownloader.getVideoInfo(youtubeLink);
      String youtubeId = videoInfo.getId();
      Optional<PersistedVideo> videoByYoutubeId =
          videosService.getStoredVideoByYoutubeId(youtubeId);
      if (videoByYoutubeId.isPresent()) {
        telegramBot.sendText(
            chatId,
            Emoji.WARN.msg(
                " The same Youtube video already exists with keywords: "
                    + videoByYoutubeId.get().getKeywordsString()));
      } else {
        Optional<YoutubeVideoFormat> appropriateFormatOptional = videoInfo.getAppropriateFormat();
        if (appropriateFormatOptional.isPresent()) {
          YoutubeVideoFormat youtubeVideoFormat = appropriateFormatOptional.get();
          if (checkFileSizeOrSendErrorMsg(chatId, youtubeVideoFormat.getFilesize())) {
            String title = videoInfo.getTitle();
            //                String url = youtubeVideoFormat.getUrl();
            //                log.info("YouTube URL: {}", url);
            youtubeDownloader.download(
                youtubeLink,
                (file, elapsedTime) -> {
                  SendResponse response =
                      telegramBot.execute(new SendVideo(chatId, file).caption(title));
                  log.info("Response:\n" + jsonHelper.toPrettyString(response));
                  if (response.isOk()) {
                    Message message = response.message();
                    handleUploadYoutubeVideo(
                        chatId, userId, message.messageId(), message.video(), title, youtubeId);
                  } else {
                    telegramBot.sendText(
                        chatId, Emoji.ERROR.msg("Failed uploading YouTube video to Telegram"));
                  }
                });
          }
        } else {
          telegramBot.sendText(chatId, Emoji.ERROR.msg("No appropriate video format!"));
        }
      }
    } catch (YoutubeDLException e) {
      telegramBot.sendText(chatId, Emoji.ERROR.msg(e.getMessage()));
    }
  }

  private void handleUploadVideo(Long chatId, Long userId, Integer messageId, Video video) {
    if (!"video/mp4".equals(video.mimeType())) {
      telegramBot.sendText(
          chatId,
          Emoji.WARN.msg(
              "Sorry, only .MP4 videos are supported! Please try again with other file."));
    } else if (checkFileSizeOrSendErrorMsg(chatId, video.fileSize())) {
      Optional<PersistedVideo> storedVideo = videosService.getStoredVideo(video.fileUniqueId());
      if (storedVideo.isPresent()) {
        telegramBot.sendText(
            chatId,
            Emoji.WARN.msg(
                " The same video already exists with keywords: "
                    + storedVideo.get().getKeywordsString()));
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
  }

  private void handleUploadYoutubeVideo(
      Long chatId, Long userId, Integer messageId, Video video, String title, String youtubeId) {
    PersistedVideo persistedVideo = new PersistedVideo(video, userId, messageId);
    persistedVideo.setYoutubeId(youtubeId);
    persistedVideo.setKeywords(List.of(title));
    videosService.store(persistedVideo);
    telegramBot.sendMarkdownV2(
        chatId,
        Emoji.SUCCESS.msg(
            "Ok received video! Video is ready for inline search! "
                + "If you want to update keywords please *REPLY* to your own video with a text. "
                + "Use \";\" as separator. Only the first string before \";\" will show as title."));
  }

  private boolean checkFileSizeOrSendErrorMsg(Long chatId, long fileSize) {
    if (fileSize > maxFileSize) {
      telegramBot.sendText(
          chatId,
          Emoji.WARN.msg(
              "Sorry, video is too big! Only videos up to "
                  + Util.humanReadableByteCount(maxFileSize, true)
                  + " are allowed, yours is "
                  + Util.humanReadableByteCount(fileSize, true)
                  + ". Please try again with other file."));
      return false;
    }

    return true;
  }

  private void handleReplaceVideo(
      Long chatId, Long userId, Integer messageId, Video replyToVideo, Video video) {
    videosService
        .getStoredVideo(userId, replyToVideo.fileUniqueId())
        .ifPresentOrElse(
            persistedVideo -> {
              persistedVideo.setFileId(video.fileId());
              persistedVideo.setFileUniqueId(video.fileUniqueId());
              persistedVideo.setMessageId(messageId);
              persistedVideo.setSize(video.fileSize());

              videosService.store(persistedVideo);

              telegramBot.sendMarkdownV2(
                  chatId,
                  Emoji.SUCCESS.msg(
                      "Cool, the video is replaced. _Please note, the inline search results can be still cached for some time!_"));
            },
            () ->
                telegramBot.sendText(
                    chatId,
                    Emoji.ERROR.msg(
                        "The video you are trying to replace doesn't exist or you don't have access to it!")));
  }

  private void handleSetKeywords(Long chatId, Long userId, Video replyToVideo, String text) {
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
    } else if (!adminUserChecker.isAdmin(userId) || !BotCommand.isAdminCommand(text)) {
      telegramBot.sendMarkdownV2(
          chatId,
          Emoji.WARN.msg(
              ("If you want to update keywords please *REPLY* to your own video with a text. "
                  + "Use \";\" as separator. Only the first string before \";\" will show as title.")));
    }
  }

  private void handleDeleteVideo(Long chatId, Long userId, Video replyToVideo) {
    if (replyToVideo != null) {
      Optional<PersistedVideo> storedVideo =
          videosService.getStoredVideo(userId, replyToVideo.fileUniqueId());

      if (storedVideo.isPresent()) {
        videosService.deleteVideo(storedVideo.get().getId());
        telegramBot.sendText(chatId, Emoji.SUCCESS.msg("The video has been removed successfully!"));
      } else {
        telegramBot.sendText(
            chatId, Emoji.ERROR.msg("The video doesn't exist or you don't have access to it!"));
      }
    } else {
      telegramBot.sendText(
          chatId,
          Emoji.WARN.msg(
              "To use this command - reply to your own video in this chat that you want to delete with the /delete command"));
    }
  }

  private void forwardMessageToAdmin(Integer messageId, Long chatId) {
    telegramBot.execute(new ForwardMessage(adminUserChecker.getAdminUser(), chatId, messageId));
  }
}
