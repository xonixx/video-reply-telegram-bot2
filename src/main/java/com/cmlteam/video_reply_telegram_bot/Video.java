package com.cmlteam.video_reply_telegram_bot;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Video {
  private String fileId;
  private String fileUniqueId;
  private List<String> keywords;

  public boolean matches(SearchStringMatcher searchStringMatcher, @NonNull String query) {
    return keywords.stream().anyMatch(s -> searchStringMatcher.matches(s, query));
  }
}
