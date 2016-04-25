package io.ap1.proximity;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;

/**
 * Created by admin on 25/04/16.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler{
    private static final String TAG = "CrashHandler";

    private String username = "unknown";
    private String userappid = "unknown";
    private String deviceinfo = "unknown";

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE;
    private Context mContext;
    private CrashHandler() {}

    public synchronized static CrashHandler getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CrashHandler();
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uncaughtException: " + e.toString());
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * @return
     * true,  means process the exception, do not throw exception anymore
     * false, means don't process the exception now, (can be saved) but hand in it to the system to process(pop up the system dialog)
     */
    private boolean handleException(final Throwable throwable) {
        if (throwable == null)
            return false;

        final String message = throwable.getMessage();
        final Throwable cause = throwable.getCause();

        final String directCause = cause.toString();
        final StackTraceElement[] causesStackTrace = cause.getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();
        for(StackTraceElement stackTraceElement : causesStackTrace)
            stringBuilder.append("at ").append(stackTraceElement.toString()).append("\n");
        final String causesTrace = stringBuilder.toString();

        final String crashReport = message + "\nCaused by: " + directCause + "\n" + causesTrace;

        String bugTrackerServerBaseUrl = "http://159.203.36.215:3999";
        String urlPath = "/report";
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("appname", "Proximity(Android)");
        postParams.put("username", username);
        postParams.put("userappid", userappid);
        postParams.put("deviceinfo", deviceinfo);
        postParams.put("cause", crashReport);

        ApiCaller.getInstance(mContext).setAPI(bugTrackerServerBaseUrl, urlPath, null, postParams, Request.Method.POST).exec(
                new DefaultVolleyCallback() {
                    @Override
                    public void onDelivered(String result) {
                        super.onDelivered(result);
                        Log.e(TAG, "Crash Cause Reported");
                    }

                    @Override
                    public void onException(final String e) {
                        super.onException(e);
                    }
                });

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                String loginfo = message + "\nCaused by: " + directCause + "\n" + causesTrace;
                Log.e("loginfo", loginfo);
                Toast.makeText(mContext, "Oops, the program comes across an error:", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

        }.start();
        return false;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setUserAppId(String userappid){
        this.userappid = userappid;
    }

    public void setDeviceInfo(String deviceinfo){
        this.deviceinfo = deviceinfo;
    }
}
