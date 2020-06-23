package com.cmlteam.video_reply_telegram_bot2;

import com.cmlteam.util.Util;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VideosBackupper {
  private static final int TIMEOUT = 10_000;
  private final String backupFolder;
  private final String token;
  private final TelegramBotWrapper telegramBot;
  private final VideosListProperties videosListProperties;

  @PostConstruct
  void postConstruct() {
    if (!new java.io.File(backupFolder).isDirectory()) {
      throw new IllegalArgumentException("Not a folder: " + backupFolder);
    }
  }

  @Async
  public void startBackup(long userToInform) {
    List<Video> videos = videosListProperties.getList();

    int total = videos.size();

    telegramBot.execute(
        new SendMessage(userToInform, "Starting backup for " + total + " videos..."));

    long t0 = System.currentTimeMillis();

    int newVideosCnt = 0;

    try {
      for (Video video : videos) {
        boolean isNew = backupVideo(video);
        if (isNew) {
          newVideosCnt++;
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      telegramBot.execute(new SendMessage(userToInform, "Exception: " + ex.toString()));
    }

    telegramBot.execute(
        new SendMessage(
            userToInform,
            "Downloaded "
                + newVideosCnt
                + " new out of total "
                + total
                + " videos in "
                + Util.renderDurationFromStart(t0)));
  }

  @SneakyThrows
  private boolean backupVideo(Video video) {
    GetFileResponse fileResponse = telegramBot.executeEx(new GetFile(video.getFileId()));

    File file = fileResponse.file();

    String filePath = file.filePath();

    String videoUrl = formFileDlUrl(filePath);

    log.info("Downloading {} : {}... ", video.getFileId(), videoUrl);

    java.io.File fileDestination = new java.io.File(backupFolder, video.getFileUniqueId() + ".mp4");

    if (fileDestination.exists()) {
      log.info("EXISTING");
      return false;
    } else {
      log.info("NEW");
      FileUtils.copyURLToFile(new URL(videoUrl), fileDestination, TIMEOUT, TIMEOUT);
      return true;
    }
  }

  private String formFileDlUrl(String filePath) {
    return String.format("https://api.telegram.org/file/bot%s/%s", token, filePath);
  }
}
