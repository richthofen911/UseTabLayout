package io.ap1.proximity.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.libap1beaconmngt.DatabaseHelper;
import io.ap1.libap1util.ApiCaller;
import io.ap1.libap1util.CallbackDefaultVolley;
import io.ap1.proximity.Constants;
import io.ap1.proximity.MyProgressDialog;
import io.ap1.proximity.R;

public class ActivityBeaconDetail extends AppCompatActivity {
    public final static String TAG = "ActivityBeaconDetail";

    @Bind(R.id.tv_toolbar_beacon_action)
    TextView tvToolbarBeaconAction;
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

    private String addOrDel;
    private String beaconId;
    private String idcompany;
    private String nickname;

    DatabaseHelper databaseHelper;

    Handler handler;

    int viewholderPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detail);
        ButterKnife.bind(this);

        databaseHelper = DatabaseHelper.getHelper(this);

        Intent intent = getIntent();
        String detectedUuid = intent.getStringExtra("uuid").toUpperCase();
        etBeaconDetailUuid.setFocusable(false);
        etBeaconDetailUuid.setFocusableInTouchMode(false);
        if(detectedUuid != null)
            etBeaconDetailUuid.setText(detectedUuid);
        String detectedMajor = intent.getStringExtra("major");
        etBeaconDetailMajor.setFocusable(false);
        etBeaconDetailMajor.setFocusableInTouchMode(false);
        if(detectedMajor != null)
            etBeaconDetailMajor.setText(detectedMajor);
        String detectedMinor = intent.getStringExtra("minor");
        etBeaconDetailMinor.setFocusable(false);
        etBeaconDetailMinor.setFocusableInTouchMode(false);
        if(detectedMinor != null)
            etBeaconDetailMinor.setText(detectedMinor);
        String detectedRssi = intent.getStringExtra("rssi");
        if(detectedRssi != null)
            etBeaconDetailRssi.setText(detectedRssi);
        else
            etBeaconDetailRssi.setText("-80");
        beaconId = intent.getStringExtra("id");
        nickname = intent.getStringExtra("nickname");
        if(nickname != null)
            if(!nickname.equals("unknown"))
                etBeaconDetailNickName.setText(nickname);
        idcompany = intent.getStringExtra("idcompany");
        if(idcompany != null)
            if(!idcompany.equals("unknown"))
                tvBeaconDetailCompanySelect.setText(databaseHelper.queryForOneCompany(idcompany).getCompany());

        addOrDel = intent.getStringExtra("addOrDel");
        if(addOrDel.equals("add"))
            tvToolbarBeaconAction.setText("ADD");
        else if(addOrDel.equals("del"))
            tvToolbarBeaconAction.setText("REMOVE");
        else
            tvToolbarBeaconAction.setVisibility(View.GONE);

        String urlNear = intent.getStringExtra("urlnear");
        if(urlNear != null)
            etBeaconDetailUrlNear.setText(urlNear);
        else
            etBeaconDetailUrlNear.setText(R.string.default_beacon_url);

        String urlFar = intent.getStringExtra("urlfar");
        if(urlFar != null)
            etBeaconDetailUrlFar.setText(urlFar);
        else
            etBeaconDetailUrlFar.setText(R.string.default_beacon_url);

        String notifyTitle = intent.getStringExtra("notifytitle");
        if(notifyTitle != null)
            etBeaconDetailTitle.setText(notifyTitle);

        String notifyText = intent.getStringExtra("notifytext");
        if(notifyText != null)
            etBeaconDetailMessage.setText(notifyText);

        String lat = intent.getStringExtra("lat");
        if(lat != null)
            etBeaconDetailLat.setText(lat);

        String lng = intent.getStringExtra("lng");
        if(lng != null)
            etBeaconDetailLng.setText(lng);

        viewholderPosition = intent.getIntExtra("position", 0);

        tvBeaconDetailCompanyArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: CompanyArrow");
                Intent intent = new Intent(ActivityBeaconDetail.this, ActivityCompanyList.class);
                startActivityForResult(intent, INTENT_CODE_SELECT_COMPANY);
            }
        });
    }

    public void onActionClicked(final View v){
        Log.e(TAG, "onActionClicked: " + ((TextView) v).getText().toString());

        // --- required params
        final String uuid = etBeaconDetailUuid.getText().toString();
        final String major = etBeaconDetailMajor.getText().toString();
        final String minor = etBeaconDetailMinor.getText().toString();
        final String rssi = etBeaconDetailRssi.getText().toString();
        if(((TextView) v).getText().toString().equals("ADD")){

            // --- optional params
            String nickname = etBeaconDetailNickName.getText().toString();
            final String macaddress = etBeaconDetailMacAddress.getText().toString();
            String lat = etBeaconDetailLat.getText().toString();
            if(lat.equals(""))
                lat = getResources().getString(R.string.default_lat);
            String lng = etBeaconDetailLng.getText().toString();
            if(lng.equals(""))
                lng = getResources().getString(R.string.default_lng);
            String urlnear = etBeaconDetailUrlNear.getText().toString();
            String urlfar = etBeaconDetailUrlFar.getText().toString();
            String notifyTitle = etBeaconDetailTitle.getText().toString();
            String notifyText = etBeaconDetailMessage.getText().toString();

            Log.e(TAG, "onActionClicked: " + uuid + "|" + major + "|" + minor);

            if(uuid.equals("")||major.equals("")||minor.equals("")||rssi.equals("")||tvBeaconDetailCompanySelect.getText().toString().equals(""))
                Toast.makeText(this, "UUID/Major/Minor/Rssi/Company cannot be empty", Toast.LENGTH_SHORT).show();
            else {
                Map<String, String> postParams = new HashMap<>();
                postParams.put("uuid", uuid.toUpperCase());
                postParams.put("major", major);
                postParams.put("minor", minor);
                postParams.put("rssi", rssi);
                postParams.put("hash", companyHash); // instead of sending idcompany, we use company's hash
                postParams.put("user", ActivityMain.loginUsername);
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
                if(!notifyTitle.equals(""))
                    postParams.put("notifytitle", notifyTitle);
                if(!notifyText.equals(""))
                    postParams.put("notifytext", notifyText);

                MyProgressDialog.show(this, "Processing...");
                ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, Constants.API_PATH_ADD_BEACON, null, postParams, Request.Method.POST)
                        .exec(new CallbackDefaultVolley(){
                            @Override
                            public void onDelivered(String result){
                                Log.e(TAG, "onDelivered: " + result);
                                MyProgressDialog.dismissDialog();
                                try{
                                    JSONObject jsonObject = new JSONObject(result);
                                    if(jsonObject.getString("success").equals("1")){
                                        Snackbar.make(v, "New Beacon Added", Snackbar.LENGTH_SHORT).show();
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("action", "add");
                                        resultIntent.putExtra("uuid", uuid);
                                        resultIntent.putExtra("major", major);
                                        resultIntent.putExtra("minor", minor);
                                        resultIntent.putExtra("position", viewholderPosition);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    }
                                    else if(jsonObject.getString("success").equals("2"))
                                        toastFromWorkThread("A user cannot add more than 3 beacon on this demo version");
                                    else
                                        toastFromWorkThread("Fail to add the beacon");
                                }catch (JSONException e){
                                    toastFromWorkThread("Add Beacon JSONException: " + e.toString());
                                    Log.e(TAG, "AddBeacon request onDelivered: " + e.toString());
                                }
                            }
                            @Override
                            public void onException(final String e){
                                MyProgressDialog.dismissDialog();
                                toastFromWorkThread(e);
                            }
                        });
            }
        }else if(((TextView) v).getText().toString().equals("REMOVE")){
            Map<String, String> postParams = new HashMap<>();
            Log.e(TAG, "onActionClicked: beaconid, " + beaconId);
            postParams.put("id", beaconId);
            postParams.put("idbundle", ActivityMain.PACKAGE_NAME);
            postParams.put("user", ActivityMain.loginUsername);

            MyProgressDialog.show(this, "Processing...");
            ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, Constants.API_PATH_DELETE_BEACON, null, postParams, Request.Method.POST)
                    .exec(new CallbackDefaultVolley(){
                        @Override
                        public void onDelivered(final String result){
                            Log.e(TAG, "onDelivered: " + result);

                            MyProgressDialog.dismissDialog();
                            try{
                                JSONObject jsonObject = new JSONObject(result);
                                if(jsonObject.getString("success").equals("1")){
                                    Snackbar.make(v, "Beacon Deleted", Snackbar.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("action", "del");
                                    resultIntent.putExtra("uuid", uuid);
                                    resultIntent.putExtra("major", major);
                                    resultIntent.putExtra("minor", minor);
                                    resultIntent.putExtra("rssi", rssi);
                                    resultIntent.putExtra("position", viewholderPosition);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }
                                else
                                    toastFromWorkThread("Fail to remove, the beacon was not added by you.");
                            }catch (JSONException e){
                                Log.e(TAG, "delete beacon request onDelivered: " + e.toString());
                            }
                        }
                        @Override
                        public void onException(final String e){
                            MyProgressDialog.dismissDialog();
                            Log.e(TAG, "onException: " + e);
                            toastFromWorkThread(e);
                        }
                    });
        }
    }

    private void toastFromWorkThread(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ActivityBeaconDetail.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
