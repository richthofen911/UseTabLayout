package io.ap1.proximity.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import io.ap1.proximity.R;

public class ActivityBeaconUrlContent extends AppCompatActivity {
    private static final String TAG = "BeaconUrlContent";

    WebView wvBeaconUrlContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_web_content);

        wvBeaconUrlContent = (WebView) findViewById(R.id.wv_beacon_url_content);
        String url = getIntent().getStringExtra("url");
        Log.e(TAG, "onCreate: get url: " + url);
        if(url.equals("unknown")) url = "http://www.ap1.io";
        wvBeaconUrlContent.loadUrl(url);
    }
}
