package io.ap1.libbeaconmanagement;

import android.app.Application;
import android.content.Context;

/**
 * Created by admin on 26/02/16.
 */
public class MyApplication extends Application {
    public static Context context;

    @Override
    public void onCreate(){
        context = this;
        super.onCreate();
    }
}
