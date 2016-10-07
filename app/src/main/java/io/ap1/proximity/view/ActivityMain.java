package io.ap1.proximity.view;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;
import com.pubnub.api.Pubnub;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.ap1.libap1beaconmngt.Ap1Beacon;
import io.ap1.libap1beaconmngt.BeaconOperation;
import io.ap1.libap1beaconmngt.CallBackSyncData;
import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.libap1beaconmngt.DatabaseHelper;
import io.ap1.libbeacondetection.BeaconParserType;
import io.ap1.libbeacondetection.RegionDescription;
import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.AppPubsubCallback;
import io.ap1.proximity.Constants;
import io.ap1.proximity.DefaultBackendlessCallback;
import io.ap1.proximity.MyProgressDialog;
import io.ap1.proximity.MyPubsubProviderClient;
import io.ap1.proximity.MyServiceBeaconMngt;
import io.ap1.proximity.PermissionHandler;
import io.ap1.proximity.ServiceMessageCenter;
import io.ap1.proximity.adapter.AdapterBeaconNearbyAdmin;
import io.ap1.proximity.adapter.AdapterBeaconNearbyUser;
import io.ap1.proximity.adapter.AdapterBeaconPlaces;
import io.ap1.proximity.adapter.AdapterFragmentPager;
import io.ap1.proximity.R;
import io.ap1.proximity.adapter.AdapterUserInList;


public class ActivityMain extends AppCompatActivity{
    private static final String TAG = "ActivityMain";

    public static String PACKAGE_NAME = "undefined";
    public static String loginUsername = "undefined";

    public static ActivityMain instance = null;

    // the app should show beacons with rssi larger than -100, and treat those rssi largenr than -60 as NEAR

    // this is just for debugging the user adapter notify() method
    public static RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);

            String className = "unknown";
            if(payload != null)
                className = payload.getClass().getSimpleName();

            //Log.e("onSuccessReceive, ", "onItemRangeChanged: " + positionStart + "->" + itemCount + "::" + ((MyBackendlessUser) payload).getUnreadMessageList().size());
            Log.e("onSuccessReceive, ", "onItemRangeChanged: " + positionStart + "->" + itemCount + "::" + className);
        }

        @Override
        public void onChanged() {
            super.onChanged();
            Log.e("onSuccessReceive, ", "onChanged");
        }
    };

    protected MyServiceBeaconMngt.BinderMyBeaconMngt binderBeaconManagement;
    MyServiceBeaconMngt serviceMyBeaconMngt;
    public ServiceMessageCenter.BinderMsgCenter binderMsgCenter;

    public MyPubsubProviderClient myPubsubProviderClient;

    //private static final String UUID_AprilBrother = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    public ViewPager viewPager;
    public AdapterBeaconNearbyAdmin adapterBeaconNearbyAdmin;
    public AdapterBeaconNearbyUser adapterBeaconNearbyUser;
    public AdapterBeaconPlaces adapterBeaconPlaces;
    public AdapterFragmentPager adapterFragmentPager;
    public static final int rssiBorder = -100;

    private ServiceConnection connBeaconManagement;
    private ServiceConnection connChat;

    String btNameOrigin = "unknown";
    private String myProximityDeviceName;
    private BroadcastReceiver mBTReceiver; // receive broadcast when find a bluetooth-ON phone, not for beacons.
    public boolean isReadyToDiscoverDevices = false; // to find other users, not beacons
    public boolean isReadyToScan = false; // to find beacons
    public boolean isSyncDataDone = false;
    public boolean isScanning = false;

    public AdapterUserInList adapterUserInList;

    public BluetoothAdapter mBluetoothAdapter = null;

    public AppPubsubCallback appPubsubCallback;

    public Toolbar toolbar;
    public LinearLayout mapSwitch;
    public TextView tvToolbarEnd;

    private BackendlessUser myUserObject;
    private String myUserObjectId;

    // use a persisted file to keep bluetooth name. if the app crash, the bluetooth cannot be resumed
    // this time but it will be resumed next time.
    private SharedPreferences spOriginalBTName;

    private DatabaseHelper databaseHelper;

    private BroadcastReceiver mLocalReceiver; // used to receive local broadcast for Service-Activity interaction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        PACKAGE_NAME = getApplicationContext().getPackageName();

        myUserObjectId = getIntent().getStringExtra("userObjectId");
        loginUsername = getIntent().getStringExtra("username");
        spOriginalBTName = getApplication().getSharedPreferences("originalBTName", 0);

        adapterBeaconNearbyAdmin = new AdapterBeaconNearbyAdmin();
        adapterBeaconNearbyUser = new AdapterBeaconNearbyUser(this);
        adapterBeaconPlaces = new AdapterBeaconPlaces(this);
        adapterFragmentPager = new AdapterFragmentPager(getSupportFragmentManager());

        databaseHelper = DatabaseHelper.getHelper(this);

        adapterUserInList = new AdapterUserInList(this);
        adapterUserInList.registerAdapterDataObserver(ActivityMain.adapterDataObserver);

        myPubsubProviderClient = new MyPubsubProviderClient(new Pubnub("pub-c-af13868a-beb9-4719-82fc-8518ddfacea8", "sub-c-48ef81b4-f118-11e5-8f88-0619f8945a4f"));
        appPubsubCallback = new AppPubsubCallback(this, myUserObjectId, adapterUserInList, TAG);

        getUserData();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        else {
            String btName = mBluetoothAdapter.getName();
            if(!btName.startsWith("proximity/")){
                spOriginalBTName.edit().putString("originalBTName", btName).commit();
                changeBTNameForThisApp(myUserObjectId);
            }

            ensureDiscoverable();
        }

        mLocalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String rawMessage = intent.getStringExtra("message");
                String[] msgFormatted = rawMessage.split("\\|");
                switch (msgFormatted[0]){
                    case "ScannerConnected":
                        isReadyToScan = true;
                        if(isSyncDataDone && !isScanning){
                            isScanning = true;
                            serviceMyBeaconMngt.startScanning();
                        }
                        break;
                    case "ScannerDisconnected":
                        isReadyToScan = false;
                        break;
                    default:
                        break;
                }
            }
        };

        mBTReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String detectedDeviceName = device.getName();
                    if(detectedDeviceName != null && detectedDeviceName.startsWith("proximity/")) {
                        Log.e("proximity user found", device.getName() + "\n" + device.getAddress());
                        binderMsgCenter.getUserObjectByObjectId(getTargetUserObjectId(detectedDeviceName));
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.e("device discovery", "finished");
                }
            }
        };

        connBeaconManagement = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Service FindBeacon", "Connected");
                binderBeaconManagement = (MyServiceBeaconMngt.BinderMyBeaconMngt) service;
                //binderBeaconManagement.getIdparent();
                serviceMyBeaconMngt = binderBeaconManagement.getService();

                //FragmentManager fragmentManager = getSupportFragmentManager();
                //fragmentManager.findFragmentByTag()
                ((FragmentNearby)adapterFragmentPager.getItem(1)).beaconManagementService = serviceMyBeaconMngt;
                //if(!serviceMyBeaconMngt.isRecyclerViewFromOutsideSet())
                serviceMyBeaconMngt.setRecyclerView(((FragmentNearby)adapterFragmentPager.getItem(1)).accessRecyclerView());

                serviceMyBeaconMngt.setListAdapter(adapterBeaconNearbyUser);

                MyProgressDialog.show(ActivityMain.this, "Updating Company Data...");
                serviceMyBeaconMngt.checkRemoteCompanyHash(Constants.API_PATH_GET_COMPANIES, new CallBackSyncData() {
                    @Override
                    public void onSuccess() {
                        MyProgressDialog.dismissDialog();
                        MyProgressDialog.show(ActivityMain.this, "Updating Beacon Data...");
                        serviceMyBeaconMngt.checkRemoteBeaconHash(Constants.API_PATH_GET_BEACONS, new CallBackSyncData() {
                            @Override
                            public void onSuccess() {
                                MyProgressDialog.dismissDialog();
                                isSyncDataDone = true;
                                if(isReadyToScan && !isScanning)
                                    startScanning();
                            }

                            @Override
                            public void onFailure(String cause) {
                                MyProgressDialog.dismissDialog();
                                Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                                Log.e("update beacon hash err", cause);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String cause) {
                        MyProgressDialog.dismissDialog();
                        Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                        Log.e("update company data err", cause);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service BeaconMngt", "Disconnected");
            }
        };

        connChat = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Service MessageCenter", "Connected");
                binderMsgCenter = (ServiceMessageCenter.BinderMsgCenter) service;

                binderMsgCenter.setMyAdapterUserInList(adapterUserInList);
                registerMyReceiver(ActivityMain.this);
                isReadyToDiscoverDevices = true;
                binderMsgCenter.setSubChannel("proximity_" + myUserObjectId);
                binderMsgCenter.setPubsubProviderClient(myPubsubProviderClient);
                binderMsgCenter.setPubsubCallback(appPubsubCallback);

                binderMsgCenter.subToChannel();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service MessageCenter", "Disconnected");
            }
        };

        String[] notGrantedPermission = PermissionHandler.checkPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        if(notGrantedPermission != null)
            PermissionHandler.requestPermissions(this, notGrantedPermission, Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION);
        else {
            bindServiceMsgCenter();
            bindServiceBeaconManagement();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitleTextColor(Color.WHITE);
        mapSwitch = (LinearLayout) findViewById(R.id.map_switch);
        tvToolbarEnd = (TextView) findViewById(R.id.tv_toolbar_end);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //viewPager.setOffscreenPageLimit(1);  // ***** prohibit pre-loading
        viewPager.setAdapter(adapterFragmentPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        //tabLayout.setBackgroundColor(Color.WHITE);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.mipmap.tab_icon_chat);
        tabLayout.getTabAt(1).setIcon(R.mipmap.tab_icon_all);
        tabLayout.getTabAt(2).setIcon(R.mipmap.tab_icon_places);
        tabLayout.getTabAt(3).setIcon(R.mipmap.tab_icon_map);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.e(TAG, "onResume: register local receiver");
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, new IntentFilter("beacon"));
    }

    @Override
    protected void onStop(){
        super.onStop();

        Log.e(TAG, "onStop: onStop: unregister local receiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
    }


    private void getUserData(){
        Backendless.Persistence.of(BackendlessUser.class).findById(myUserObjectId, new DefaultBackendlessCallback<BackendlessUser>(this, TAG, "Pulling User Data...") {
            @Override
            public void handleResponse(BackendlessUser response) {
                super.handleResponse(response);

                myUserObject = response;
                setUpDrawer();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                Log.e("Handle fault", fault.toString());
            }
        });
    }

    protected void setUpDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        int headers = navigationView.getHeaderCount();
        if(headers > 0){
            if(myUserObject != null){
                Log.e(TAG, "setUpDrawer: " + myUserObject.getProperty("profileImage") + myUserObject.getProperty("nickname") + myUserObject.getProperty("email"));
                LinearLayout headerView = (LinearLayout)navigationView.getHeaderView(0);
                ImageView headerImage = (ImageView) headerView.findViewById(R.id.iv_drawer_header_image);
                Picasso.with(ActivityMain.this).load(Constants.PROFILE_IMAGE_PATH_ROOT + myUserObject.getProperty("profileImage")).into(headerImage);
                ((TextView) headerView.findViewById(R.id.tv_drawer_header_name)).setText((String)myUserObject.getProperty("nickname"));
                ((TextView) headerView.findViewById(R.id.tv_drawer_header_email)).setText((String)myUserObject.getProperty("email"));
            }else
                Toast.makeText(this, "MyUserObject is null", Toast.LENGTH_SHORT).show();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    startActivity(new Intent(ActivityMain.this, ActivitySettings.class).putExtra("userObjectId", myUserObjectId));
                } else if (id == R.id.nav_logout) {
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserInfo", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.USER_LOGIN_KEY_LOGINNAME, null);
                    editor.putString(Constants.USER_LOGIN_KEY_LOGINPASSWORD, null);
                    editor.apply();
                    startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
                    finish();
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    // override onBackPressed() here to handle swiping out the drawer
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void discoverDevices(){
        if(!mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.startDiscovery();
    }

    protected void bindServiceBeaconManagement(){
        Log.e("trying to bind", "Service BeaconManagement");
        RegionDescription regionGeneral = new RegionDescription("RegionGeneral", null, -1, -1);
        Gson gson = new Gson();
        String jsonObj1 = gson.toJson(regionGeneral);
        String[] regionDescriptions = new String[]{jsonObj1};
        int[] beaconParsers = new int[]{BeaconParserType.IBEACON};

        Bundle beaconInfo = new Bundle();
        beaconInfo.putString("idbundle", PACKAGE_NAME);
        beaconInfo.putStringArray("regionDescriptions", regionDescriptions);
        beaconInfo.putIntArray("beaconParsers", beaconParsers);
        beaconInfo.putInt("rssiBorder", -100);
        bindService(new Intent(ActivityMain.this, MyServiceBeaconMngt.class).putExtras(beaconInfo), connBeaconManagement, BIND_AUTO_CREATE);
    }

    private void bindServiceMsgCenter(){
        Log.e("trying to bind", "Service MessageCenter");
        Bundle bundle = new Bundle();
        bundle.putString("myUserObjectId", myUserObjectId);
        bindService(new Intent(ActivityMain.this, ServiceMessageCenter.class).putExtras(bundle), connChat, BIND_AUTO_CREATE);
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        }
    }

    private void changeBTNameForThisApp(String myUserObjectId){
        btNameOrigin = mBluetoothAdapter.getName();
        myProximityDeviceName = "proximity/" + myUserObjectId;
        mBluetoothAdapter.setName(myProximityDeviceName);
    }

    private void registerMyReceiver(Context context) {
        context.registerReceiver(mBTReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(mBTReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    private String getTargetUserObjectId(String targetDeviceName){
        String[] components = targetDeviceName.split("/");
        return components[1];
    }

    protected void unbindServiceBeaconManagement(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            unbindService(connBeaconManagement);
    }

    protected void unbindServiceChat(){
        if(binderMsgCenter != null && binderMsgCenter.isBinderAlive()){
            binderMsgCenter.unsubAll();
            unbindService(connChat);
        }
    }

    public void startScanning(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            serviceMyBeaconMngt.startScanning();
    }

    public void stopScanning(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            serviceMyBeaconMngt.stopScanning();
    }

    /*
    public void updateCompanySet(String apiPath, CallBackSyncData callBackUpdateCompanySet){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            serviceMyBeaconMngt.checkRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
    }

    public void updateBeaconSet(String apiPath, CallBackSyncData callBackSyncData){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            serviceMyBeaconMngt.checkRemoteBeaconHash(apiPath, callBackSyncData);
    }
    */

    public void clearRecyclerViewData(ArrayList arrayList, RecyclerView.Adapter adapter){
        int size = arrayList.size();
        arrayList.clear();
        /*
        ((FragmentNearby)adapterFragmentPager.getItem(1)).accessRecyclerView().getRecycledViewPool().clear();
        for(int i = 0; i < size; i++){
            if(size > 0)
                arrayList.remove(i);
            adapter.notifyItemRangeRemoved(0, size);
        }
*/
        adapter.notifyDataSetChanged();
    }

    public void updateDataAfterBeaconAddDel(final Intent data){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive()){
            MyProgressDialog.show(ActivityMain.this, "Updating Company Data...");
            serviceMyBeaconMngt.checkRemoteCompanyHash(Constants.API_PATH_GET_COMPANIES, new CallBackSyncData() {
                @Override
                public void onSuccess() {
                    MyProgressDialog.dismissDialog();
                    MyProgressDialog.show(ActivityMain.this, "Updating Beacon Data...");
                    serviceMyBeaconMngt.checkRemoteBeaconHash(Constants.API_PATH_GET_BEACONS, new CallBackSyncData() {
                        @Override
                        public void onSuccess() {
                            MyProgressDialog.dismissDialog();

                            isSyncDataDone = true;

                            String action = data.getStringExtra("action");
                            int position = data.getIntExtra("position", 0);
                            if(action.equals("add")){
                                Ap1Beacon newAddedBeacon = new Ap1Beacon();
                                newAddedBeacon.setUuid(data.getStringExtra("uuid"));
                                newAddedBeacon.setMajor(data.getStringExtra("major"));
                                newAddedBeacon.setMinor(data.getStringExtra("minor"));

                                List<Ap1Beacon> beaconQueried = databaseHelper.queryBeacons(newAddedBeacon);
                                Ap1Beacon queryResult = null;
                                if(beaconQueried != null) {
                                    // if beaconQueried is in local db, add it to detected&registered list
                                    queryResult = beaconQueried.get(0);
                                    String nickname = queryResult.getNickname();
                                    if (nickname == null)
                                        queryResult.setNickname("undefined"); // it means the beacon is in Ap1 DB but the name was not given
                                    else
                                        queryResult.setNickname(nickname);
                                    queryResult.setUrlfar(queryResult.getUrlfar());
                                    queryResult.setUrlnear(queryResult.getUrlnear());
                                }
                                if(queryResult != null){
                                    DataStore.detectedBeaconList.set(position, queryResult);
                                    adapterBeaconNearbyAdmin.notifyItemChanged(position, queryResult);
                                    DataStore.detectedAndAddedBeaconList.add(queryResult);
                                    adapterBeaconNearbyUser.notifyItemInserted(DataStore.detectedAndAddedBeaconList.size() - 1);
                                }
                            }else if(action.equals("del")){
                                Ap1Beacon newEmptyBeacon = new Ap1Beacon();
                                newEmptyBeacon.setUuid(data.getStringExtra("uuid"));
                                newEmptyBeacon.setMajor(data.getStringExtra("major"));
                                newEmptyBeacon.setMinor(data.getStringExtra("minor"));
                                newEmptyBeacon.setRssi(data.getStringExtra("rssi"));
                                DataStore.detectedBeaconList.set(position, newEmptyBeacon);
                                adapterBeaconNearbyAdmin.notifyItemChanged(position, newEmptyBeacon);

                                for(int i = 0; i < DataStore.detectedAndAddedBeaconList.size(); i++){
                                    if(BeaconOperation.equals(newEmptyBeacon, DataStore.detectedAndAddedBeaconList.get(i))){
                                        Log.e(TAG, "onSuccess: find added one to remove: " + i);
                                        DataStore.detectedAndAddedBeaconList.remove(i);
                                        adapterBeaconNearbyUser.notifyItemRemoved(i);
                                        //adapterBeaconNearbyUser.notifyItemRangeChanged(0, DataStore.detectedAndAddedBeaconList.size() - 1);
                                        break;
                                    }
                                }

                            }

                            startScanning();
                        }

                        @Override
                        public void onFailure(String cause) {
                            MyProgressDialog.dismissDialog();
                            Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                            Log.e("update beacon hash err", cause);
                        }
                    });
                }

                @Override
                public void onFailure(String cause) {
                    MyProgressDialog.dismissDialog();
                    Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                    Log.e("update company data err", cause);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.INTENT_REQUEST_CODE_AD_BEACON:
                if(resultCode == RESULT_OK){
                    DataStore.registeredAndGroupedBeaconList.clear();
                    Log.e(TAG, "onActivityResult: updating beacon list after beacon AD");

                    //clearRecyclerViewData(DataStore.detectedBeaconList, adapterBeaconNearbyAdmin);
                    //clearRecyclerViewData(DataStore.detectedAndAddedBeaconList, adapterBeaconNearbyUser);
                    //((FragmentNearby)adapterFragmentPager.getItem(1)).accessRecyclerView().swapAdapter(adapterEmpty, true);
                    updateDataAfterBeaconAddDel(data);
                    //((FragmentNearby)adapterFragmentPager.getItem(1)).accessRecyclerView().swapAdapter(adapterBeaconNearbyAdmin, true);

                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindServiceBeaconManagement();
                } else {
                    Snackbar.make(toolbar, "Location permission is required to display beacon detection results and to show the beacon map",
                            Snackbar.LENGTH_SHORT)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PermissionHandler.requestPermissions(ActivityMain.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION);
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        serviceMyBeaconMngt.stopScanning();
        isScanning = false;

        DataStore.detectedBeaconList.clear();
        DataStore.detectedAndAddedBeaconList.clear();

        unbindServiceChat();
        unbindServiceBeaconManagement();
        appPubsubCallback = null;

        if (mBluetoothAdapter != null){
            mBluetoothAdapter.setName(spOriginalBTName.getString("originalBTName", "MyDevice"));
            if(mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
        }

        AppDataStore.userList.clear();
        AppDataStore.duplicateCheck.clear();
        adapterUserInList.notifyItemChanged(0, AppDataStore.userList.size() - 1);
        adapterUserInList.unregisterAdapterDataObserver(adapterDataObserver);

        try{
            unregisterReceiver(mBTReceiver);
        }catch (Exception e){
            Log.e(TAG, "mBTReceiver was not registered, cannot unregister it");
        }
    }
}
