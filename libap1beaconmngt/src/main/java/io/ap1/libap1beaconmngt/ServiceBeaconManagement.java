package io.ap1.libap1beaconmngt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import io.ap1.libbeacondetection.ServiceBeaconDetection;

public abstract class ServiceBeaconManagement<T extends RecyclerView.Adapter> extends ServiceBeaconDetection {
    private static final String TAG = "ServiceBeaconMngt";

    protected SharedPreferences spHashValue;

    protected String idBundle = "undefined";

    protected boolean needToResort = false;
    protected boolean needToResetNearbyStatus = false;

    protected Timer timerResortList;

    protected DatabaseHelper databaseHelper;

    private T t;
    protected String currentAdapter = "";
    protected static final String adapterTypeAdmin = "AdapterBeaconNearbyAdmin";
    protected static final String adapterTypeUser = "AdapterBeaconNearbyUser";

    protected HandlerThread handlerThread;
    protected Handler handler;

    protected RecyclerView recyclerViewFromOutside;
    protected boolean isRecyclerViewFromOutsideSet = false;

    public ServiceBeaconManagement() {
    }

    public class BinderBeaconManagement extends Binder {
        public ServiceBeaconManagement getService(){
            return ServiceBeaconManagement.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        spHashValue = getApplication().getSharedPreferences("HashValue.sp", 0);
        databaseHelper = DatabaseHelper.getHelper(this);

        handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        idBundle = intent.getExtras().getString("idbundle");

        return new BinderBeaconManagement();
    }

    @Override
    protected void actionOnEnter(Beacon beacon){
        Ap1Beacon detectedBeacon = new Ap1Beacon();
        detectedBeacon.setUuid(beacon.getId1().toString());
        detectedBeacon.setMajor(String.valueOf(beacon.getId2()));
        detectedBeacon.setMinor(String.valueOf(beacon.getId3()));
        //Log.e(TAG, "inOut: new detected Rssi: " + recoBeacon.getRssi());
        detectedBeacon.setRssi(String.valueOf(beacon.getRssi()));

        boolean isNewDetectedAp1Beacon = true;
        int index = -1;  // if the index stays as -1, it means the detected beacon is
        String rssiNew = null;

        // check if the beacon has been in the detected list already or not
        for(int i = 0; i < DataStore.detectedBeaconList.size(); i++){
            Ap1Beacon beaconInDetectedList = DataStore.detectedBeaconList.get(i);
            if(BeaconOperation.equals(detectedBeacon, beaconInDetectedList)){
                isNewDetectedAp1Beacon = false;

                String rssiThisTime = detectedBeacon.getRssi();
                String rssiLastTime = beaconInDetectedList.getRssi();
                if(rssiLastTime.equals(""))
                    rssiLastTime = "-60";
                if(Math.abs(Integer.parseInt(rssiThisTime) - Integer.parseInt(rssiLastTime)) > 3){
                    //Log.e("but rssi", "has changed, need to resort list");
                    index = i;
                    rssiNew = detectedBeacon.getRssi();
                }
                break;
            }
        }
        if(isNewDetectedAp1Beacon)
            actionOnEnterAp1Beacon(detectedBeacon);
        else {
            if(index != -1 && rssiNew != null)
                actionOnRssiChanged(index, rssiNew);
        }
    }

    @Override
    protected void actionOnExit(Beacon beacon){

    }

    protected void actionOnEnterAp1Beacon(Ap1Beacon ap1Beacon){
        List<Ap1Beacon> beaconQueried = databaseHelper.queryBeacons(ap1Beacon);
        if(beaconQueried != null){
            // if beaconQueried is in local db, add it to detected&registered list
            for(Ap1Beacon queryResult : beaconQueried){
                Log.e(TAG, "actionOnEnterAp1Beacon: " + queryResult.getMajor() + "-" + queryResult.getMinor() + ": " + queryResult.getIdparent() + " is in localDB");
                String nickname = queryResult.getNickname();
                if(nickname == null)
                    queryResult.setNickname("undefined"); // it means the beacon is in Ap1 DB but the name was not given
                else
                    queryResult.setNickname(nickname);
                queryResult.setUrlfar(queryResult.getUrlfar());
                queryResult.setUrlnear(queryResult.getUrlnear());

                if(recyclerViewFromOutside == null){
                    Log.e(TAG, "actionOnEnterAp1Beacon: recyclerveiw is null");
                }else{
                    addToDetectedList(recyclerViewFromOutside, DataStore.detectedBeaconList.size(), queryResult);
                    addToDetectedAndRegisteredList(recyclerViewFromOutside, DataStore.detectedAndAddedBeaconList.size(), queryResult);
                }
            }
        }else{
            if(recyclerViewFromOutside == null){
                Log.e(TAG, "actionOnEnterAp1Beacon, non-local: recyclerview is null");
            }else
                addToDetectedList(recyclerViewFromOutside, DataStore.detectedBeaconList.size(), ap1Beacon);
        }

    }

    @Override
    protected void actionOnRssiChanged(int index, String newRssi){
        Ap1Beacon tmpBeacon = DataStore.detectedBeaconList.get(index);
        tmpBeacon.setRssi(newRssi);   // override the beacon's rssi in detectedBeaconList
        if(!needToResort)
            needToResort = true;

        int registeredListSize = DataStore.detectedAndAddedBeaconList.size();
        for(int j = 0; j < registeredListSize; j++){
            if(BeaconOperation.equals(tmpBeacon, DataStore.detectedAndAddedBeaconList.get(j))){
                DataStore.detectedAndAddedBeaconList.get(j).setRssi(newRssi); // override the beacon's rssi in detected and registeredBeaconList
                if(!needToResetNearbyStatus)
                    needToResetNearbyStatus = true;
            }
        }
    }

    protected abstract void checkRemoteBeaconHash(String urlPath, CallBackSyncData callBackSyncData);
    protected abstract void checkRemoteCompanyHash(String urlPath, CallBackSyncData callBackUpdateCompanySet);

    protected void updateLocalBeaconDB(JSONArray newBeaconSet, final CallBackSyncData callBackSyncData, String remoteBeaconHash){ //save all beacons into local DB one by one
        Log.e(TAG, "updateing local beacons table...");
        Gson gson = new Gson(); //use Gson to parse a Beacon JSONObject to a POJO
        int newBeaconSetLength = newBeaconSet.length();
        for(int i = 0; i < newBeaconSetLength; i++){
            try{
                Ap1Beacon beaconFromNewRemoteSet = gson.fromJson(newBeaconSet.getJSONObject(i).toString(), Ap1Beacon.class);
                databaseHelper.saveBeacon(beaconFromNewRemoteSet); //add all the beacons to the new DB
            }catch (JSONException e) {
                Log.e("Beacons traversal error", e.toString());
            }
        }
        spHashValue.edit().putString("hashBeacon", remoteBeaconHash).apply();
        // DataStore.registeredAndGroupedBeaconList = groupBeaconsByCompany();
        DataStore.beaconAllList = groupBeaconsByCompany((ArrayList<Ap1Beacon>) databaseHelper.queryForAllBeacons());
        Log.e(TAG, "update beacon info success");
        callBackSyncData.onSuccess();
    }

    protected void updateLocalCompanyDB(JSONArray newCompanySet, final CallBackSyncData callBackUpdateCompanySet, String remoteCompanyHash){
        Log.e(TAG, "update local companies table...");
        Gson gson = new Gson();
        int newCompanySetLength = newCompanySet.length();
        for(int i = 0; i < newCompanySetLength; i++){
            try{
                Company companyFromRemoteSet = gson.fromJson(newCompanySet.getJSONObject(i).toString(), Company.class);
                databaseHelper.saveCompany(companyFromRemoteSet);
            }catch (JSONException e){
                Log.e(TAG, "update company err " + e.toString());
                callBackUpdateCompanySet.onFailure(e.toString());
            }
        }
        spHashValue.edit().putString("hashCompany", remoteCompanyHash).apply();
        Log.e(TAG, "update company info success");
        callBackUpdateCompanySet.onSuccess();
    }

    protected void addToDetectedList(final RecyclerView recyclerView, final int position, final Ap1Beacon newBeacon) {
        DataStore.detectedBeaconList.add(newBeacon);
        Log.e("new detected beacon", "added");

        Log.e("adapter type", currentAdapter);
        if(currentAdapter.equals(adapterTypeAdmin)){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(!recyclerView.isComputingLayout()){
                        t.notifyItemInserted(position);
                        if (position != DataStore.detectedBeaconList.size() - 1) {
                            Log.e("notifyItemRangeChanged", "position extends");
                            t.notifyItemRangeChanged(position, DataStore.detectedBeaconList.size() - position);
                        }
                    }
                }
            });

        }
    }

    protected void addToDetectedAndRegisteredList(final RecyclerView recyclerView, final int position, final Ap1Beacon newBeacon){
        DataStore.detectedAndAddedBeaconList.add(newBeacon);
        Log.e("new registered beacon", "added");

        if(currentAdapter.equals(adapterTypeUser)){
            handler.post(new Runnable(){
                public void run(){
                    if(!recyclerView.isComputingLayout()){
                        t.notifyItemInserted(position);
                        if(position != DataStore.detectedAndAddedBeaconList.size() - 1){
                            t.notifyItemRangeChanged(position, DataStore.detectedAndAddedBeaconList.size() - position);
                        }
                    }
                }
            });
        }

    }

    protected final void deleteFromDetectedList(int pos){
        DataStore.detectedBeaconList.remove(pos);
    }

    // this method is called when updating/reseting local beacon db
    protected void clearDetectedBeaconList(){
        for(int i = DataStore.detectedBeaconList.size() - 1; i >= 0; i--){
            deleteFromDetectedList(i);
        }
    }

    // use registeredBeacon as input and return beacons grouped by companyid
    protected ArrayList<Ap1Beacon> groupBeaconsByCompany(ArrayList<Ap1Beacon> ungroupedBeacons){
        // ArrayList<Beacon> ungroupedRegisterdBeacons = getBeaconsWithAppIdParent(String.valueOf(idparent));
        Map<String, ArrayList<Ap1Beacon>> groupedAp1Beacons = new HashMap<String, ArrayList<Ap1Beacon>>();
        for (Ap1Beacon beacon: ungroupedBeacons) {
            String key = beacon.getIdcompany();
            if (groupedAp1Beacons.get(key) == null) {
                groupedAp1Beacons.put(key, new ArrayList<Ap1Beacon>());
                Ap1Beacon groupDivider = new Ap1Beacon(); // create a fake beacon as a group divider for different companies
                groupDivider.setIdcompany(key);
                groupDivider.setNickname("groupDivider");
                groupedAp1Beacons.get(key).add(groupDivider);
            }
            groupedAp1Beacons.get(key).add(beacon);
        }

        ArrayList<Ap1Beacon> groupedBeaconsList = new ArrayList<>();
        for(ArrayList<Ap1Beacon> beaconGroup : groupedAp1Beacons.values())
                groupedBeaconsList.addAll(beaconGroup);

        return groupedBeaconsList;
    }

    public void setAdapter(@NonNull T t){
        this.t = t;
        currentAdapter = t.getClass().getSimpleName();
    }

    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerViewFromOutside = recyclerView;
        //isRecyclerViewFromOutsideSet = true;
        Log.e(TAG, "setRecyclerView: true");
    }

    public T getAdapter(){
        return t;
    }

    @Override
    public void startScanning(){
        super.startScanning();

        //DataStore.detectedBeaconList.clear();
        //DataStore.detectedAndAddedBeaconList.clear();

        timerResortList = new Timer(); // cancel it in onDestroy()
        timerResortList.schedule(new TimerTask() {
            @Override
            public void run() {
                if(needToResort){
                    Log.e("resorting list", "...");
                    Collections.sort(DataStore.detectedBeaconList); // resort beacon sequence by rssi
                    //displayDetectedBeaconsList("afterResort");
                    needToResort = false;
                    //if(currentAdapter.equals(adapterTypeAdmin))
                    //ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
                }
                if(needToResetNearbyStatus){
                    //if(currentAdapter.equals(adapterTypeUser))
                    //    ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
                    needToResetNearbyStatus = false;
                }
                //ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());  swap adapters may cause the X issue
            }}, 15000, 15000);
    }

    @Override
    public void stopScanning(){
        super.stopScanning();

        if(timerResortList != null){
            timerResortList.cancel();
            timerResortList.purge();
        }
        Log.e(TAG, "stopScanning: timer canceled");
    }

    public void onDestroy(){
        super.onDestroy();

        if(timerResortList != null){
            timerResortList.cancel();
            timerResortList.purge();
        }
    }

    /*
    public void getRemoteBeaconHash(String apiPath, CallBackSyncData callBackSyncData){
        checkRemoteBeaconHash(apiPath, callBackSyncData);
    }

    public void getRemoteCompanyHash(String apiPath, CallBackSyncData callBackUpdateCompanySet){
        checkRemoteCompanyHash(apiPath, callBackUpdateCompanySet);
    }

    public void setListAdapter(T t){ //must set an adapter, it cannot be null
        setAdapter(t);
    }

    public ArrayList<Ap1Beacon> getBeaconInAllPlaces(){
        return DataStore.beaconAllList;
    }

    public ArrayList<Ap1Beacon> getBeaconDetected(){
        return DataStore.detectedBeaconList;
    }

    public ArrayList<Ap1Beacon> getBeaconDetectedAndRegistered(){
        return DataStore.detectedAndAddedBeaconList;
    }

    // return beacons that (have same idparent as registered in this app)
    public ArrayList<Ap1Beacon> getMyBeacons(){
        return DataStore.registeredAndGroupedBeaconList;
    }

    public T getListAdapter(){
        return getAdapter();
    }

    public void setUrlBase(String url){
        DataStore.urlBase = url;
    }

    public String getUrlBase(){
        return DataStore.urlBase;
    }
    */
}
