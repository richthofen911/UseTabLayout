package io.ap1.proximity.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import io.ap1.proximity.R;

public class ActivityColorPicker extends AppCompatActivity {

    private static final String TAG = "ActivityColorPicker";

    String caller;
    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        Intent intent = getIntent();
        caller = intent.getStringExtra("caller");
        defaultColor = Color.parseColor(intent.getStringExtra("defaultColor"));

        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        float density = metrics.density;
        Log.e("width & height", config.screenWidthDp * density + "--" + config.screenHeightDp * density);

        View viewColorPicker = new ColorPickerView(this, new ColorPickerView.OnColorChangedListener(){
            @Override
            public void onColorChanged(String str,int color) {
                // send the color to caller Activity
                Intent resultIntent = new Intent();
                Log.e(TAG, "color: " + color);
                resultIntent.putExtra("newColor", color);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, defaultColor, defaultColor, config.screenWidthDp * density, config.screenHeightDp * density, density);

        LinearLayout rootView = (LinearLayout) findViewById(R.id.color_picker_container);
        rootView.addView(viewColorPicker);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.activity_color_picker, null);

    }

}
