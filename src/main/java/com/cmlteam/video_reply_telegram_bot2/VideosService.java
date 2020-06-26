package com.cmlteam.video_reply_telegram_bot2;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideosService {
  private final PersistedVideoRepository persistedVideoRepository;
  private final SearchStringMatcher searchStringMatcher;

  public static final int MAX_ALLOWED_INLINE_RESULTS = 50;

  /**
   * @param query user query string
   * @param offset offset identifier for pagination
   * @return list of file_ids of videos stored in telegram
   */
  VideosPage searchVideo(@NonNull String query, @NonNull String offset) {
    List<PersistedVideo> matchedResults =
        getAllVideos().stream()
            .filter(v -> v.matches(searchStringMatcher, query))
            .collect(Collectors.toList());

    return VideosPage.of(matchedResults, MAX_ALLOWED_INLINE_RESULTS, offset);
  }

  @PostConstruct
  public void postConstruct() {
    log.info("Videos: " + getAllVideos().size());
  }

  // TODO make this more efficient than this
  public List<PersistedVideo> getAllVideos() {
    return persistedVideoRepository.findAll();
  }

  public void store(PersistedVideo persistedVideo) {
    persistedVideoRepository.save(persistedVideo);
  }

  public Optional<PersistedVideo> getLastUploadedVideo(int userId) {
    return persistedVideoRepository.getFirstByUserIdOrderByLastModifiedDateDesc(userId);
  }

  public Optional<PersistedVideo> getStoredVideo(int userId, int messageId) {
    return persistedVideoRepository.getFirstByUserIdAndMessageId(userId, messageId);
  }

  public void deleteVideo(String id) {
    persistedVideoRepository.deleteById(id);
  }
}
