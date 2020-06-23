package com.cmlteam.video_reply_telegram_bot2;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideosListService {
  private final SearchStringMatcher searchStringMatcher;

  public static final int MAX_ALLOWED_INLINE_RESULTS = 50;

  /**
   * @param query user query string
   * @param offset offset identifier for pagination
   * @return list of file_ids of videos stored in telegram
   */
  VideosPage searchVideo(@NonNull String query, @NonNull String offset) {
    List<Video> matchedResults = List.of(); //
    //        videosListProperties.getList().stream()
    //            .filter(v -> v.matches(searchStringMatcher, query))
    //            .collect(Collectors.toList());

    return VideosPage.of(matchedResults, MAX_ALLOWED_INLINE_RESULTS, offset);
  }

  @PostConstruct
  public void postConstruct() {
    log.info("Videos: " + getList().size());
  }

  public List<Video> getList() {
    throw new UnsupportedOperationException("TBD");
  }
}
