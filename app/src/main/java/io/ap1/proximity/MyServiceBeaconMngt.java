package io.ap1.proximity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.ap1.libap1beaconmngt.Ap1Beacon;
import io.ap1.libap1beaconmngt.CallBackSyncData;
import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.libap1beaconmngt.ServiceBeaconManagement;
import io.ap1.libap1util.ApiCaller;
import io.ap1.libap1util.CallbackDefaultVolley;

public class MyServiceBeaconMngt<T extends RecyclerView.Adapter> extends ServiceBeaconManagement {
    private final String TAG = "MyServiceBeaconMgnt";

    public MyServiceBeaconMngt() {
    }

    public class BinderMyBeaconMngt extends Binder {
        public MyServiceBeaconMngt getService(){
            return MyServiceBeaconMngt.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return new BinderMyBeaconMngt();
    }

    @Override
    public void checkRemoteBeaconHash(String urlPath, final CallBackSyncData callBackSyncData){
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", spHashValue.getString("hashBeacon", "empty"));
        //postParams.put("idbundle", Constants.BUNDLE_ID);
        postParams.put("idbundle", getString(R.string.idbundle));

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, urlPath, null, postParams, Request.Method.POST).exec(
                new CallbackDefaultVolley() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e(TAG, "resp check beacon hash: " + result);
                        if (result.equals("1")) {
                            Log.e(TAG, "beacon hash local == remote");
                            DataStore.beaconAllList = groupBeaconsByCompany((ArrayList<Ap1Beacon>) databaseHelper.queryForAllBeacons());
                            // DataStore.registeredAndGroupedBeaconList = groupBeaconsByCompany();
                            callBackSyncData.onSuccess();
                        } else {
                            Log.e(TAG, "beacon hash local != remote");
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String remoteBeaconHash = jsonObject.getString("hash");
                                databaseHelper.deleteAllBeacons(MyServiceBeaconMngt.this); //drop the old beacon table and create a new one
                                //clearDetectedBeaconList(); // clear current detected beacon list display
                                //DataStore.detectedBeaconList.clear();
                                JSONArray beaconSetRemote = jsonObject.getJSONArray("beacons");
                                updateLocalBeaconDB(beaconSetRemote, callBackSyncData, remoteBeaconHash);
                            } catch (JSONException e) {
                                callBackSyncData.onFailure(e.toString());
                            }
                        }
                    }

                    @Override
                    public void onException(final String e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyServiceBeaconMngt.this, e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    public void checkRemoteCompanyHash(final String urlPath, final CallBackSyncData callBackUpdateCompanySet){
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", spHashValue.getString("hashCompany", "empty"));
        //postParams.put("idbundle", Constants.BUNDLE_ID);
        postParams.put("idbundle", getString(R.string.idbundle));

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, urlPath, null, postParams, Request.Method.POST).exec(
                new CallbackDefaultVolley() {
                    @Override
                    public void onDelivered(String result) {
                        Log.e(TAG, "resp check company hash: " + result);
                        if (result.equals("1")) {
                            Log.e(TAG, "company hash local == remote");
                            DataStore.beaconAllList = (ArrayList<Ap1Beacon>) databaseHelper.queryForAllBeacons();
                            callBackUpdateCompanySet.onSuccess();
                        } else {
                            Log.e(TAG, "company hash local != remote");
                            try {
                                //Log.e(TAG, "onDelivered: company str" + result);
                                JSONObject jsonObject = new JSONObject(result);
                                String remoteCompanyHash = jsonObject.getString("hash");
                                databaseHelper.deleteAllCompanies(); //drop the old company table and create a new one
                                JSONArray companySetRemote = jsonObject.getJSONArray("companies");
                                updateLocalCompanyDB(companySetRemote, callBackUpdateCompanySet, remoteCompanyHash);
                            } catch (JSONException e) {
                                callBackUpdateCompanySet.onFailure(e.toString());
                            }
                        }
                    }

                    @Override
                    public void onException(final String e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyServiceBeaconMngt.this, e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    public void setListAdapter(T t){ //must set an adapter, it cannot be null
        setAdapter(t);
    }

    public ArrayList<Ap1Beacon> getBeaconInAllPlaces(){
        return DataStore.beaconAllList;
    }

    // return beacons that (have same idparent as registered in this app)
    public ArrayList<Ap1Beacon> getMyBeacons(){
        return DataStore.registeredAndGroupedBeaconList;
    }
}
