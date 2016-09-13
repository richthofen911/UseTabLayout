package io.ap1.proximity.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.backendless.BackendlessUser;

import net.callofdroidy.apas.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.ap1.proximity.AppPubsubCallback;
import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.MyBackendlessUser;
import io.ap1.proximity.R;
import io.ap1.proximity.ServiceMessageCenter;
import io.ap1.proximity.adapter.AdapterChatMsgList;

public class ActivityChat extends AppCompatActivity {
    private final static String TAG = "ActivityChat";

    private String myUserObjectId;
    private String targetUserObjectId;
    private MyBackendlessUser otherBackendlessUser;

    public AdapterChatMsgList adapterChatMsgList;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;

    ServiceMessageCenter.BinderMsgCenter binderMsgCenter;

    private ServiceConnection messageCenterChatConn;

    public EditText etMsgInput;
    public Button btnSend;

    private ArrayList<Message> currentChatHistoryDataSource;

    private String myChannel;
    private String otherChannel;

    private BackendlessUser myUserObject;

    private final static String channelNamePrefix = "proximity_";

    private AppPubsubCallback appPubsubCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_chat_msg_list);
        etMsgInput = (EditText) findViewById(R.id.et_msg_input);
        btnSend = (Button) findViewById(R.id.btn_send_msg);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        int selectedUserIndex = getIntent().getIntExtra("selectedIndex", 0);
        otherBackendlessUser = AppDataStore.userList.get(selectedUserIndex);
        targetUserObjectId = otherBackendlessUser.getUserObjectId();
        otherBackendlessUser.getProfileImage();
        //myUserObjectId = ActivityMain.myUserObjectId;

        currentChatHistoryDataSource = otherBackendlessUser.getUnreadMessageList();
        if(currentChatHistoryDataSource == null)
            currentChatHistoryDataSource = new ArrayList<>();

        messageCenterChatConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderMsgCenter = (ServiceMessageCenter.BinderMsgCenter) service;

                myUserObject = binderMsgCenter.getMyUserObject();
                myUserObjectId = myUserObject.getObjectId();
                myChannel = binderMsgCenter.getSubChannel();
                otherChannel = channelNamePrefix + otherBackendlessUser.getUserObjectId();
                binderMsgCenter.setPubChannel(otherChannel);

                Log.e(TAG, "pub channel: " + binderMsgCenter.getPubChannel() + "\n" + "sub channel: " + binderMsgCenter.getSubChannel());

                adapterChatMsgList = new AdapterChatMsgList(ActivityChat.this, currentChatHistoryDataSource, myUserObjectId, targetUserObjectId, (String) myUserObject.getProperty("profileImage"), otherBackendlessUser.getProfileImage());
                recyclerView.setAdapter(adapterChatMsgList);

                appPubsubCallback = AppPubsubCallback.getAppPubsubCallback();
                appPubsubCallback.setActivity(ActivityChat.this);
                appPubsubCallback.setChatMsgListAdapter(adapterChatMsgList);
                appPubsubCallback.setCurrentTalkingUser(targetUserObjectId);
                appPubsubCallback.setRecyclerViewToScroll(recyclerView);
                appPubsubCallback.setTAG(TAG);

                binderMsgCenter.setPubsubCallback(AppPubsubCallback.getAppPubsubCallback());

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("source", myUserObjectId);
                        headers.put("name", (String) myUserObject.getProperty("name"));
                        headers.put("timestamp", String.valueOf(new Date().getTime()));
                        String body = etMsgInput.getText().toString();
                        Message msg = new Message(headers, body);

                        binderMsgCenter.pubToChannel(msg);
                        binderMsgCenter.pubToAnotherChannel(myChannel, msg);

                        etMsgInput.setText("");
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service MessageCenter", "Disconnected, name: " + name);
            }
        };

        bindService(new Intent(ActivityChat.this, ServiceMessageCenter.class), messageCenterChatConn, BIND_AUTO_CREATE);
    }
    
    @Override
    public void onStop(){
        super.onStop();

        Log.e(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy");
        adapterChatMsgList.notifyItemRangeRemoved(0, 0);
        otherBackendlessUser.clearMessageList();
        binderMsgCenter.setPubChannel(null); // clear targetPublishChannel
        appPubsubCallback.setCurrentTalkingUser(null);
        appPubsubCallback.setCurrentTalkingUser("0");
        appPubsubCallback.setChatMsgListAdapter(null);

        if(binderMsgCenter != null && binderMsgCenter.isBinderAlive())
            this.unbindService(messageCenterChatConn);
    }

}
