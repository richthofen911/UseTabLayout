package io.ap1.proximity.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ap1.proximity.R;

public class ActivityCompanyDetails extends AppCompatActivity {

    @Bind(R.id.tv_company_details_title_name)
    TextView tvCompanyDetailsTitleName;
    @Bind(R.id.tv_company_details_name)
    TextView tvCompanyDetailsName;
    @Bind(R.id.tv_company_details_title_color)
    TextView tvCompanyDetailsTitleColor;
    @Bind(R.id.tv_company_details_color)
    TextView tvCompanyDetailsColor;
    @Bind(R.id.tv_company_details_color_change)
    TextView tvCompanyDetailsColorChange;
    @Bind(R.id.tv_company_details_title_latitude)
    TextView tvCompanyDetailsTitleLatitude;
    @Bind(R.id.tv_company_details_latitude)
    TextView tvCompanyDetailsLatitude;
    @Bind(R.id.tv_company_details_title_longitude)
    TextView tvCompanyDetailsTitleLongitude;
    @Bind(R.id.tv_company_details_longitude)
    TextView tvCompanyDetailsLongitude;

    private static final int COMPANY_CHANGE_COLOR = 4;
    private static final String TAG = "ActivityCompanyDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);
        ButterKnife.bind(this);

        tvCompanyDetailsColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeColor = new Intent(ActivityCompanyDetails.this, ActivityColorPicker.class);
                changeColor.putExtra("caller", "companyColor");
                changeColor.putExtra("defaultColor", tvCompanyDetailsColorChange.getText().toString());
                startActivityForResult(changeColor, COMPANY_CHANGE_COLOR);
            }
        });
    }

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
                }
        }
    }
}
