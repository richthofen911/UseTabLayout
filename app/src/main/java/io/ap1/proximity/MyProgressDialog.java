package io.ap1.proximity;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by admin on 26/05/16.
 */
public class MyProgressDialog extends AlertDialog{
    private static final String TAG = "MyProgressDialog";

    private static boolean isLastDialogDismissed = true;
    private static Activity activity;
    private static Builder builder;
    private static AlertDialog instance;
    final private static OnCancelListener onCancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            isLastDialogDismissed = true;
            Toast.makeText(activity, "Still in progress", Toast.LENGTH_SHORT).show();
        }
    };

    public MyProgressDialog(Activity activity){
        super(activity);
        Log.e(TAG, "create builder for this activity...");
        builder = new Builder(activity);
    }

    public static void show(@NonNull Activity currentActivity, String message){
        if(isLastDialogDismissed){
            if(activity != currentActivity){
                //Log.e(TAG, "different activity, create new builder...");
                new MyProgressDialog(currentActivity);
                activity = currentActivity;
            }

            builder.
                    setTitle("").
                    setCancelable(true).
                    setOnCancelListener(onCancelListener).
                    setMessage(message);
            instance = builder.create();
            instance.show();
            isLastDialogDismissed = false;
        }else
            throw new RuntimeException("Last dialog has not been dismissed/canceled yet!");
    }

    public static void dismissDialog (){
        if(instance == null)
            throw new RuntimeException("You must set the Dialog first!");
        instance.dismiss();
        isLastDialogDismissed = true;
    }
}
