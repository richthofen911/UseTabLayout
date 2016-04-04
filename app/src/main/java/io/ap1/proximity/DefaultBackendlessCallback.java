package io.ap1.proximity;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

public class DefaultBackendlessCallback<T> extends BackendlessCallback<T> {
    private Context context;
    private ProgressDialog progressDialog;
    private String TAG;

    public DefaultBackendlessCallback(Context context, String TAG) {
        this.context = context;
        this.TAG = TAG;
        //progressDialog = ProgressDialog.show( context, "", "Loading...", true );
        //progressDialog.setCancelable(true);
    }

    public DefaultBackendlessCallback(Context context, String TAG, String message) {
        this.context = context;
        this.TAG = TAG;
        progressDialog = ProgressDialog.show(context, "", message, true );
    }

    @Override
    public void handleResponse( T response ) {
        if(progressDialog != null)
            progressDialog.cancel();
        Log.e("progressDialog", "canceled");
    }

    @Override
    public void handleFault( BackendlessFault fault ) {
        if(progressDialog != null)
            progressDialog.cancel();
        Toast.makeText(context, TAG + ": " + fault.getMessage(), Toast.LENGTH_LONG).show();
    }
}