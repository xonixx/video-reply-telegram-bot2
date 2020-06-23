package com.cmlteam.video_reply_telegram_bot2;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.replaceChars;

@Component
public class SearchStringMatcher {

  private static final String RU_KEYBOARD = "йцукенгшщзхъфывапролджэячсмитьбюё";
  private static final String EN_KEYBOARD = "qwertyuiop[]asdfghjkl;'zxcvbnm,.`";

  public boolean matches(String keyword, @NonNull String query) {
    String keywordNormalized = normalize(keyword);
    String queryNormalized = normalize(query);

    return matches0(keywordNormalized, queryNormalized)
        || matches0(
            keywordNormalized, normalize(replaceChars(queryNormalized, EN_KEYBOARD, RU_KEYBOARD)))
        || matches0(
            keywordNormalized, normalize(replaceChars(queryNormalized, RU_KEYBOARD, EN_KEYBOARD)));
  }

  private boolean matches0(String keywordNormalized, String queryNormalized) {
    return queryNormalized.contains(keywordNormalized)
        || Pattern.compile("\\b" + Pattern.quote(queryNormalized))
            .matcher(keywordNormalized)
            .find();
  }

  private String normalize(@NonNull String str) {
    return replaceChars(str.toLowerCase(), "ё", "е");
  }
}
