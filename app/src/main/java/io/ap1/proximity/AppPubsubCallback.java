package io.ap1.proximity;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

import net.callofdroidy.apas.GeneralPubsubCallback;
import net.callofdroidy.apas.Message;

import org.json.JSONArray;
import org.json.JSONException;

import io.ap1.proximity.adapter.AdapterChatMsgList;
import io.ap1.proximity.adapter.AdapterUserInList;
import io.ap1.proximity.view.ActivityMain;

/**
 * Created by admin on 23/03/16.
 */
public class AppPubsubCallback extends Callback implements GeneralPubsubCallback {
    private Gson gson;
    private Activity activity;
    private Handler handler;
    private String TAG;
    private AdapterUserInList adapterUserInList;
    private AdapterChatMsgList adapterChatMsgList;
    private String myUserObjectId;
    private String currentChattingUserObjectId;
    private RecyclerView recyclerViewToScroll;
    public volatile static long checkDuplicateTimestamp = 0;
    public volatile static String checkDuplicateSource = "unknown";

    private static AppPubsubCallback instance;

    public AppPubsubCallback(Activity activity, String myUserObjectId, AdapterUserInList adapterUserInList, @NonNull String TAG) {
        super();
        instance = this;
        this.activity = activity;
        this.myUserObjectId = myUserObjectId;
        this.adapterUserInList = adapterUserInList;
        this.handler = new Handler(activity.getMainLooper());
        this.TAG = TAG;
        gson = new Gson();

    }

    public synchronized static AppPubsubCallback getAppPubsubCallback(){
        return instance;
    }

    @Override
    public void connectCallback(String channel, Object message) {
        Log.e("SUBSCRIBE", "CONNECT on channel:" + channel
                + " : " + message.getClass() + " : "
                + message.toString());
    }

    @Override
    public void disconnectCallback(String channel, Object message) {
        Log.e("SUBSCRIBE", "DISCONNECT on channel:" + channel
                + " : " + message.getClass() + " : "
                + message.toString());
    }

    public void reconnectCallback(String channel, Object message) {
        Log.e("SUBSCRIBE", "RECONNECT on channel:" + channel
                + " : " + message.getClass() + " : "
                + message.toString());
    }

    // this method is called when either successfully receive/publish a message
    @Override
    public void successCallback(String channel, Object message) {
        try {
            JsonElement msgInJsonElement = gson.fromJson(message.toString(), JsonElement.class);
            onSuccessReceive(gson.fromJson(msgInJsonElement, Message.class));
        } catch (JsonSyntaxException e) {
            try {
                JSONArray jsonArray = (JSONArray) message;
                if (String.valueOf(jsonArray.get(0)).equals("1"))
                    onSuccessPublish("Msg sent");
            } catch (JSONException x) {
                Log.e(TAG, "get a message, but not an expected one and no need to process");
            }
        }
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        onFailReceive(error.getErrorString());
        onFailPublish(error.getErrorString());
    }

    // this method is from GeneralPubsubCallback
    @Override
    public void onSuccessReceive(final Message message) {
        synchronized (this){
            boolean isDuplicateMessage = false;
            long timestamp = Long.parseLong(message.getHeaders().get("timestamp"));
            String source = message.getHeaders().get("source");
            Log.e(TAG, "onSuccessReceiveNotFiltered, last timestamp: " + checkDuplicateTimestamp + ", this timestamp: " + timestamp +
                    "\nlast source: " + checkDuplicateSource + ", this source: " + source);
            if(timestamp == checkDuplicateTimestamp && source.equals(checkDuplicateSource))
                isDuplicateMessage = true;
            if(!isDuplicateMessage){
                checkDuplicateTimestamp = timestamp;
                checkDuplicateSource = source;
                if(source.equals(myUserObjectId)){ // this is the msg I sent, show it on my chat history
                    if(adapterChatMsgList != null){
                        adapterChatMsgList.getChatHistory().add(message);
                        adapterChatMsgList.notifyItemInserted(adapterChatMsgList.getItemCount());
                        if(recyclerViewToScroll != null){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // scroll to the bottom to show new msg
                                    recyclerViewToScroll.scrollToPosition(adapterChatMsgList.getItemCount() - 1);
                                }
                            });
                        }
                    }
                }else{
                    for (int i = 0; i < AppDataStore.userList.size(); i++) {
                        Log.e(TAG, AppDataStore.userList.get(i).getUserObjectId());
                        if (source.equals(AppDataStore.userList.get(i).getUserObjectId())) { // if publishId is in userList
                            if(!source.equals(currentChattingUserObjectId)){ // if it's not the user you're currently chatting with
                                Log.e(TAG, "onSuccessReceive, source is from a user in users list");
                                AppDataStore.userList.get(i).addToMessageList(message);  // update the userList
                                adapterUserInList.notifyItemChanged(i, AppDataStore.userList.get(i));
                            }else{
                                Log.e(TAG, "onSuccessReceive, source is from the person I'm talking to");
                                adapterChatMsgList.getChatHistory().add(message); // if it is the user you're currently chatting with
                                adapterChatMsgList.notifyItemInserted(adapterChatMsgList.getItemCount()); // update the chat history list
                                if(recyclerViewToScroll != null){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // scroll to the bottom to show new msg
                                            recyclerViewToScroll.scrollToPosition(adapterChatMsgList.getItemCount() - 1);
                                        }
                                    });
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

    }

    public void setActivity(@NonNull Activity activity){
        this.activity = activity;
        this.handler = new Handler(this.activity.getMainLooper());
    }

    public void setTAG(String newTAG){
        this.TAG = newTAG;
    }

    public void setChatMsgListAdapter(AdapterChatMsgList adapterChatMsgList){
        this.adapterChatMsgList = adapterChatMsgList;
    }

    public void setRecyclerViewToScroll(RecyclerView recyeclerViewToScroll){
        this.recyclerViewToScroll = recyeclerViewToScroll;
    }

    public void setCurrentTalkingUser(String currentTalkingUserObjectId){
        this.currentChattingUserObjectId = currentTalkingUserObjectId;
    }

    // this method is from GeneralPubsubCallback
    @Override
    public void onFailReceive(Object object) {
        Log.e("fail recv", object.toString());
        toastCallbackResult(object.toString());
    }

    // this method is from GeneralPubsubCallback
    @Override
    public void onSuccessPublish(Object object) {
        Log.e("success pub", object.toString());
    }

    // this method is from GeneralPubsubCallback
    @Override
    public void onFailPublish(Object object) {
        Log.e("fail pub", object.toString());
        toastCallbackResult(object.toString());
    }

    protected void toastCallbackResult(final String result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Callback info: " + result, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Callback info: " + result);

            }
        });
    }
}
