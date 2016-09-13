package io.ap1.proximity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by admin on 04/03/16.
 */
public class ColorPickerView extends View {
    private Paint mPaint;
    private float mCurrentHue = 0;
    private int mCurrentX = 0, mCurrentY = 0;
    private int mCurrentColor, mDefaultColor;
    private final int[] mHueBarColors = new int[258];
    private int[] mMainColors = new int[65536];
    private OnColorChangedListener mListener;

    private int widthScreen;
    private int heightScreen;
    private int widthView;
    private int heightView;

    private float strokeWidth;
    private float density;

    private int hueBarXOffset;
    private int hueBarXEnd;
    private int hueBarHeight = 250;  // px
    private int hueBarYStart;
    private int hueBarYEnd;

    private int mainFieldXOffset;
    private int mainFieldXEnd;
    private int mainFieldYStart;
    private int mainFieldHeight = 250;
    private int mainFieldYEnd;

    private int currentColorCircleX;
    private int currentColorCircleY;
    private int currentColorCircleRadius;

    private int buttonXOffset;
    private int buttonXEnd;
    private int buttonYStart;
    private int buttonHeight = 125;
    private int buttonYEnd;


    private int defaultPaddingInDp = 16; // this is default style padding, usually 16 dp
    private float defaultPaddingInPx;

    public interface OnColorChangedListener{
        void onColorChanged(String tag, int selectedColor);
    }

    ColorPickerView(Context c, OnColorChangedListener l, int color, int defaultColor, float widthScreen, float heightScreen, float density) {
        super(c);

        this.widthScreen = (int) widthScreen;
        this.heightScreen = (int) heightScreen;
        this.density = density;

        defaultPaddingInPx = defaultPaddingInDp * density;

        strokeWidth = (float) Math.floor((widthScreen - defaultPaddingInDp * 2 * density) / 256);   // 16 is default padding in dp, * 2 means left & right, 256 means 256-bit color
        widthView = (int) (256 * strokeWidth);
        hueBarXOffset = (int) ((widthScreen - widthView) / 2);

        hueBarXEnd = (int) (hueBarXOffset + widthView);
        hueBarYStart = (int) defaultPaddingInPx;
        hueBarYEnd = hueBarYStart + hueBarHeight;
        //Log.e("widthview", widthView + "");
        heightView = (int) (heightScreen - defaultPaddingInPx * 2);

        mainFieldXOffset = hueBarXOffset;
        mainFieldXEnd = hueBarXEnd;
        mainFieldYStart = (int) (hueBarYEnd + defaultPaddingInPx);
        mainFieldYEnd = mainFieldYStart + mainFieldHeight;

        currentColorCircleRadius = (int) (35 * density);
        currentColorCircleX = (int) widthScreen / 2;
        currentColorCircleY = (int) (mainFieldYEnd + 40 * density + currentColorCircleRadius);

        buttonXOffset = hueBarXOffset;
        buttonXEnd = hueBarXEnd;
        buttonYStart = (int)(currentColorCircleY + currentColorCircleRadius + 40 * density);
        buttonYEnd = buttonYStart + buttonHeight;

        Log.e("cpv init", widthScreen + " " + heightScreen + " " + strokeWidth + " " + hueBarXOffset + " " + widthView + " " + hueBarXEnd);

        mListener = l;
        mDefaultColor = defaultColor;

        // Get the current hue from the current color and update the main color field
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mCurrentHue = hsv[0];
        updateMainColors();

        mCurrentColor = color;

        // Initialize the colors of the hue slider bar
        initHueSliderBar();

        // Initializes the Paint that will draw the View
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(12);
    }

    public void initHueSliderBar(){
        int index = 0;
        // Red (#f00) to pink (#f0f)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb(255, 0, (int) i);
            index++;
        }

        // Pink (#f0f) to blue (#00f)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb(255 - (int) i, 0, 255);
            index++;
        }

        // Blue (#00f) to light blue (#0ff)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb(0, (int) i, 255);
            index++;
        }

        // Light blue (#0ff) to green (#0f0)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb(0, 255, 255 - (int) i);
            index++;
        }

        // Green (#0f0) to yellow (#ff0)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb((int) i, 255, 0);
            index++;
        }

        // Yellow (#ff0) to red (#f00)
        for (float i = 0; i < 256; i += 256 / 42) {
            mHueBarColors[index] = Color.rgb(255, 255 - (int) i, 0);
            index++;
        }
    }

    // Get the current selected color from the hue bar
    private int getCurrentHueBarMainColor() {
        int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
        int index = 0;
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255, 0, (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255 - (int) i, 0, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(0, (int) i, 255);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(0, 255, 255 - (int) i);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb((int) i, 255, 0);
            index++;
        }
        for (float i = 0; i < 256; i += 256 / 42) {
            if (index == translatedHue)
                return Color.rgb(255, 255 - (int) i, 0);
            index++;
        }
        return Color.RED;
    }

    // Update the main field colors depending on the current selected hue
    private void updateMainColors() {
        int mainColor = getCurrentHueBarMainColor();
        int index = 0;
        int[] topColors = new int[256];
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                if (y == 0) {
                    mMainColors[index] = Color.rgb(
                            255 - (255 - Color.red(mainColor)) * x / 255,
                            255 - (255 - Color.green(mainColor)) * x / 255,
                            255 - (255 - Color.blue(mainColor)) * x / 255);
                    topColors[x] = mMainColors[index];
                } else
                    mMainColors[index] = Color.rgb(
                            (255 - y) * Color.red(topColors[x]) / 255,
                            (255 - y) * Color.green(topColors[x]) / 255,
                            (255 - y) * Color.blue(topColors[x]) / 255);
                index++;
            }
        }
        Log.e("mainColors", mMainColors[0] + "");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.e("cpv onDraw", widthScreen + " " + heightScreen + " " + strokeWidth + " " + hueBarXOffset);
        float y = 0;
        int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        // Display all the colors of the hue bar with lines, vertically, 256 times
        for (int x = 0; x < 256; x++) {
            if (translatedHue != x) { // If this is not the current selected hue, display the actual color
                mPaint.setColor(mHueBarColors[x]);

            } else { // else display a slightly larger black line
                mPaint.setColor(Color.BLACK);
            }

            canvas.drawLine(hueBarXOffset + y, 0, hueBarXOffset + y, hueBarHeight, mPaint);
            y = y + strokeWidth;
        }


        // Display the main field colors using LinearGradient. vertically, 256 times
        y = 0;
        for (int x = 0; x < 256; x++) {
            int[] colors = new int[2];
            colors[0] = mMainColors[x];
            colors[1] = Color.BLACK;

            Shader shader = new LinearGradient(mainFieldXOffset + x, mainFieldYStart, mainFieldXOffset + x, mainFieldYEnd, colors, null, Shader.TileMode.REPEAT);
            mPaint.setShader(shader);

            canvas.drawLine(mainFieldXOffset + y, mainFieldYStart, mainFieldXOffset + y, mainFieldYEnd, mPaint);
            y = y + strokeWidth;
        }

        mPaint.setShader(null);

        // Display the circle around the currently selected color in the main field

        if (mCurrentX > mainFieldXOffset && mCurrentX < mainFieldXEnd && mCurrentY > mainFieldYStart && mCurrentY < mainFieldYEnd) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
            mPaint.setColor(Color.BLACK);
            canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
        }

        // Display the current color in a solid circle below the main field
        mPaint.setShader(null);
        mPaint.setColor(mCurrentColor);
        mPaint.setStyle(Paint.Style.FILL);
        Log.e("circle coords", currentColorCircleX + "-" + currentColorCircleY);
        canvas.drawCircle(currentColorCircleX, currentColorCircleY, currentColorCircleRadius, mPaint);

        // Draw a 'confirm' button
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#737373"));
        canvas.drawRect(buttonXOffset, buttonYStart, buttonXEnd, buttonYEnd, mPaint);

        // Draw text on the button
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(16 * density);
        canvas.drawText("Pick", buttonXOffset + (buttonXEnd - buttonXOffset) / 2, buttonYStart + (buttonYEnd - buttonYStart) / 2 , mPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // set dialog dimension size 276 * 366 here
        //setMeasuredDimension((int)widthView, (int)heightView); // this doesn't work, crashes
        setMeasuredDimension((int)widthScreen, (int)heightScreen);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;
        float x = event.getX();
        float y = event.getY();
        Log.e("touch", x + " - " + y);

        // If the touch event is located in the hue bar
        if (x > hueBarXOffset && x < hueBarXEnd && y > hueBarYStart && y < hueBarYEnd) {
            // Update the main field colors
            mCurrentHue = (255 - (x - hueBarXOffset) / strokeWidth) * 360 / 255;    // 360 is HSL color mode(nature), 256 is  RGB mode(monitor)
            // Log.e("currentHue", mCurrentHue + "");
            updateMainColors();

            // Update the current selected color
            mCurrentX = (int) x;
            mCurrentY = (int) y;
            int transX = mCurrentX - hueBarXOffset;
            int transY = mCurrentY - hueBarYStart;
            Log.e("touch", "current X Y: " + mCurrentX + " " + mCurrentY + " in hue bar");
            Log.e("touch", "transX Y: " + transX + " " + transY + " in hue bar");
            int index = 256 * (transY - 1) + transX;
            Log.e("main color index", "from hue bar: " + index);
            Log.e("main colors length", "from hue bar: " + mMainColors.length);
            if (index > 0 && index < mMainColors.length){
                // Update the current color
                mCurrentColor = mMainColors[index];
                // Force the redraw of the view
                Log.e("force redrawing", "from hue bar");
                invalidate();
            }

        }

        // If the touch event is located in the main field
        if (x > mainFieldXOffset && x < mainFieldXEnd && y > mainFieldYStart && y < mainFieldYEnd) {
            mCurrentX = (int) x;
            mCurrentY = (int) y;
            int transX = mCurrentX - mainFieldXOffset;
            int transY = mCurrentY - mainFieldYStart;
            Log.e("touch", "current X Y: " + mCurrentX + " " + mCurrentY + " in main field");
            Log.e("touch", "transX Y: " + transX + " " + transY + " in main field");
            int index = 256 * (transY - 1) + transX;
            if (index > 0 && index < mMainColors.length) {
                // Update the current color
                mCurrentColor = mMainColors[index];
                // Force the redraw of the view
                Log.e("force redrawing", "from main field");
                invalidate();
            }
        }

        // If the touch event is located in the button, notify the listener with the current color
        if (x > buttonXOffset && x < buttonXEnd && y > buttonYStart && y < buttonYEnd)
            mListener.onColorChanged("Color Selected", mCurrentColor);

        return true;
    }
}
