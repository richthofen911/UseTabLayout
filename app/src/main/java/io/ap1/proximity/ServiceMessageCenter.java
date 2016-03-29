package io.ap1.proximity;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessFault;

import net.callofdroidy.apas.ServiceMsgIOCenter;

import io.ap1.proximity.adapter.AdapterChatMsgList;
import io.ap1.proximity.adapter.AdapterUserInList;

public class ServiceMessageCenter extends ServiceMsgIOCenter<MyPubsubProviderClient, AppPubsubCallback> {

    private RecyclerView recyclerViewToScroll;

    private String myUserObjectId;
    private String targetUserObjectId;

    private BackendlessUser myUserObject;

    private AdapterUserInList adapterUserInList;
    private AdapterChatMsgList adapterChatMsgList;

    public ServiceMessageCenter() {
    }

    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle bundle = intent.getExtras();
        if(bundle.getString("myUserObjectId") != null){
            myUserObjectId = bundle.getString("myUserObjectId");

            Backendless.Persistence.of( BackendlessUser.class ).findById(myUserObjectId, new DefaultBackendlessCallback<BackendlessUser>(this) {
                @Override
                public void handleResponse(BackendlessUser response) {
                    myUserObject = response;
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    super.handleFault(fault);
                    Log.e("Handle fault", fault.toString());
                }
            });
        }

        return new BinderMsgCenter();
    }

    public void setAdapterUserInList(AdapterUserInList adapterUserInList){
        this.adapterUserInList = adapterUserInList;
    }

    private void retrieveUserObject(final String detectedUserObjectId){
        Backendless.Persistence.of( BackendlessUser.class ).findById(detectedUserObjectId, new DefaultBackendlessCallback<BackendlessUser>(this) {
            @Override
            public void handleResponse(BackendlessUser response) {
                Log.e("detect user info", response.getProperty("name") + "\n" + response.getProperty("profileImage"));
                // if not duplicate user, add to list
                if(AppDataStore.duplicateCheck.get(response.getObjectId()) == null){
                    AppDataStore.duplicateCheck.put(response.getObjectId(), "hold");
                    AppDataStore.userList.add(new MyBackendlessUser(response));
                    adapterUserInList.notifyItemInserted(AppDataStore.userList.size());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                Log.e("Handle fault", fault.toString());
            }
        });
    }

    private BackendlessUser getMyBackendlessUserObject(){
        return myUserObject;
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);

        adapterChatMsgList = null;

        return true;
    }

    public class BinderMsgCenter extends ServiceMsgIOCenter.BinderMsgIO{
        public BinderMsgCenter(){
            super();
        }

        public BackendlessUser getMyUserObject(){
            return getMyBackendlessUserObject();
        }

        public void setMyAdapterUserInList(AdapterUserInList adapterUserInList) {
            setAdapterUserInList(adapterUserInList);
        }

        public void getUserObjectByObjectId(String detectedUserObjectId){
            retrieveUserObject(detectedUserObjectId);
        }
    }
}
