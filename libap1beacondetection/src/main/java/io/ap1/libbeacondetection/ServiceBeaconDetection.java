package io.ap1.libbeacondetection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ServiceBeaconDetection extends Service implements BeaconConsumer, RangeNotifier{
    private static final String TAG = "ServiceBeaconDetection";

    protected int rssiBorder;

    protected BeaconManager beaconManager;

    protected ArrayList<Region> definedRegions;
    protected Map<Beacon, Integer> concerningBeacons = new HashMap<>();

    protected LocalBroadcastManager localBroadcastManager;

    /**
     * Default constructor
     */
    public ServiceBeaconDetection() {
    }

    public class BinderBeaconDetection extends Binder {
        public ServiceBeaconDetection getService(){
            return ServiceBeaconDetection.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        applyScanConfig(intent);
        beaconManager.setRangeNotifier(this);
        Log.e(TAG, "binding BeaconManager");
        beaconManager.bind(this);

        return new BinderBeaconDetection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        applyScanConfig(intent);

        return START_STICKY;
    }

    /**
     * resolve the region descriptions passed from an Activity and set the rssi border
     * @param intent passed from an Activity binding this Service
     */
    protected void applyScanConfig(Intent intent){
        ArrayList<RegionDescription> regionDescriptions = new ArrayList<>();
        Gson gson = new Gson();

        Bundle bundle = intent.getExtras();
        // apply regions
        String[] regionArray = bundle.getStringArray("regionDescriptions");
        if(regionArray != null && regionArray.length > 0)
            for(String regionDescription : regionArray)
                regionDescriptions.add(gson.fromJson(regionDescription, RegionDescription.class));
        // apply beaconParsers
        int[] beaconParsers = bundle.getIntArray("beaconParsers");
        if(beaconParsers != null && beaconParsers.length > 0){
            for(int beaconParserCode : beaconParsers)
                beaconManager.getBeaconParsers().add(BeaconParserFactory.create(beaconParserCode));
        }
        // apply rssi border
        rssiBorder = bundle.getInt("rssiBorder");

        definedRegions = generateBeaconRegions(regionDescriptions);
    }

    /**
     * Implement the interface method from android-beacon-library
     * This is the callback of beaconManager.bind(BeaconConsumer)
     */
    @Override
    public void onBeaconServiceConnect(){
        Log.e(TAG, "BeaconScannerConnected");

        sendLocalBroadcast("ScannerConnected|empty");
    }

    /**
     * Send some status of this service to Activity
     * @param message message to be sent in String
     */
    final protected void sendLocalBroadcast(String message){
        localBroadcastManager.sendBroadcast(new Intent("beacon").putExtra("message", message));
    }

    /**
     * start scanning defined beacon regions
     */
    public void startScanning(){
        for(Region region : definedRegions){
            try{
                Log.e(TAG, "BeaconServiceConnected: start ranging for region: " + region.getId1() + "|" + region.getId2() + "|" + region.getId3());
                beaconManager.startRangingBeaconsInRegion(region);
            }catch (RemoteException e){
                Log.e(TAG, "BeaconServiceConnectError: " + e.toString());
            }
        }
    }

    /**
     * stop scanning beacon regions
     */
    public void stopScanning(){
        for(Region region : definedRegions){
            try{
                Log.e(TAG, "BeaconServiceConnected: stop ranging for region: " + region.getId1() + "|" + region.getId2() + "|" + region.getId3());
                beaconManager.stopRangingBeaconsInRegion(region);
            }catch (RemoteException e){
                Log.e(TAG, "BeaconServiceConnectError: " + e.toString());
            }
        }
    }

    /**
     * This method provides only beacons meeting the defined region conditions.
     * @param beacons Beacon class is defined by android-beacon-library
     * @param region Region class is defined by android-beacon-library
     */
    @Override
    final public synchronized void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(beacons.size() > 0){
            for(Beacon beacon : beacons){
                determineInOut(beacon.getRssi(), beacon);
            }
        }
    }

    /**
     * This method is called when a detected beacon's status is determine as IN by determineInOut method
     * @param beacon
     */
    protected abstract void actionOnEnter(Beacon beacon);

    /**
     * This method is called when a detected beacon's status is determine as OUT by determineInOut method
     * @param beacon
     */
    protected abstract void actionOnExit(Beacon beacon);

    /**
     * This method is called when a detected beacon's rssi changes. It is unnecessary for most situations
     * so a developer need to override the determineInOut method to use this.
     * @param index
     * @param newRssi
     */
    protected abstract void actionOnRssiChanged(int index, String newRssi);

    /**
     * Generate a list of Beacon Regions based on the region descriptions
     * @param regionDescriptions it defines a region's unique name, uuid, major and minor
     * @return
     */
    final protected ArrayList<Region> generateBeaconRegions(List<RegionDescription> regionDescriptions){
        ArrayList<Region> regions = new ArrayList<>();
        for(RegionDescription regionDescription : regionDescriptions){
            regions.add(new Region(
                    regionDescription.getUniqueName(),
                    regionDescription.getUuid(),
                    regionDescription.getMajor(),
                    regionDescription.getMinor()));
        }
        return regions;
    }

    /**
     * Determine if a beacon is IN/OUT the app's logic detection range by comparing its rssi with
     * it also use a HashMap to prevent duplicate IN
     * the defined rssi border
     * @param rssi
     * @param beacon
     */
    protected void determineInOut(int rssi, Beacon beacon){
        if(rssi > rssiBorder){ // if the beacon's rssi is strong enough as an ENTER signal
            if(!concerningBeacons.containsKey(beacon)){
                Log.e(TAG, "new found beacon: " + beacon.getId1() + "|" + beacon.getId2() + "|" + beacon.getId3());
                concerningBeacons.put(beacon, 0);
                actionOnEnter(beacon);
            }//else{ Log.e(TAG, "in region already");}
        }else {
            if(concerningBeacons.containsKey(beacon)){
                int exitCount = concerningBeacons.get(beacon);
                if(exitCount < 3)
                    concerningBeacons.put(beacon, ++exitCount);
                else{
                    concerningBeacons.remove(beacon);
                    actionOnExit(beacon);
                }
            }//else{Log.e(TAG, "determineInOut: find a beacon but the rssi is too weak, means not really interacting with it");}
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(beaconManager.isBound(this))
            beaconManager.unbind(this);

        super.onUnbind(intent);
        return false;
    }

    @Override
    public void onDestroy(){
        if(beaconManager.isBound(this))
            beaconManager.unbind(this);

        super.onDestroy();
    }
}
