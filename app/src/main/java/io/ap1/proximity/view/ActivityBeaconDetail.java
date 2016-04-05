package io.ap1.proximity.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
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

        tvBeaconDetailCompanyArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: CompanyArrow");
                Intent intent = new Intent(ActivityBeaconDetail.this, ActivityCompanyList.class);
                startActivityForResult(intent, INTENT_CODE_SELECT_COMPANY);
            }
        });
    }

    public void onAddClicked(View v){

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case INTENT_CODE_SELECT_COMPANY:
                if(resultCode == RESULT_OK){
                    String id = data.getStringExtra("id");
                    String companyName = data.getStringExtra("company");

                    tvBeaconDetailCompany.setText(companyName);
                }
        }
    }

}
