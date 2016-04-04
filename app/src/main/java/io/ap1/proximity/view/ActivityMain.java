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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.Pubnub;

import io.ap1.libbeaconmanagement.ServiceBeaconManagement;
import io.ap1.libbeaconmanagement.Utils.CallBackUpdateBeaconSet;
import io.ap1.libbeaconmanagement.Utils.CallBackUpdateCompanySet;
import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.AppPubsubCallback;
import io.ap1.proximity.MyBackendlessUser;
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
    private static final int requestCodeFineLoc = 101;

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

    private String myUserObjectId;


    public boolean isAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myUserObjectId = getIntent().getStringExtra("userObjectId");

        adapterBeaconNearbyAdmin = new AdapterBeaconNearbyAdmin();
        adapterBeaconNearbyUser = new AdapterBeaconNearbyUser(this);
        adapterBeaconPlaces = new AdapterBeaconPlaces(this);
        adapterFragmentPager = new AdapterFragmentPager(getSupportFragmentManager());

        adapterUserInList = new AdapterUserInList(this);
        adapterUserInList.registerAdapterDataObserver(ActivityMain.adapterDataObserver);

        myPubsubProviderClient = new MyPubsubProviderClient(new Pubnub("pub-c-af13868a-beb9-4719-82fc-8518ddfacea8", "sub-c-48ef81b4-f118-11e5-8f88-0619f8945a4f"));
        appPubsubCallback = new AppPubsubCallback(this, myUserObjectId, adapterUserInList, TAG);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        else {
            changeBTNameForThisApp(myUserObjectId);
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
                binderBeaconManagement.getRemoteCompanyHash("/getAllCompanies_a.php", new CallBackUpdateCompanySet() {
                    @Override
                    public void onSuccess() {
                        progCheckCompany.dismiss();

                        final ProgressDialog progressDialog = android.app.ProgressDialog.show(ActivityMain.this, "Update Beacon Data", "Please Wait", true);
                        binderBeaconManagement.getRemoteBeaconHash("/getAllBeaconsv5_a.php", new CallBackUpdateBeaconSet() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();
                                if(!PermissionHandler.checkPermission(ActivityMain.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                                    PermissionHandler.requestPermission(ActivityMain.this, Manifest.permission.ACCESS_FINE_LOCATION);
                                }else {
                                    startScanning();
                                }
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
                        Log.e("update beacon hash err", cause);
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

        if(!PermissionHandler.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            PermissionHandler.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }else {
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

        setUpDrawer();

        intentShowBeaconUrlContent = new Intent(ActivityMain.this, ActivityBeaconUrlContent.class);
        intentShowBeaconUrlContent = new Intent(ActivityMain.this, ActivityBeaconDetail.class);
    }

    protected void setUpDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    startActivity(new Intent(ActivityMain.this, ActivitySettings.class).putExtra("userObjectId", myUserObjectId));
                } else if (id == R.id.nav_logout) {

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
        if(binderMsgCenter != null && binderMsgCenter.isBinderAlive())
            unbindService(connChat);
    }

    public void startScanning(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.startScanning();
    }

    public void stopScanning(){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.stopScanning();
    }

    public void updateCompanySet(String apiPath, CallBackUpdateCompanySet callBackUpdateCompanySet){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.getRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
    }

    public void updateBeaconSet(String apiPath, CallBackUpdateBeaconSet callBackUpdateBeaconSet){
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            binderBeaconManagement.getRemoteBeaconHash(apiPath, callBackUpdateBeaconSet);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case requestCodeFineLoc: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindServiceBeaconManagement();
                } else {
                    Toast.makeText(this, "no permission to run this app", Toast.LENGTH_SHORT).show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
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
            mBluetoothAdapter.setName(btNameOrigin);
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
