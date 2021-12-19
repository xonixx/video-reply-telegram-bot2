package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersistedVideoRepository extends MongoRepository<PersistedVideo, String> {
  Optional<PersistedVideo> getFirstByUserIdOrderByLastModifiedDateDesc(long userId);

  Optional<PersistedVideo> getFirstByUserIdAndMessageId(long userId, int messageId);

  Optional<PersistedVideo> getFirstByUserIdAndFileUniqueId(long userId, String fileUniqueId);

  Optional<PersistedVideo> getFirstByFileUniqueId(String fileUniqueId);

  Optional<PersistedVideo> getFirstByYoutubeId(String youtubeId);
}
