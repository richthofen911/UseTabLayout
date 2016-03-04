package io.ap1.libbeaconmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.perples.recosdk.RECOBeaconRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.ServiceBeaconDetection;
import io.ap1.libbeaconmanagement.Utils.ICallBackUpdateBeaconSet;

public class ServiceBeaconManagement<T extends RecyclerView.Adapter> extends ServiceBeaconDetection {
    protected SharedPreferences spHashValue;
    protected int idparent;

    protected Handler handler;

    protected boolean needToResort = false;
    protected boolean needToResetNearbyStatus = false;

    protected Timer timerResortList;
    protected TimerTask timerTaskResortList;

    private T t;
    protected String currentAdapter = "";
    protected static final String adapterTypeAdmin = "AdapterBeaconNearbyAdmin";
    protected static final String adapterTypeUser = "AdapterBeaconNearbyUser";


    public ServiceBeaconManagement() {
    }

    public void onCreate() {
        super.onCreate();

        handler = new Handler(getMainLooper());
        timerResortList = new Timer(); // cancel it in onDestroy()
        spHashValue = getApplication().getSharedPreferences("HashValue.sp", 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);

        return new BinderManagement(definedRegions);
    }

    @Override
    protected void actionOnEnterAp1Beacon(Beacon ap1Beacon){
        Log.e("onEnter", ap1Beacon.getMajor() + "-" + ap1Beacon.getMinor());

        Beacon beaconQueried = databaseHelper.queryForOneBeacon(ap1Beacon);
        if(beaconQueried != null){
            // if beaconQueried is in local db, add it to detected&registerd list
            //Log.e("detected&registered", "add to new&registered list");
            if(beaconQueried.getIdparent().equals(idparent)){
                String nickname = beaconQueried.getNickname();
                if(nickname != null && !nickname.equals(""))
                    ap1Beacon.setNickname(nickname);
                else
                    ap1Beacon.setNickname("Inactive");
                ap1Beacon.setUrlfar(beaconQueried.getUrlfar());
                ap1Beacon.setUrlnear(beaconQueried.getUrlnear());
                addToDetectedAndRegisteredList(DataStore.detectedAndRegisteredBeaconList.size(), ap1Beacon);
            }
        }else
            ap1Beacon.setNickname("Inactive");
        addToDetectedList(DataStore.detectedBeaconList.size(), ap1Beacon);

        displayDetectedAndRegisteredBeaconsList("NewBeacon");
    }


    @Override
    protected void actionOnRssiChanged(int index, String newRssi){
        Beacon tmpBeacon = DataStore.detectedBeaconList.get(index);
        tmpBeacon.setRssi(newRssi);   // override the beacon's rssi in detectedBeaconList
        if(!needToResort)
            needToResort = true;

        int registeredListSize = DataStore.detectedAndRegisteredBeaconList.size();
        for(int j = 0; j < registeredListSize; j++){
            if(BeaconOperation.equals(tmpBeacon, DataStore.detectedAndRegisteredBeaconList.get(j))){
                DataStore.detectedAndRegisteredBeaconList.get(j).setRssi(newRssi); // override the beacon's rssi in detected&registeredBeaconList
                if(!needToResetNearbyStatus)
                    needToResetNearbyStatus = true;
            }
        }
    }

    protected void checkRemoteServerHash(final String urlPath, final ICallBackUpdateBeaconSet iCallBackUpdateBeaconSet) {
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", spHashValue.getString("hashValue", "empty"));

        ApiCaller.INSTANCE.setAPI(DataStore.urlBase, urlPath, null, postParams, Request.Method.POST).exec(
                new ApiCaller.VolleyCallback() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e("resp check hash", result);
                        if (result.equals("1")) {
                            Log.e("hash", "local == remote");
                            DataStore.beaconInAllPlacesList = sortBeaconsByCompany();
                            iCallBackUpdateBeaconSet.onSuccess();
                        } else {
                            Log.e("hash", "local != remote");
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String remoteHash = jsonObject.getString("hash");
                                databaseHelper.deleteAllBeacons(ServiceBeaconManagement.this); //drop the old beacon database and create a new one
                                clearDetectedBeaconList(); // clear current detected beacon list display
                                DataStore.detectedBeaconList.clear();
                                JSONArray beaconSetRemote = jsonObject.getJSONArray("beacons");
                                updateLocalBeaconDB(beaconSetRemote, iCallBackUpdateBeaconSet, remoteHash);
                            } catch (JSONException e) {
                                iCallBackUpdateBeaconSet.onFailure(e.toString());
                            }
                        }
                    }

                    @Override
                    public void onException(final String e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServiceBeaconManagement.this, e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    protected void updateLocalBeaconDB(JSONArray newBeaconSet, final ICallBackUpdateBeaconSet iCallBackUpdateBeaconSet, String remoteHash){ //save all beacons into local DB one by one
        Log.e("updating", "local beacon db...");
        Gson gson = new Gson(); //use Gson to parse a Beacon JSONObject to a POJO
        for(int i = 0; i < newBeaconSet.length(); i++){
            try{
                Beacon beaconFromNewRemoteSet = gson.fromJson(newBeaconSet.getJSONObject(i).toString(), Beacon.class);
                databaseHelper.saveABeacon(beaconFromNewRemoteSet); //add all the beacons to the new DB

            }catch (JSONException e) {
                Log.e("Beacons traversal error", e.toString());
            }
        }
        spHashValue.edit().putString("hashValue", remoteHash).apply();
        DataStore.beaconInAllPlacesList = sortBeaconsByCompany();
        iCallBackUpdateBeaconSet.onSuccess();
    }


    protected void saveUrlContent(String beaconUrl, String urlContent){ //save url content as local html file
        FileOutputStream outputStream;
        try{
            String fileName = beaconUrl.replace(":", "");
            fileName = fileName.replace("/", "");
            fileName = fileName.replace(".", "");
            File fileUrlContent = new File(getExternalCacheDir().getPath() + "/" + fileName + ".html");
            if(!fileUrlContent.exists()){
                outputStream = new FileOutputStream(new File(getExternalCacheDir().getPath() + "/" + fileName + ".html"));
                outputStream.write(urlContent.getBytes());
                outputStream.close();
                Log.e("write url content", "success");
            }else
                Log.e("write url content", "file exited already");  //may need to modify here for updating file ******
        }catch (Exception e){
            Log.e("write url content", "error");
        }
    }

    protected void addToDetectedList(int position, Beacon newBeacon) {
        DataStore.detectedBeaconList.add(position, newBeacon);
        Log.e("new detected beacon", "added");

        Log.e("adapter type", currentAdapter);
        if(currentAdapter.equals(adapterTypeAdmin)){
            t.notifyItemInserted(position);
            if (position != DataStore.detectedBeaconList.size() - 1) {
                Log.e("notifyItemRangeChanged", "position extends");
                t.notifyItemRangeChanged(position, DataStore.detectedBeaconList.size() - position);
            }
        }
    }

    protected void addToDetectedAndRegisteredList(int position, Beacon newBeacon){
        DataStore.detectedAndRegisteredBeaconList.add(position, newBeacon);
        Log.e("new registered beacon", "added");

        if(currentAdapter.equals(adapterTypeUser))
            t.notifyItemInserted(position);
            if(position != DataStore.detectedAndRegisteredBeaconList.size() - 1){
                t.notifyItemRangeChanged(position, DataStore.detectedAndRegisteredBeaconList.size() - position);
            }
    }

    protected final void deleteFromDetectedList(int pos){
        DataStore.detectedBeaconList.remove(pos);
        /*
        t.notifyItemRemoved(pos);
        if(pos != detectedBeaconList.size() - 1) {
            t.notifyItemRangeChanged(pos, detectedBeaconList.size() - pos);
        }
        */
    }

    // this method is called when updating/reseting local beacon db
    protected void clearDetectedBeaconList(){
        for(int i = DataStore.detectedBeaconList.size() - 1; i >= 0; i--){
            deleteFromDetectedList(i);
        }
    }

    protected ArrayList<Beacon> sortBeaconsByCompany(){
        String[] companyNames = databaseHelper.queryDistinct("companyname");

        if(companyNames != null){
            ArrayList[] sortByCompany = new ArrayList[companyNames.length];
            for(int i = 0; i < sortByCompany.length; i++){
                sortByCompany[i] = (ArrayList) databaseHelper.queryForBeaconsByCompany(companyNames[i]);
            }
            ArrayList<Beacon> finalResult = new ArrayList<>();
            for(ArrayList byOneCompany : sortByCompany){
                Beacon groupDivider = new Beacon(); // create a fake beacon as a group divider for different companies
                groupDivider.setCompanyname(((Beacon) byOneCompany.get(0)).getCompanyname());
                groupDivider.setNickname("groupDivider");
                finalResult.add(groupDivider);
                finalResult.addAll(byOneCompany);
                byOneCompany.clear(); // release the arraylist for memory
            }
            return finalResult;
        }else {
            return null;
        }
    }

    public void setAdapter(@NonNull T t){
        this.t = t;
        currentAdapter = t.getClass().getSimpleName();
        timerTaskResortList = new TimerTask() {
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
                ServiceBeaconManagement.this.t.notifyItemRangeChanged(0, ServiceBeaconManagement.this.t.getItemCount());
            }
        };
    }

    public T getAdapter(){
        return t;
    }


    private void displayDetectedAndRegisteredBeaconsList(String step){
        StringBuilder stringBuilder = new StringBuilder();
        for(Beacon beacon : DataStore.detectedAndRegisteredBeaconList){
            stringBuilder.append(beacon.getMajor()).append("-").append(beacon.getMinor()).append("\n");
        }
        Log.e(step + ":List", stringBuilder.toString());
    }


    public void onDestroy(){
        super.onDestroy();
        timerResortList.cancel();
        timerResortList.purge();
    }

    public class BinderManagement extends ServiceBeaconDetection.BinderDetection {
        public BinderManagement(ArrayList<RECOBeaconRegion> definedRegions){
            super(definedRegions);
        }

        public void getIdparent(){
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                idparent = appInfo.metaData.getInt("idparent");
                Log.e("idparent", "" + idparent);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void startScanning(){
            super.startScanning();
            timerResortList.schedule(timerTaskResortList, 15000, 15000);
        }

        @Override
        public void stopScanning(){
            super.stopScanning();
            timerResortList.cancel();
        }

        public void getServerHash(String apiPath, ICallBackUpdateBeaconSet callBackUpdateBeaconSet){
            checkRemoteServerHash(apiPath, callBackUpdateBeaconSet);
        }

        public void setListAdapter(T t){ //must set an adapter, it cannot be null
            setAdapter(t);
        }

        public ArrayList<Beacon> getBeaconInAllPlaces(){
            return DataStore.beaconInAllPlacesList;
        }

        public ArrayList<Beacon> getBeaconDetected(){
            return DataStore.detectedBeaconList;
        }

        public ArrayList<Beacon> getBeaconDetectedAndRegistered(){
            return DataStore.detectedAndRegisteredBeaconList;
        }

        // return beacons that (have same idparent as registered in this app && in proximity && in local db)
        public ArrayList<Beacon> getMyBeaconsOnMap(){
            ArrayList<Beacon> myBeaconsOnMap = new ArrayList<>();
            for(Beacon beacon : DataStore.detectedAndRegisteredBeaconList){
                if(Integer.parseInt(beacon.getRssi()) > rssiBorder)
                    myBeaconsOnMap.add(beacon);
            }
            return myBeaconsOnMap;
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
    }
}