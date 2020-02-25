package com.y00dadev.video_test;

import android.net.Uri;
import android.os.AsyncTask;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

public class GetUrlTask extends AsyncTask<String, Void, String> {

    private static final String API_URL = "https://api.twitch.tv/api/channels";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN = "token";
    private static final String SIG = "sig";

    private AsyncResponse mCallback;

    public interface AsyncResponse {
        void finished(String url);
    }

    public GetUrlTask(AsyncResponse callback) {
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        String channelName = strings[0];
        String sig = "";
        String token = "";

        Uri uri = Uri.parse(API_URL).buildUpon()
                .appendPath(channelName)
                .appendPath(ACCESS_TOKEN)
                .build();
        URL apiUrl = UrlHelper.uriToUrl(uri);

        String resultString = UrlHelper.urlToJson(apiUrl);
        try {
            JSONObject resultJson = new JSONObject(resultString);
            token = URLEncoder.encode(resultJson.getString(TOKEN), StandardCharsets.UTF_8.toString());
            sig = resultJson.getString(SIG);

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String streamUrl = String.format("http://usher.twitch.tv/api/channel/hls/%s.m3u8" +
                "?player=twitchweb&" +
                "&token=%s" +
                "&sig=%s" +
                "&allow_audio_only=true" +
                "&allow_source=true" +
                "&type=any" +
                "&p=%s", channelName, token, sig, "" + new Random().nextInt(6));

        return streamUrl;
    }

    @Override
    protected void onPostExecute(String url) {
        mCallback.finished(url);
    }
}
