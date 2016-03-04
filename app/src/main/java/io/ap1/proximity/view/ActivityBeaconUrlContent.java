package io.ap1.proximity.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import io.ap1.proximity.R;

public class ActivityBeaconUrlContent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_web_content);

        WebView wvBeaconUrlContent = (WebView) findViewById(R.id.wv_beacon_url_content);
        wvBeaconUrlContent.loadUrl(getIntent().getStringExtra("url"));
    }
}
