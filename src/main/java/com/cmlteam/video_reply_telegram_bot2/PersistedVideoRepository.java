package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersistedVideoRepository extends MongoRepository<PersistedVideo, String> {
  Optional<PersistedVideo> getFirstByUserIdOrderByLastModifiedDateDesc(int userId);

  Optional<PersistedVideo> getFirstByUserIdAndMessageId(int userId, int messageId);

  Optional<PersistedVideo> getFirstByUserIdAndFileUniqueId(int userId, String fileUniqueId);
}
