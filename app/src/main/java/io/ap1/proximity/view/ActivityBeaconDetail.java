package io.ap1.proximity.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.DefaultVolleyCallback;
import io.ap1.proximity.R;

public class ActivityBeaconDetail extends AppCompatActivity {
    public final static String TAG = "ActivityBeaconDetail";

    @Bind(R.id.tv_beacon_detail_nickName)
    TextView tvBeaconDetailNickName;
    @Bind(R.id.et_beacon_detail_nickName)
    EditText etBeaconDetailNickName;
    @Bind(R.id.tv_beacon_detail_url_far)
    TextView tvBeaconDetailUrlFar;
    @Bind(R.id.et_beacon_detail_url_far)
    EditText etBeaconDetailUrlFar;
    @Bind(R.id.tv_beacon_detail_url_near)
    TextView tvBeaconDetailUrlNear;
    @Bind(R.id.et_beacon_detail_url_near)
    EditText etBeaconDetailUrlNear;
    @Bind(R.id.tv_beacon_detail_company)
    TextView tvBeaconDetailCompany;
    @Bind(R.id.tv_beacon_detail_company_select)
    TextView tvBeaconDetailCompanySelect;
    @Bind(R.id.tv_beacon_detail_company_arrow)
    TextView tvBeaconDetailCompanyArrow;
    @Bind(R.id.tv_beacon_detail_title)
    TextView tvBeaconDetailTitle;
    @Bind(R.id.et_beacon_detail_title)
    EditText etBeaconDetailTitle;
    @Bind(R.id.tv_beacon_detail_message)
    TextView tvBeaconDetailMessage;
    @Bind(R.id.et_beacon_detail_message)
    EditText etBeaconDetailMessage;
    @Bind(R.id.tv_beacon_detail_uuid)
    TextView tvBeaconDetailUuid;
    @Bind(R.id.et_beacon_detail_uuid)
    EditText etBeaconDetailUuid;
    @Bind(R.id.tv_beacon_detail_major)
    TextView tvBeaconDetailMajor;
    @Bind(R.id.et_beacon_detail_major)
    EditText etBeaconDetailMajor;
    @Bind(R.id.tv_beacon_detail_minor)
    TextView tvBeaconDetailMinor;
    @Bind(R.id.et_beacon_detail_minor)
    EditText etBeaconDetailMinor;
    @Bind(R.id.tv_beacon_detail_rssi)
    TextView tvBeaconDetailRssi;
    @Bind(R.id.et_beacon_detail_rssi)
    EditText etBeaconDetailRssi;
    @Bind(R.id.tv_beacon_detail_macAddress)
    TextView tvBeaconDetailMacAddress;
    @Bind(R.id.et_beacon_detail_macAddress)
    EditText etBeaconDetailMacAddress;
    @Bind(R.id.tv_beacon_detail_lat)
    TextView tvBeaconDetailLat;
    @Bind(R.id.et_beacon_detail_lat)
    EditText etBeaconDetailLat;
    @Bind(R.id.tv_beacon_detail_lng)
    TextView tvBeaconDetailLng;
    @Bind(R.id.et_beacon_detail_lng)
    EditText etBeaconDetailLng;

    private String companyHash = "";

    public final static int INTENT_CODE_SELECT_COMPANY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String detectedUuid = intent.getStringExtra("uuid");
        if(detectedUuid != null)
            etBeaconDetailUuid.setText(detectedUuid);
        String detectedMajor = intent.getStringExtra("major");
        if(detectedMajor != null)
            etBeaconDetailMajor.setText(detectedMajor);
        String detectedMinor = intent.getStringExtra("minor");
        if(detectedMinor != null)
            etBeaconDetailMinor.setText(detectedMinor);
        String detectedRssi = intent.getStringExtra("rssi");
        if(detectedRssi != null)
            etBeaconDetailRssi.setText(detectedRssi);

        etBeaconDetailUrlNear.setText(R.string.default_beacon_url);
        etBeaconDetailUrlFar.setText(R.string.default_beacon_url);

        tvBeaconDetailCompanyArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: CompanyArrow");
                Intent intent = new Intent(ActivityBeaconDetail.this, ActivityCompanyList.class);
                startActivityForResult(intent, INTENT_CODE_SELECT_COMPANY);
            }
        });
    }

    public void onAddClicked(final View v){
        // --- required params
        String uuid = etBeaconDetailUuid.getText().toString();
        String major = etBeaconDetailMajor.getText().toString();
        String minor = etBeaconDetailMinor.getText().toString();
        String rssi = etBeaconDetailRssi.getText().toString();
        // --- optional params
        String nickname = etBeaconDetailNickName.getText().toString();
        String macaddress = etBeaconDetailMacAddress.getText().toString();
        String lat = etBeaconDetailLat.getText().toString();
        String lng = etBeaconDetailLng.getText().toString();
        String urlnear = etBeaconDetailUrlNear.getText().toString();
        String urlfar = etBeaconDetailUrlFar.getText().toString();

        if(uuid.equals("")||major.equals("")||minor.equals("")||rssi.equals(""))
            Toast.makeText(this, "UUID/Major/Minor/Rssi cannot be empty", Toast.LENGTH_SHORT).show();
        else {
            Map<String, String> postParams = new HashMap<>();
            postParams.put("uuid", urlfar);
            postParams.put("major", major);
            postParams.put("minor", minor);
            postParams.put("rssi", rssi);
            postParams.put("hash", companyHash);
            if(!nickname.equals(""))
                postParams.put("nickname", nickname);
            if(!macaddress.equals(""))
                postParams.put("macaddress", macaddress);
            if(!lat.equals(""))
                postParams.put("lat", lat);
            if(!lng.equals(""))
                postParams.put("long", lng);
            if(!urlnear.equals(""))
                postParams.put("urlnear", urlnear);
            if(!urlfar.equals(""))
                postParams.put("urlfar", urlfar);

            ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, "/addBeaconv5.php", null, postParams, Request.Method.POST)
                    .exec(new DefaultVolleyCallback(this, "Processing"){
                        @Override
                        public void onDelivered(String result){
                            super.onDelivered(result);
                            Log.e(TAG, "onDelivered: " + result);
                            try{
                                JSONObject jsonObject = new JSONObject(result);
                                if(jsonObject.getInt("success") == 1){
                                    Snackbar.make(v, "New Beacon Added", Snackbar.LENGTH_SHORT).show();
                                }
                                else
                                    Snackbar.make(v, "Beacon Existed Already", Snackbar.LENGTH_SHORT).show();
                            }catch (JSONException e){
                                Log.e(TAG, "AddBeacon request onDelivered: " + e.toString());
                            }
                        }
                        @Override
                        public void onException(final String e){
                            super.onException(e);
                            Toast.makeText(ActivityBeaconDetail.this, e, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case INTENT_CODE_SELECT_COMPANY:
                if(resultCode == RESULT_OK){
                    String id = data.getStringExtra("id");
                    String companyName = data.getStringExtra("company");
                    companyHash = data.getStringExtra("companyHash");
                    tvBeaconDetailCompanySelect.setText(companyName);
                }
        }
    }
}
