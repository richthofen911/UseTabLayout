package io.ap1.proximity.view;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.proximity.R;
import io.ap1.proximity.Constants;

public class FragmentMap extends FragmentPreloadControl implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {
    private static final String TAG = "FragmentMap";

    private GoogleMap googleMap;

    private Intent intentBeaconUrlContent;

    GoogleApiClient mGoogleApiClient;

    public double mLatitude;
    public double mLongitude;

    protected Location mLastLocation;
    ArrayList<Marker> markers;

    private String url = "http://ap1.io"; // use ap1.io as default

    private Toolbar toolbar;
    private ArrayList<Beacon> myBeacons;
    private ArrayList<Beacon> allBeacons;
    private LinearLayout mapSwitch;
    private TextView tvMapMyBeacons;
    private TextView tvMapAllBeacons;

    public FragmentMap() {
        // Required empty public constructor
    }


    // method flow: GoogleApiClient.connect -> onConnected -> requestLastLocation -> getMapAsync -> onMapReady -> addMarkers -> setUpMap

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.e(TAG, "onMapReady");
        googleMap = map;

        tvMapMyBeacons.callOnClick();
    }

    private void setUpMap() {
        Log.e("map", "loading map and with last location coords");
        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        else {
            googleMap.setMyLocationEnabled(true);
            LatLng myLatLng = new LatLng(mLatitude, mLongitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                url = marker.getSnippet();
                marker.setSnippet(null);
                marker.showInfoWindow();
                return false;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                intentBeaconUrlContent.putExtra("url", url);
                startActivity(intentBeaconUrlContent);
            }
        });
    }

    private void requestLastLocation(){
        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
            }else{
                mLatitude = 43.7000000;  // coord of Toronto
                mLongitude = -79.4000000;
            }
            Log.e("location", "last location: " + String.valueOf(mLatitude) + " :: " + String.valueOf(mLongitude));
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("GoogleApiClient", "connected");
        Log.e("location", "requesting last location...");
        requestLastLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onResult(LocationSettingsResult result) {
        Log.e("arrive onResult", "");
        final Status status = result.getStatus();
        switch(status.getStatusCode()){
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can initialize location, request here
                requestLastLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
                    status.startResolutionForResult(getActivity(), 404); //404 here is just a requestCode for ActivityResult
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                break;
        }
    }

    private void reloadMarkers(ArrayList<Beacon> beaconsList){
        markers.clear(); // clear markers dataset
        googleMap.clear(); // clear markers on the map

        for(Beacon beacon : beaconsList){
            String tmpLat = beacon.getLat();
            String tmpLng = beacon.getLng();
            if((tmpLat != null && !tmpLat.equals("")) && (tmpLng != null && !tmpLng.equals(""))){
                LatLng tmpCoords = new LatLng(Double.parseDouble(tmpLat), Double.parseDouble(tmpLng));
                markers.add(googleMap.addMarker(new MarkerOptions().position(tmpCoords).title(beacon.getNickname() + "   >").snippet(beacon.getUrlfar())));
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPermission(String permissionName){ // The global Permission Handler methods cannot be recognized in this fragment
        return (ContextCompat.checkSelfPermission(getActivity(), permissionName) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission(String permissionName){ // The global Permission Handler methods cannot be recognized in this fragment
        ActivityCompat.requestPermissions(getActivity(), new String[]{permissionName}, Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.map);
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commitAllowingStateLoss(); // cannot use ft.commit(), because 'Can not perform this action after onSaveInstanceState'
    }

    @Override
    protected void lazyLoad(){
        Log.e(TAG, "onVisibleLazyLoad");
        //if(toolbar != null){
        markers = new ArrayList<>();
        toolbar = ((ActivityMain)getActivity()).toolbar;
        toolbar.setTitle("Map");
        //toolbar.getLayoutParams()
        //LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        mapSwitch = ((ActivityMain)getActivity()).mapSwitch;
        mapSwitch.setVisibility(View.VISIBLE);

        tvMapMyBeacons = (TextView) ((ActivityMain) getActivity()).mapSwitch.findViewById(R.id.tv_map_my_beacons);
        tvMapAllBeacons = (TextView) ((ActivityMain) getActivity()).mapSwitch.findViewById(R.id.tv_map_all_beacons);

        tvMapMyBeacons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.WHITE);
                v.setPadding(15, 3, 15, 3);
                ((TextView) v).setTextColor(getResources().getColor(R.color.light_blue));

                tvMapAllBeacons.setBackgroundResource(R.drawable.outline_switch);
                tvMapAllBeacons.setPadding(15, 3, 15, 3);
                tvMapAllBeacons.setTextColor(Color.WHITE);

                reloadMarkers(((ActivityMain) getActivity()).binderBeaconManagement.getMyBeacons());
                setUpMap();
            }
        });

        tvMapAllBeacons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.WHITE);
                v.setPadding(15, 3, 15, 3);
                ((TextView) v).setTextColor(getResources().getColor(R.color.light_blue));

                tvMapMyBeacons.setBackgroundResource(R.drawable.outline_switch);
                tvMapMyBeacons.setPadding(15, 3, 15, 3);
                tvMapMyBeacons.setTextColor(Color.WHITE);

                reloadMarkers(((ActivityMain) getActivity()).binderBeaconManagement.getBeaconInAllPlaces());
                setUpMap();
            }
        });

        intentBeaconUrlContent = new Intent(getActivity(), ActivityBeaconUrlContent.class);
        buildGoogleApiClient();
        if(!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onInvisible(){
        Log.e(TAG, "onInvisible");
        //if(toolbar != null)
        if(mapSwitch != null)
            mapSwitch.setVisibility(View.GONE);
            // toolbar.removeView(mapSwitch);
    }
}
