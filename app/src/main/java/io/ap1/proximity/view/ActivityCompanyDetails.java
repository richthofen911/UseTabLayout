package io.ap1.proximity.view;

import android.content.Intent;
import android.graphics.Color;
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

public class ActivityCompanyDetails extends AppCompatActivity {

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
    private static final String TAG = "ActivityCompanyDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
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

        etCompanyDetailsLatitude.setText(intent.getStringExtra("lat"));
        etCompanyDetailsLongitude.setText(intent.getStringExtra("lng"));

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

        ApiCaller.getInstance(getApplicationContext()).setAPI(DataStore.urlBase, "/addCompany.php", null, postParams, Request.Method.POST)
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
                        Toast.makeText(ActivityCompanyDetails.this, e, Toast.LENGTH_SHORT).show();
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
                    tvCompanyDetailsColor.setText(hexColor.substring(1));
                }
        }
    }
}
