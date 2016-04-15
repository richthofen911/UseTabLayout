package io.ap1.proximity.view;

import android.content.Context;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ap1.libbeaconmanagement.Utils.ApiCaller;
import io.ap1.libbeaconmanagement.Utils.DataStore;
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
            postParams.put("nickname", nickname);
            postParams.put("macaddress", macaddress);
            postParams.put("lat", lat);
            postParams.put("lng", lng);
            postParams.put("urlnear", urlnear);
            postParams.put("urlfar", urlfar);

            ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, "/addBeaconv5.php", null, postParams, Request.Method.POST)
                    .exec(new ApiCaller.VolleyCallback(){
                        @Override
                        public void onDelivered(String result){
                            Log.e(TAG, "onDelivered: " + result);
                            if(result.equals("1")){
                                Snackbar.make(v, "New Company Added", Snackbar.LENGTH_SHORT).show();
                            }else if(result.equals("0")){
                                Snackbar.make(v, "Company Existed Already", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onException(final String e){
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

                    tvBeaconDetailCompanySelect.setText(companyName);
                }
        }
    }
}
