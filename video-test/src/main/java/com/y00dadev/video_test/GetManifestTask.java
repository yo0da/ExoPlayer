package com.y00dadev.video_test;

import android.net.Uri;
import android.os.AsyncTask;
import com.google.android.exoplayer2.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetManifestTask extends AsyncTask<String, Void, ArrayList<String>> {

  private static final String TAG_MEDIA = "#EXT-X-MEDIA";
  private static final String AUDIO_ONLY = "audio_only";
  private static final String AUDIO_ONLY_REPLACE = "Audio only";
  private static final Pattern REGEX_GROUP_ID = Pattern.compile("GROUP-ID=\"(.+?)\"");
  private static final Pattern REGEX_NAME = Pattern.compile("NAME=\"(.+?)\"");

  private GetManifestResponse mCallback;

  public interface GetManifestResponse{
    void finishedGetManifestTask(ArrayList<String> namesList);
  }

  public GetManifestTask(GetManifestResponse callback) {
    mCallback = callback;
  }

  @Override
  protected ArrayList<String> doInBackground(String... strings) {
    String url = strings[0];
    Uri uri = Uri.parse(url);
    URL queryUrl = UrlHelper.uriToUrl(uri);

    InputStream stream = UrlHelper.urlToInputStream(queryUrl);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    ArrayList<String> lines = new ArrayList<>();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if(line.startsWith(TAG_MEDIA)) {
          lines.add(line);
        }
      }
      return parseMediaInfo(lines);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Util.closeQuietly(reader);
      Util.closeQuietly(stream);
      try {
        UrlHelper.closeInputStreamConnection();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  protected void onPostExecute(ArrayList<String> strings) {
    mCallback.finishedGetManifestTask(strings);
  }

  private ArrayList<String> parseMediaInfo(ArrayList<String> lines) throws IOException {
    String line;
    ArrayList<String> namesList = new ArrayList<>();
    for(int i = 0; i < lines.size(); i++) {
      line = lines.get(i);
      String name = parseStringAttr(line, REGEX_NAME);
      if(name.equals(AUDIO_ONLY)) {
        name = AUDIO_ONLY_REPLACE;
      }
      namesList.add(name);
    }
    return namesList;
  }

  private String parseStringAttr(String line, Pattern regexName) {
    Matcher matcher = regexName.matcher(line);
    String value = matcher.find() ? matcher.group(1) : "";
    return value;
  }



}
