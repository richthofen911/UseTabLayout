package io.ap1.proximity;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;

/**
 * Created by admin on 12/04/16.
 */
public class MyApplication extends Application{
    private static final String TAG = "MyApplication";

    private SharedPreferences spUserInfo;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler(){
        @Override
        public void uncaughtException(Thread thread, final Throwable throwable){
            String bugTrackerServerBaseUrl = "http://192.168.128.57:3999";
            String urlPath = "/";
            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("cause", throwable.toString());

            ApiCaller.getInstance(getApplicationContext()).setAPI(bugTrackerServerBaseUrl, urlPath, null, postParams, Request.Method.POST).exec(
                new ApiCaller.VolleyCallback() {
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
        spUserInfo = this.getSharedPreferences("UserInfo", 0);
    }

    public void setUserLoginName(String loginName){
        spUserInfo.edit().putString("username", loginName).apply();
    }

    public void setUserLoginPassword(String loginPassword){
        spUserInfo.edit().putString("password", loginPassword).apply();
    }

    public String getUserLoginName(){
        return spUserInfo.getString("username", null);
    }

    public String getUserLoginPassword(){
        return spUserInfo.getString("password", null);
    }
}
