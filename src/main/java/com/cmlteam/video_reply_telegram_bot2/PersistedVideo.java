package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.model.Video;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersistedVideo {
  @Id private String id;
  private String fileId;
  private String fileUniqueId;
  private int messageId;
  private List<String> keywords;

  public PersistedVideo(Video video, int messageId) {
    this(null, video.fileId(), video.fileUniqueId(), messageId, List.of());
  }

  public boolean matches(SearchStringMatcher searchStringMatcher, @NonNull String query) {
    return keywords.stream().anyMatch(s -> searchStringMatcher.matches(s, query));
  }
}
