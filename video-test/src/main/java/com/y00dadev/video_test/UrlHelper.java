package com.y00dadev.video_test;

import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class UrlHelper {

    private static final String CLIENT_ID = "Client-ID";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_KEY = "application/vnd.twitchtv.v5+json";
    private static final String TWITCH_CLIENT_ID = "kimne78kx3ncx6brgo4mv6wki5h1ko";
  private static HttpURLConnection mInputStreamConnection = null;


  public static String urlToJson(URL url) {

        HttpURLConnection connection = null;
        Scanner scanner = null;
        String result = "";

        try {
            connection = openConnection(url);
            connection.setRequestProperty(ACCEPT, ACCEPT_KEY);
            connection.setRequestProperty(CLIENT_ID, TWITCH_CLIENT_ID);
            InputStream stream = connection.getInputStream();
            scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");
            boolean hasData = scanner.hasNext();
            if (hasData) {
                return scanner.next();
            } else {
                return result;
            }

        } catch (Exception e) {
            Log.d("Error", e.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static InputStream urlToInputStream(URL url) {
        try {
            mInputStreamConnection = openConnection(url);
            return mInputStreamConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeInputStreamConnection() throws IOException {
      if(mInputStreamConnection != null) {
        mInputStreamConnection.disconnect();
        mInputStreamConnection = null;
      }
    }

    public static HttpURLConnection openConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    public static URL uriToUrl(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

}
