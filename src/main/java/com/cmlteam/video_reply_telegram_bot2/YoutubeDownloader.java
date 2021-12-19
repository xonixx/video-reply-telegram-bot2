package com.cmlteam.video_reply_telegram_bot2;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class YoutubeDownloader {
  Pattern youtubeLinkRegex =
      Pattern.compile("^(https?://)?((www\\.)?youtube\\.com|youtu\\.?be)/.+$");

  boolean isYoutubeLink(String candidate) {
    return youtubeLinkRegex.matcher(candidate).matches();
  }

  public YoutubeVideoInfo getVideoInfo(String videoUrl) throws YoutubeDLException {
    String directory = "/tmp";
    YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, directory);
    request.setOption("dump-json");

    YoutubeDL.setExecutablePath("python39 /usr/local/bin/yt-dlp");

    YoutubeDLResponse response = YoutubeDL.execute(request);

    if (response.getExitCode() != 0) {
      throw new YoutubeDLException(getResultInfo(response));
    }

    return JsonUtil.parseJson(response.getOut(), YoutubeVideoInfo.class);
  }

  public void download(String videoUrl, YoutubeDlResultHandler youtubeDlResultHandler)
      throws YoutubeDLException {

    String directory = "/tmp";

    String fname = UUID.randomUUID() + ".mp4";

    YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, directory);
    //    request.setOption("ignore-errors"); // --ignore-errors
    //    request.setOption("output", "%(id)s.mp4"); // --output "%(id)s"
    request.setOption("output", fname); // --output "%(id)s"
    request.setOption("retries", 3); // --retries 3
    request.setOption("format", 18);

    //    System.out.println(YoutubeDL.getVersion());

    YoutubeDLResponse response = YoutubeDL.execute(request);

    if (response.getExitCode() != 0) {
      throw new YoutubeDLException(getResultInfo(response));
    }

    File resultFile = new File(directory, fname);

    if (!resultFile.isFile()) {
      throw new YoutubeDLException("Result file missing. " + getResultInfo(response));
    }
    try {
      youtubeDlResultHandler.handleResultFile(resultFile, response.getElapsedTime());
    } finally {
      if (resultFile.exists()) {
        if (!resultFile.delete()) {
          throw new RuntimeException("Unable to delete: " + resultFile);
        }
      }
    }
  }

  private String getResultInfo(YoutubeDLResponse response) {
    return "exit code = "
        + response.getExitCode()
        + "\n"
        + "elapsed = "
        + response.getElapsedTime()
        + "\n"
        + "stdOut = "
        + response.getOut();
  }

  interface YoutubeDlResultHandler {
    void handleResultFile(File file, int elapsedTime);
  }
}
