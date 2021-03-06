package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.telegram_bot_common.TelegramBotWrapper;
import com.cmlteam.util.Util;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VideosBackupper {
  private static final int TIMEOUT = 10_000;
  private final String backupFolder;
  private final String token;
  private final TelegramBotWrapper telegramBot;
  private final VideosService videosService;
  private final PersistedVideoRepository persistedVideoRepository;

  @PostConstruct
  void postConstruct() {
    if (!new java.io.File(backupFolder).isDirectory()) {
      throw new IllegalArgumentException("Not a folder: " + backupFolder);
    }
  }

  @RequiredArgsConstructor(staticName = "of")
  private static class ErrVid {
    final PersistedVideo persistedVideo;
    final Exception exception;
  }

  @Async
  public void startBackup(long userToInform) {
    List<PersistedVideo> persistedVideos = videosService.getAllVideos();

    int total = persistedVideos.size();

    telegramBot.execute(
        new SendMessage(userToInform, "Starting backup for " + total + " videos..."));

    long t0 = System.currentTimeMillis();

    int newVideosCnt = 0;
    List<ErrVid> errVideos = new ArrayList<>();

    try {
      for (PersistedVideo persistedVideo : persistedVideos) {
        try {
          boolean isNew = backupVideo(persistedVideo);
          if (isNew) {
            newVideosCnt++;
          }
        } catch (Exception ex) {
          log.error("", ex);
          errVideos.add(ErrVid.of(persistedVideo, ex));
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      telegramBot.execute(new SendMessage(userToInform, "Exception: " + ex.toString()));
    }

    telegramBot.sendMarkdownV2(
        userToInform,
        "Downloaded "
            + newVideosCnt
            + " new out of total "
            + total
            + " videos in "
            + Util.renderDurationFromStart(t0)
            + (errVideos.isEmpty()
                ? ""
                : ". **" + errVideos.size() + " videos failed to download!**"));
    if (!errVideos.isEmpty()) {
      int i = 0;
      for (ErrVid errVideo : errVideos) {
        PersistedVideo persistedVideo = errVideo.persistedVideo;
        telegramBot.execute(
            new SendVideo(userToInform, persistedVideo.getFileId())
                .caption("Err video #" + (++i) + ": " + persistedVideo.getKeywordsString()));
        telegramBot.sendText(userToInform, "Err: " + errVideo.exception.getMessage());
      }
    }
  }

  @SneakyThrows
  private boolean backupVideo(PersistedVideo persistedVideo) {
    java.io.File fileDestination = new java.io.File(backupFolder, persistedVideo.getId() + ".mp4");

    boolean result;

    if (fileDestination.exists()) {
      log.info("EXISTING");
      result = false;
    } else {
      log.info("NEW");
      GetFileResponse fileResponse = telegramBot.executeEx(new GetFile(persistedVideo.getFileId()));

      File file = fileResponse.file();

      String filePath = file.filePath();

      String videoUrl = formFileDlUrl(filePath);

      log.info("Downloading {} : {}... ", persistedVideo.getFileId(), videoUrl);
      FileUtils.copyURLToFile(new URL(videoUrl), fileDestination, TIMEOUT, TIMEOUT);
      result = true;
    }

    if (persistedVideo.getSize() == 0) {
      persistedVideo.setSize((int) fileDestination.length());
      persistedVideoRepository.save(persistedVideo);
    }

    return result;
  }

  private String formFileDlUrl(String filePath) {
    return String.format("https://api.telegram.org/file/bot%s/%s", token, filePath);
  }
}
