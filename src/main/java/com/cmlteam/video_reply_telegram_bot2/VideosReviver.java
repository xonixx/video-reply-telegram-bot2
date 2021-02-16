package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.telegram_bot_common.TelegramBotWrapper;
import com.cmlteam.util.Util;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VideosReviver {
  private final String backupFolder;
  private final TelegramBotWrapper telegramBot;
  private final VideosService videosService;

  @PostConstruct
  void postConstruct() {
    if (!new java.io.File(backupFolder).isDirectory()) {
      throw new IllegalArgumentException("Not a folder: " + backupFolder);
    }
  }

  @Async
  public void revive(long userToInform) {
    List<PersistedVideo> persistedVideos = videosService.getAllVideos();

    int total = persistedVideos.size();

    telegramBot.execute(
        new SendMessage(userToInform, "Starting revive for " + total + " videos..."));

    long t0 = System.currentTimeMillis();

    int revivedVideosCnt = 0;

    try {
      for (PersistedVideo persistedVideo : persistedVideos) {
        boolean isRevived = reviveVid(userToInform, persistedVideo);
        if (isRevived) {
          revivedVideosCnt++;
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      telegramBot.execute(new SendMessage(userToInform, "Exception: " + ex.toString()));
    }

    telegramBot.sendMarkdownV2(
        userToInform,
        "Revived "
            + revivedVideosCnt
            + " dead out of total "
            + total
            + " videos in "
            + Util.renderDurationFromStart(t0));
  }

  @SneakyThrows
  private boolean reviveVid(long userToInform, PersistedVideo persistedVideo) {
    GetFileResponse fileResponse = telegramBot.execute(new GetFile(persistedVideo.getFileId()));

    if (!fileResponse.isOk()) {
      telegramBot.sendText(
          userToInform,
          "Video "
              + persistedVideo.getFileUniqueId()
              + " with keywords '"
              + persistedVideo.getKeywords()
              + "' is dead :(");
      return true;
    }

    //    File file = fileResponse.file();
    //
    //    String filePath = file.filePath();
    //
    //    String videoUrl = formFileDlUrl(filePath);

    return false;
  }
}
