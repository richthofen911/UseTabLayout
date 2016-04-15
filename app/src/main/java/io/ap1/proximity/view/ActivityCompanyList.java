package io.ap1.proximity.view;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.ap1.libbeaconmanagement.Company;
import io.ap1.libbeaconmanagement.ServiceBeaconManagement;
import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.CallBackSyncData;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.DatabaseHelper;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.adapter.AdapterCompanyList;

public class ActivityCompanyList extends AppCompatActivity {
    private final static String TAG = "ActivityCompanyList";

    private RecyclerView recyclerViewCompanyList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private AdapterCompanyList adapterCompanyList;
    ArrayList<Company> companyListData;
    DatabaseHelper databaseHelper;

    ServiceBeaconManagement.BinderManagement binderBeaconManagement;
    ServiceConnection connBeaconManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        Log.e(TAG, "onCreate: ");

        databaseHelper = DatabaseHelper.getHelper(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar_company_list);
        companyListData = (ArrayList)databaseHelper.queryForAllCompanies();
        adapterCompanyList = new AdapterCompanyList(companyListData);
        recyclerViewCompanyList = (RecyclerView) findViewById(R.id.recyclerView_company_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerViewCompanyList.setLayoutManager(linearLayoutManager);
        recyclerViewCompanyList.setHasFixedSize(true);
        recyclerViewCompanyList.setAdapter(adapterCompanyList);
    }

    public void deleteCompany(String hash){
        Map<String, String> postParams = new HashMap<>();
        postParams.put("hash", hash);
        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, "/deleteCompany.php", null, postParams, Request.Method.POST)
                .exec(new ApiCaller.VolleyCallback(){
                    @Override
                    public void onDelivered(String result){
                        Log.e(TAG, "onDelivered: " + result);
                        if(result.equals("1")){
                            connBeaconManagement = new ServiceConnection() {
                                @Override
                                public void onServiceConnected(ComponentName name, IBinder service) {
                                    Log.e(TAG, "Service UpdateCompany: Connected");
                                    binderBeaconManagement = (ServiceBeaconManagement.BinderManagement) service;

                                    final ProgressDialog progCheckCompany = ProgressDialog.show(ActivityCompanyList.this, "Updating Company Data", "Please wait", true);
                                    binderBeaconManagement.getRemoteCompanyHash("/getAllCompanies_a.php", new CallBackSyncData() {
                                        @Override
                                        public void onSuccess() {
                                            progCheckCompany.dismiss();
                                            Toast.makeText(ActivityCompanyList.this, "The company has been deleted", Toast.LENGTH_SHORT).show();
                                            companyListData = (ArrayList)databaseHelper.queryForAllCompanies();
                                            adapterCompanyList.setDataSource(companyListData);
                                            recyclerViewCompanyList.setAdapter(null);
                                            recyclerViewCompanyList.setAdapter(adapterCompanyList);
                                        }

                                        @Override
                                        public void onFailure(String cause) {
                                            progCheckCompany.dismiss();
                                            Toast.makeText(ActivityCompanyList.this, "Fail, " + cause, Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Fail to update Company Data" + cause);
                                        }
                                    });
                                    progCheckCompany.setCancelable(true);
                                }

                                @Override
                                public void onServiceDisconnected(ComponentName name) {
                                    Log.e("Service BeaconMngt", "Disconnected");
                                }
                            };

                            bindService(new Intent(ActivityCompanyList.this, ServiceBeaconManagement.class), connBeaconManagement, BIND_AUTO_CREATE);

                        }else if(result.equals("0")){
                            Toast.makeText(ActivityCompanyList.this, "Company doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onException(final String e){
                        Toast.makeText(ActivityCompanyList.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onAddClicked(View v){
        Log.e(TAG, "onAddClicked: clicked");
        Intent intent = new Intent(this, ActivityCompanyDetails.class);
        intent.putExtra("addOrEdit", "add");
        startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_ADD_COMPANY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: ");
        switch (requestCode){
            case Constants.INTENT_REQUEST_CODE_ADD_COMPANY:
                if(resultCode == RESULT_OK){
                    // refresh company list
                    companyListData = (ArrayList)databaseHelper.queryForAllCompanies();
                    adapterCompanyList.setDataSource(companyListData);
                    recyclerViewCompanyList.setAdapter(null);
                    recyclerViewCompanyList.setAdapter(adapterCompanyList);
                }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(binderBeaconManagement != null && binderBeaconManagement.isBinderAlive())
            unbindService(connBeaconManagement);
    }
}
