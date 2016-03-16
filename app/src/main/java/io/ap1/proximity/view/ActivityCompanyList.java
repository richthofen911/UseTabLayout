package io.ap1.proximity.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Utils.DatabaseHelper;
import io.ap1.proximity.R;
import io.ap1.proximity.adapter.AdapterCompanyList;

public class ActivityCompanyList extends AppCompatActivity {

    private RecyclerView recyclerViewCompanyList;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private AdapterCompanyList adapterCompanyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        DatabaseHelper databaseHelper = DatabaseHelper.getHelper(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar_company_list);
        adapterCompanyList = new AdapterCompanyList((ArrayList)databaseHelper.queryForAllCompanies());
        recyclerViewCompanyList = (RecyclerView) findViewById(R.id.recyclerView_company_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerViewCompanyList.setLayoutManager(linearLayoutManager);
        recyclerViewCompanyList.setHasFixedSize(true);
        recyclerViewCompanyList.setAdapter(adapterCompanyList);

    }
}
