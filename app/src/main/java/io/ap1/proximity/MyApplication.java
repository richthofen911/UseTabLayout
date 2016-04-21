package io.ap1.proximity;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;

/**
 * Created by admin on 12/04/16.
 */
public class MyApplication extends Application{
    private static final String TAG = "MyApplication";
    private String username;
    private String userObjectId;
    private final static String deviceInfo = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler(){
        @Override
        public void uncaughtException(Thread thread, final Throwable throwable){
            Log.e(TAG, "uncaughtException: " + throwable.toString());

            String bugTrackerServerBaseUrl = "http://159.203.36.215:3999";
            String urlPath = "/report";
            Log.e(TAG, "uncaughtException, params: " + username + "/" + userObjectId);
            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("appname", "Proximity(Android)");
            postParams.put("username", username);
            postParams.put("userappid", userObjectId);
            postParams.put("deviceinfo", deviceInfo);
            String cause = throwable.toString();
            cause = cause.replace("\n", ". ");
            postParams.put("cause", cause);

            ApiCaller.getInstance(getApplicationContext()).setAPI(bugTrackerServerBaseUrl, urlPath, null, postParams, Request.Method.POST).exec(
                new DefaultVolleyCallback() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e(TAG, "Crash Cause Reported");
                    }

                    @Override
                    public void onException(final String e) {

                    }
                });
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setUserObjectId(String userObjectId){
        this.userObjectId = userObjectId;
    }
}
