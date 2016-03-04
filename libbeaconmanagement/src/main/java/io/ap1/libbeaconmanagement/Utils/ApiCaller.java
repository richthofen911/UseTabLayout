package io.ap1.libbeaconmanagement.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.ap1.libbeaconmanagement.MyApplication;

/**
 * Created by admin on 26/02/16.
 *  This is a singleton class used as an http client to call http APIs
 */
public enum  ApiCaller {

    INSTANCE(MyApplication.context, Volley.newRequestQueue(MyApplication.context));

    private final Context context;
    private RequestQueue requestQueue;
    private String APIUrlStr;
    private String APIUrlEncoded;

    private int requestMethod; //Request.Method.GET is an int
    private Map<String, String> postParams; //this is for POST request
    private StringRequest requestCallAPI;

    ApiCaller(Context context, RequestQueue requestQueue){
        this.context = context;
        this.requestQueue = requestQueue;
    }

    public ApiCaller setAPI(String urlBase, String urlPath, String urlParams, Map<String, String> postParams, int method){
        if(urlParams == null)
            APIUrlStr = urlBase + urlPath;
        else
            APIUrlStr = urlBase + urlPath + urlParams;
        APIUrlEncoded = Uri.encode(APIUrlStr).replace("%3A", ":");
        APIUrlEncoded = APIUrlEncoded.replace("%2F", "/");
        APIUrlEncoded = APIUrlEncoded.replace("%3F", "?");
        APIUrlEncoded = APIUrlEncoded.replace("%3D", "=");
        APIUrlEncoded = APIUrlEncoded.replace("%26", "&");
        Log.e("url encoded", APIUrlEncoded);

        if(postParams != null && method == Request.Method.POST)
            this.postParams = postParams;
        else {
            Log.e("postParams", "is illegal here");
        }

        requestMethod = method;

        return this;
    }

    public interface VolleyCallback{
        void onDelivered(final String result);
        void onException(final String e);
    }

    public void exec(final VolleyCallback callback){
        if(APIUrlEncoded == null){
            callback.onDelivered("API has not been set yet");
        }else {
            final Timer timerRequestExec = new Timer();
            requestCallAPI = new StringRequest(requestMethod, APIUrlEncoded, new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    requestCallAPI.markDelivered();
                    timerRequestExec.cancel();
                    callback.onDelivered(response.replace("\\", ""));
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    requestCallAPI.markDelivered();
                    timerRequestExec.cancel();
                    callback.onDelivered(error.toString());
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return postParams;
                }
            };
            requestCallAPI.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(requestCallAPI);
            timerRequestExec.schedule(new TimerTask() { //if the request doesn't get any response in 5 seconds(timeout), cancel it and trigger the callback
                @Override
                public void run() {
                    if(requestCallAPI != null && !requestCallAPI.hasHadResponseDelivered()){
                        requestCallAPI.cancel();
                        Log.e("request cancelled", "");
                        callback.onException("Time Out");
                    }
                }
            }, 5000);
        }
    }

    public void cancelRequest(){
        if(requestCallAPI != null)
            requestCallAPI.cancel();
        Log.e("request cancelled", "");
    }
}