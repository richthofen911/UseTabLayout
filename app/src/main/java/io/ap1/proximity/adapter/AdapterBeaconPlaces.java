package io.ap1.proximity.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ap1.libap1beaconmngt.Ap1Beacon;
import io.ap1.libap1beaconmngt.Company;
import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.libap1beaconmngt.DatabaseHelper;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderBeaconPlaces;

/**
 * Created by admin on 16/02/16.
 */
public class AdapterBeaconPlaces extends RecyclerView.Adapter<ViewHolderBeaconPlaces>{
    private static final String TAG = "AdapterBeaconPlaces";

    private Ap1Beacon beaconTmp = null;
    private DatabaseHelper databaseHelper;

    public AdapterBeaconPlaces(Context context){
        databaseHelper = DatabaseHelper.getHelper(context);
        /*
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            myIdParent = appInfo.metaData.getInt("idparent");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public ViewHolderBeaconPlaces onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_places, viewGroup, false);
        return new ViewHolderBeaconPlaces(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderBeaconPlaces viewHolder, final int position){
        viewHolder.setIsRecyclable(false);
        beaconTmp = DataStore.beaconAllList.get(position);
        if(beaconTmp.getNickname().equals("groupDivider")){
            viewHolder.beaconPlacesCell.setClickable(false);
            viewHolder.tvBeaconPlacesIcon.setText("");
            viewHolder.tvBeaconPlacesIcon.setBackgroundColor(Constants.COLOR_WHITE);
            viewHolder.tvBeaconPlacesAttributes.setText("");
            Company companyTmp = databaseHelper.queryForOneCompany(beaconTmp.getIdcompany());
            String companyName = companyTmp.getCompany();
            viewHolder.tvBeaconPlacesName.setText(companyName);
        }else {
            String title = databaseHelper.queryForOneCompany(beaconTmp.getIdcompany()).getCompany();
            title = title.substring(0, 1);
            viewHolder.tvBeaconPlacesIcon.setText(title);
            String companyColor = databaseHelper.queryForOneCompany(beaconTmp.getIdcompany()).getColor(); // default color, WHITE
            if (companyColor.equals("")) {
                companyColor = "FFFFFF"; // if not defined, use white
            }
            viewHolder.tvBeaconPlacesIcon.setBackgroundColor(Color.parseColor("#" + companyColor));
            viewHolder.tvBeaconPlacesName.setText(beaconTmp.getNickname());
            viewHolder.tvBeaconPlacesAttributes.setText(Constants.MAJOR + beaconTmp.getMajor() + Constants.MINOR + beaconTmp.getMinor());
            viewHolder.tvArrowPlaces.setText(">");
            viewHolder.url = (DataStore.beaconAllList.get(position).getUrlfar());
        }

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.beaconAllList.size();
    }
}
