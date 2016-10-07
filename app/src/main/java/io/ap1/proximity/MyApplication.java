package io.ap1.proximity;

import android.app.Application;
import android.os.Build;

/**
 * Created by admin on 12/04/16.
 */
public class MyApplication extends Application{
    private static final String TAG = "MyApplication";

    private final static String deviceInfo = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;

    private CrashHandler crashHandler;

    @Override
    public void onCreate(){
        super.onCreate();

        //crashHandler = CrashHandler.getInstance();
        //crashHandler.init(this);
//        crashHandler.setDeviceInfo(deviceInfo);
    }

    public void setUsername(String username){
//        crashHandler.setUsername(username);
    }

    public void setUserObjectId(String userObjectId){
//        crashHandler.setUserAppId(userObjectId);
    }
}
