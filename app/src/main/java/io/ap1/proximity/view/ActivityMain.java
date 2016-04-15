package io.ap1.proximity.view;

import android.Manifest;
import android.app.ProgressDialog;
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
import com.pubnub.api.Pubnub;
import com.squareup.picasso.Picasso;

import io.ap1.libbeaconmanagement.ServiceBeaconManagement;
import io.ap1.libbeaconmanagement.Utils.CallBackSyncData;
import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.AppPubsubCallback;
import io.ap1.proximity.Constants;
import io.ap1.proximity.DefaultBackendlessCallback;
import io.ap1.proximity.MyPubsubProviderClient;
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

    public static Intent intentShowBeaconUrlContent;
    public static Intent intentShowBeaconDetails;

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

    protected ServiceBeaconManagement.BinderManagement binderBeaconManagement;
    public ServiceMessageCenter.BinderMsgCenter binderMsgCenter;

    public MyPubsubProviderClient myPubsubProviderClient;

    private static final String UUID_AprilBrother = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    public ViewPager viewPager;
    public AdapterBeaconNearbyAdmin adapterBeaconNearbyAdmin;
    public AdapterBeaconNearbyUser adapterBeaconNearbyUser;
    public AdapterBeaconPlaces adapterBeaconPlaces;
    public AdapterFragmentPager adapterFragmentPager;
    public static final int rssiBorder = -80;

    private ServiceConnection connBeaconManagement;
    private ServiceConnection connChat;

    String btNameOrigin = "unknown";
    private String myProximityDeviceName;
    private BroadcastReceiver mReceiver;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myUserObjectId = getIntent().getStringExtra("userObjectId");
        spOriginalBTName = getApplication().getSharedPreferences("originalBTName", 0);

        adapterBeaconNearbyAdmin = new AdapterBeaconNearbyAdmin();
        adapterBeaconNearbyUser = new AdapterBeaconNearbyUser(this);
        adapterBeaconPlaces = new AdapterBeaconPlaces(this);
        adapterFragmentPager = new AdapterFragmentPager(getSupportFragmentManager());

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
                spOriginalBTName.edit().putString("originalBTName", btName);
                changeBTNameForThisApp(myUserObjectId);
            }

            ensureDiscoverable();
        }

        mReceiver = new BroadcastReceiver() {
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
                    //discoverDevices();
                    Log.e("device discovery", "finished");
                }
            }
        };

        connBeaconManagement = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Service FindBeacon", "Connected");
                binderBeaconManagement = (ServiceBeaconManagement.BinderManagement) service;
                binderBeaconManagement.getIdparent();

                binderBeaconManagement.setListAdapter(adapterBeaconNearbyUser);

                final ProgressDialog progCheckCompany = android.app.ProgressDialog.show(ActivityMain.this, "Update Company Data", "Please wait", true);
                binderBeaconManagement.getRemoteCompanyHash("/getAllCompanies_a.php", new CallBackSyncData() {
                    @Override
                    public void onSuccess() {
                        progCheckCompany.dismiss();

                        final ProgressDialog progressDialog = android.app.ProgressDialog.show(ActivityMain.this, "Update Beacon Data", "Please Wait", true);
                        binderBeaconManagement.getRemoteBeaconHash("/getAllBeaconsv5_a.php", new CallBackSyncData() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();
                                startScanning();
                            }

                            @Override
                            public void onFailure(String cause) {
                                progressDialog.dismiss();
                                Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                                Log.e("update beacon hash err", cause);
                            }
                        });
                        progressDialog.setCancelable(true);
                    }

                    @Override
                    public void onFailure(String cause) {
                        progCheckCompany.dismiss();
                        Toast.makeText(ActivityMain.this, cause, Toast.LENGTH_SHORT).show();
                        Log.e("update company data err", cause);
                    }
                });
                progCheckCompany.setCancelable(true);
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
                discoverDevices();
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

        intentShowBeaconUrlContent = new Intent(ActivityMain.this, ActivityBeaconUrlContent.class);
        intentShowBeaconUrlContent = new Intent(ActivityMain.this, ActivityBeaconDetail.class);
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

    private void discoverDevices(){
        if(!mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.startDiscovery();
    }

    protected void bindServiceBeaconManagement(){
        Log.e("trying to bind", "Service BeaconManagement");
        Bundle beaconInfo = new Bundle();
        beaconInfo.putString("uuid", UUID_AprilBrother);
        beaconInfo.putInt("major", -1);
        beaconInfo.putInt("minor", -1);
        beaconInfo.putInt("borderValue", rssiBorder);
        beaconInfo.putBoolean("useGeneralSearchMode", true);
        //beaconInfo.putString("idparent", "11");
        bindService(new Intent(ActivityMain.this, ServiceBeaconManagement.class).putExtras(beaconInfo), connBeaconManagement, BIND_AUTO_CREATE);
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
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
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
            binderBeaconManagement.startScanning();
    }

    public void stopScanning(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.stopScanning();
    }

    public void updateCompanySet(String apiPath, CallBackSyncData callBackUpdateCompanySet){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.getRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
    }

    public void updateBeaconSet(String apiPath, CallBackSyncData callBackSyncData){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.getRemoteBeaconHash(apiPath, callBackSyncData);
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
            unregisterReceiver(mReceiver);
        }catch (Exception e){
            Log.e(TAG, "mReceiver was not registered, cannot unregister it");
        }
    }
}
