package io.ap1.proximity.view;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ap1.libbeaconmanagement.ServiceBeaconManagement;
import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.CallBackSyncData;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;

public class ActivityCompanyDetails extends AppCompatActivity {
    private static final String TAG = "ActivityCompanyDetails";

    @Bind(R.id.tv_company_details_title_name)
    TextView tvCompanyDetailsTitleName;
    @Bind(R.id.et_company_details_name)
    EditText etCompanyDetailsName;
    @Bind(R.id.tv_company_details_title_color)
    TextView tvCompanyDetailsTitleColor;
    @Bind(R.id.tv_company_details_color)
    TextView tvCompanyDetailsColor;
    @Bind(R.id.tv_company_details_color_change)
    TextView tvCompanyDetailsColorChange;
    @Bind(R.id.tv_company_details_title_latitude)
    TextView tvCompanyDetailsTitleLatitude;
    @Bind(R.id.et_company_details_latitude)
    EditText etCompanyDetailsLatitude;
    @Bind(R.id.tv_company_details_title_longitude)
    TextView tvCompanyDetailsTitleLongitude;
    @Bind(R.id.et_company_details_longitude)
    EditText etCompanyDetailsLongitude;

    private static final int COMPANY_CHANGE_COLOR = 4;

    ServiceBeaconManagement.BinderManagement binderBeaconManagement;
    ServiceConnection connBeaconManagement;
    String apiActionPath;
    String addOrEdit;
    String companyHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        addOrEdit = intent.getStringExtra("addOrEdit");
        if(addOrEdit.equals("add"))
            apiActionPath = Constants.API_PATH_ADD_COMPANY;
        else
            apiActionPath = Constants.API_PATH_EDIT_COMPANY;

        companyHash = intent.getStringExtra("hash");
        etCompanyDetailsName.setText(intent.getStringExtra("company"));
        String colorToDisplay = "04A9CE";
        String color = intent.getStringExtra("color");
        if(color != null)
            colorToDisplay = color;
        tvCompanyDetailsColor.setText(colorToDisplay);
        String colorToParse = "#" + colorToDisplay;
        int colorParsed = Color.parseColor(colorToParse);
        tvCompanyDetailsColorChange.setBackgroundColor(colorParsed);
        tvCompanyDetailsColorChange.setText(colorToParse);
        tvCompanyDetailsColorChange.setTextColor(colorParsed);

        String lat = intent.getStringExtra("lat");
        if(lat != null)
            etCompanyDetailsLatitude.setText(lat);
        String lng = intent.getStringExtra("lng");
        if(lng != null)
            etCompanyDetailsLongitude.setText(lng);

        tvCompanyDetailsColorChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeColor = new Intent(ActivityCompanyDetails.this, ActivityColorPicker.class);
                changeColor.putExtra("caller", "companyColor");
                changeColor.putExtra("defaultColor", tvCompanyDetailsColorChange.getText().toString());
                startActivityForResult(changeColor, COMPANY_CHANGE_COLOR);
            }
        });
    }

    public void onSaveClicked(final View v){
        Log.e(TAG, "save clicked");
        String companyName = etCompanyDetailsName.getText().toString();
        String companyColor = tvCompanyDetailsColor.getText().toString();
        String companyLat = etCompanyDetailsLatitude.getText().toString();
        String companyLng = etCompanyDetailsLongitude.getText().toString();

        Map<String, String> postParams = new HashMap<>();
        postParams.put("company", companyName);
        postParams.put("color", companyColor);
        postParams.put("lat", companyLat);
        postParams.put("long", companyLng);
        if(companyHash != null)
            postParams.put("hash", companyHash);
        postParams.put("user", ActivityMain.loginUsername);
        postParams.put("idbundle", ActivityMain.PACKAGE_NAME);

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, apiActionPath, null, postParams, Request.Method.POST)
                .exec(new DefaultVolleyCallback(){
                    @Override
                    public void onDelivered(String result){
                        Log.e(TAG, "onDelivered: " + result);
                        boolean success = false;
                        try{
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getString("success").equals("1"))
                                success = true;
                        }catch (JSONException e){
                            Log.e(TAG, "onDelivered: response is not in json format");
                            if(result.equals("1"))
                                success = true;
                        }
                        if(success){
                            connBeaconManagement = new ServiceConnection() {
                                @Override
                                public void onServiceConnected(ComponentName name, IBinder service) {
                                    Log.e(TAG, "Service UpdateCompany: Connected");
                                    binderBeaconManagement = (ServiceBeaconManagement.BinderManagement) service;

                                    binderBeaconManagement.getRemoteCompanyHash("/getAllCompanies_a.php", new CallBackSyncData(ActivityCompanyDetails.this, "Updating Company Data") {
                                        @Override
                                        public void onSuccess() {
                                            super.onSuccess();
                                            Toast.makeText(ActivityCompanyDetails.this, "Success", Toast.LENGTH_SHORT).show();
                                            Intent resultIntent = new Intent();
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(String cause) {
                                            super.onFailure(cause);
                                            Toast.makeText(ActivityCompanyDetails.this, "Fail, " + cause, Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Fail to update Company Data" + cause);
                                        }
                                    });
                                }

                                @Override
                                public void onServiceDisconnected(ComponentName name) {
                                    Log.e("Service BeaconMngt", "Disconnected");
                                }
                            };

                            bindService(new Intent(ActivityCompanyDetails.this, ServiceBeaconManagement.class), connBeaconManagement, BIND_AUTO_CREATE);

                        }else if(result.equals("0")){
                            Snackbar.make(v, "Failed", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onException(final String e){
                        Toast.makeText(ActivityCompanyDetails.this, e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case COMPANY_CHANGE_COLOR:
                if(resultCode == RESULT_OK){
                    int newColor = data.getIntExtra("newColor", 0);
                    String hexColor = String.format("#%06X", (0xFFFFFF & newColor));
                    Log.e(TAG, "color: " + hexColor);
                    tvCompanyDetailsColorChange.setBackgroundColor(newColor);
                    tvCompanyDetailsColorChange.setText(hexColor);
                    tvCompanyDetailsColorChange.setTextColor(newColor);
                    tvCompanyDetailsColor.setText(hexColor.substring(1));
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
