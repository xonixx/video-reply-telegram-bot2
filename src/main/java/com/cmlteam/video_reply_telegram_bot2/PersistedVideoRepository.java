package com.cmlteam.video_reply_telegram_bot2;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersistedVideoRepository extends MongoRepository<PersistedVideo, String> {}
