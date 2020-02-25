package com.y00dadev.video_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GetUrlTask.AsyncResponse {

    private static final String EXTRA_STREAM_URL = "extra_stream_url";
    private Intent mIntent;
    private String mChannelName = "summit1g";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            mIntent = new Intent(getBaseContext(), VideoActivity.class);
            new GetUrlTask(this).execute(mChannelName);
        });
    }

    @Override
    public void finished(String url) {
        mIntent.putExtra(EXTRA_STREAM_URL, url);
        startActivity(mIntent);
    }
}
