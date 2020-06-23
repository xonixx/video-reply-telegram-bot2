package com.cmlteam.video_reply_telegram_bot;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Getter
class VideosPage {
  private final int offsetIdx;
  private final List<Video> videos;
  private final String nextOffset;

  static VideosPage of(List<Video> allResults, int pageSize, @NonNull String offsetS) {
    Offset offset = Offset.of(offsetS);
    Collections.shuffle(allResults, new Random(offset.seed));
    int total = allResults.size();
    int offsetIdx = offset.offset;
    int nextOffsetNum = offsetIdx + pageSize;
    String nextOffsetS =
        nextOffsetNum < total ? new Offset(offset.seed, nextOffsetNum).toString() : "";
    return new VideosPage(
        offsetIdx, allResults.subList(offsetIdx, Math.min(nextOffsetNum, total)), nextOffsetS);
  }

  @RequiredArgsConstructor
  static class Offset {
    final long seed;
    final int offset;

    static Offset of(@NonNull String offset) {
      if ("".equals(offset)) {
        return new Offset(new Random().nextLong(), 0);
      }
      String[] parts = offset.split(":");
      return new Offset(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
    }

    @Override
    public String toString() {
      return seed + ":" + offset;
    }
  }
}
