package com.cmlteam.video_reply_telegram_bot2.imp;

import com.cmlteam.video_reply_telegram_bot2.AdminUserChecker;
import com.cmlteam.video_reply_telegram_bot2.PersistedVideo;
import com.cmlteam.video_reply_telegram_bot2.PersistedVideoRepository;
import com.cmlteam.video_reply_telegram_bot2.SearchStringMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideosListImporter {
  private final VideosListProperties videosListProperties;
  private final SearchStringMatcher searchStringMatcher;
  private final PersistedVideoRepository persistedVideoRepository;
  private final AdminUserChecker adminUserChecker;

  @PostConstruct
  public void postConstruct() {
    log.info("Videos: " + videosListProperties.getList().size());
    runImport();
  }

  void runImport() {
    log.info("Start importing videos.");

    int i = 0;

    for (Video video : videosListProperties.getList()) {

      if (i++ % 10 == 0) {
        log.info("Processed {}...", i);
      }

      Optional<PersistedVideo> persistedVideoOpt =
          persistedVideoRepository.getFirstByFileUniqueId(video.getFileUniqueId());
      persistedVideoOpt.ifPresent(persistedVideoRepository::delete);

      PersistedVideo persistedVideo = new PersistedVideo();
      persistedVideo.setCreateDate(Instant.now());
      persistedVideo.setFileId(video.getFileId());
      persistedVideo.setFileUniqueId(video.getFileUniqueId());
      persistedVideo.setKeywords(video.getKeywords());
      persistedVideo.setUserId((int) adminUserChecker.getAdminUser());

      persistedVideoRepository.save(persistedVideo);
    }

    log.info("Done importing {} videos!", i);
  }
}
