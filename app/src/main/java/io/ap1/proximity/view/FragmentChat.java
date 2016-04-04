package io.ap1.proximity.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.ap1.proximity.AppPubsubCallback;
import io.ap1.proximity.Constants;
import io.ap1.proximity.MyPubsubProviderClient;
import io.ap1.proximity.R;
import io.ap1.proximity.ServiceMessageCenter;
import io.ap1.proximity.adapter.AdapterUserInList;

/**
 * Created by admin on 09/02/16.
 */
public class FragmentChat extends FragmentPreloadControl {
    private static final String TAG = "FragmentChat";
    private Toolbar toolbar;

    private TextView tvToolbarEnd;

    public AppPubsubCallback appPubsubCallback;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    public FragmentChat() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_user_list);
        linearLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(((ActivityMain)getActivity()).adapterUserInList);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        appPubsubCallback = AppPubsubCallback.getAppPubsubCallback();
        appPubsubCallback.setActivity((ActivityMain)getActivity());
        appPubsubCallback.setChatMsgListAdapter(null);
        appPubsubCallback.setRecyclerViewToScroll(recyclerView);
        appPubsubCallback.setTAG(TAG);
    }

    @Override
    protected void lazyLoad(){
        Log.e(TAG, "onVisibleLazyLoad");
        toolbar = ((ActivityMain)getActivity()).toolbar;
        toolbar.setTitle("Messages");
        tvToolbarEnd = ((ActivityMain)getActivity()).tvToolbarEnd;
        tvToolbarEnd.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onInvisible() {
        Log.e(TAG, "onInvisible");
        if(tvToolbarEnd != null)
            tvToolbarEnd.setVisibility(View.GONE);
    }
}
