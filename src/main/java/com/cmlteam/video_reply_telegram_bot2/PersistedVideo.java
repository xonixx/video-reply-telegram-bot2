package com.cmlteam.video_reply_telegram_bot2;

import com.pengrad.telegrambot.model.Video;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "video")
public class PersistedVideo {
  @Id private String id;
  private String fileId;
  private String fileUniqueId;
  private String youtubeId;
  private int userId;
  private int messageId;
  private int size; // size in bytes
  private List<String> keywords;
  @CreatedDate private Instant createDate;
  @LastModifiedDate private Instant lastModifiedDate;

  public PersistedVideo(Video video, int userId, int messageId) {
    this(
        null,
        video.fileId(),
        video.fileUniqueId(),
        null,
        userId,
        messageId,
        video.fileSize(),
        List.of(),
        Instant.now(),
        Instant.now());
  }

  public boolean matches(SearchStringMatcher searchStringMatcher, @NonNull String query) {
    return keywords.stream().anyMatch(s -> searchStringMatcher.matches(s, query));
  }

  public String getKeywordsString() {
    return String.join("; ", getKeywords());
  }
}
