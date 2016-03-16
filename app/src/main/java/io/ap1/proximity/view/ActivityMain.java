package io.ap1.proximity.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import io.ap1.libbeaconmanagement.ServiceBeaconManagement;
import io.ap1.libbeaconmanagement.Utils.CallBackUpdateBeaconSet;
import io.ap1.libbeaconmanagement.Utils.CallBackUpdateCompanySet;
import io.ap1.proximity.PermissionHandler;
import io.ap1.proximity.adapter.AdapterBeaconNearbyAdmin;
import io.ap1.proximity.adapter.AdapterBeaconNearbyUser;
import io.ap1.proximity.adapter.AdapterBeaconPlaces;
import io.ap1.proximity.adapter.AdapterFragmentPager;
import io.ap1.proximity.R;


public class ActivityMain extends AppCompatActivity{

    public static Intent intentShowBeaconUrlContent;
    public static Intent intentShowBeaconDetails;
    private static final int requestCodeFineLoc = 101;

    protected ServiceBeaconManagement.BinderManagement myBinder;

    private static final String UUID_AprilBrother = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    public ViewPager viewPager;
    public AdapterBeaconNearbyAdmin adapterBeaconNearbyAdmin;
    public AdapterBeaconNearbyUser adapterBeaconNearbyUser;
    public AdapterBeaconPlaces adapterBeaconPlaces;
    public AdapterFragmentPager adapterFragmentPager;
    public static final int rssiBorder = -80;

    private ServiceConnection conn;

    public BluetoothAdapter mBluetoothAdapter = null;

    public Toolbar toolbar;
    public LinearLayout mapSwitch;
    public TextView tvToolbarEnd;

    private String userObjectId;

    public boolean isAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userObjectId = getIntent().getStringExtra("userObjectId");

        adapterBeaconNearbyAdmin = new AdapterBeaconNearbyAdmin();
        adapterBeaconNearbyUser = new AdapterBeaconNearbyUser();
        adapterBeaconPlaces = new AdapterBeaconPlaces();
        adapterFragmentPager = new AdapterFragmentPager(getSupportFragmentManager());

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        else {
            //mBluetoothAdapter.getBluetoothLeAdvertiser();
            //AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()

        }

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Service FindBeacon", "Connected");
                myBinder = (ServiceBeaconManagement.BinderManagement) service;
                myBinder.getIdparent();
                //myBinder.setListAdapter(adapterBeaconNearbyAdmin);
                myBinder.setListAdapter(adapterBeaconNearbyUser);

                final ProgressDialog progCheckCompany = android.app.ProgressDialog.show(ActivityMain.this, "Update Company Data", "Please wait", true);
                myBinder.getRemoteCompanyHash("/getAllCompanies_a.php", new CallBackUpdateCompanySet() {
                    @Override
                    public void onSuccess() {
                        progCheckCompany.dismiss();

                        final ProgressDialog progressDialog = android.app.ProgressDialog.show(ActivityMain.this, "Update Beacon Data", "Please Wait", true);
                        myBinder.getRemoteBeaconHash("/getAllBeacons_a.php", new CallBackUpdateBeaconSet() {
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

        if(!PermissionHandler.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            PermissionHandler.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }else {
            bindServiceBeaconManagement();
        }

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
                    startActivity(new Intent(ActivityMain.this, ActivitySettings.class).putExtra("userObjectId", userObjectId));
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

    protected void bindServiceBeaconManagement(){
        Log.e("trying to bind", "Service BeaconManagement");
        Bundle beaconInfo = new Bundle();
        beaconInfo.putString("uuid", UUID_AprilBrother);
        beaconInfo.putInt("major", -1);
        beaconInfo.putInt("minor", -1);
        beaconInfo.putInt("borderValue", rssiBorder);
        beaconInfo.putBoolean("useGeneralSearchMode", true);
        //beaconInfo.putString("idparent", "11");
        bindService(new Intent(ActivityMain.this, ServiceBeaconManagement.class).putExtras(beaconInfo), conn, BIND_AUTO_CREATE);
    }

    protected void unbindServiceBeaconManagement(){
        if(myBinder != null && myBinder.isBinderAlive())
            unbindService(conn);
    }

    public void startScanning(){
        if(myBinder != null && myBinder.isBinderAlive())
            myBinder.startScanning();
    }

    public void stopScanning(){
        if(myBinder != null && myBinder.isBinderAlive())
            myBinder.stopScanning();
    }

    public void updateCompanySet(String apiPath, CallBackUpdateCompanySet callBackUpdateCompanySet){
        if(myBinder != null && myBinder.isBinderAlive())
            myBinder.getRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
    }

    public void updateBeaconSet(String apiPath, CallBackUpdateBeaconSet callBackUpdateBeaconSet){
        if(myBinder != null && myBinder.isBinderAlive())
            myBinder.getRemoteBeaconHash(apiPath, callBackUpdateBeaconSet);
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
        unbindServiceBeaconManagement();
        super.onDestroy();
    }

}
