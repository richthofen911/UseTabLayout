package io.ap1.proximity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ap1.libap1beaconmngt.Ap1Beacon;
import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.libap1beaconmngt.DatabaseHelper;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.view.ActivityMain;
import io.ap1.proximity.viewholder.ViewHolderBeaconNearbyUser;

/**
 * Created by admin on 22/02/16.
 */
public class AdapterBeaconNearbyUser extends RecyclerView.Adapter<ViewHolderBeaconNearbyUser>{
    private final static String TAG = "AdapterBeaconUser";

    private Ap1Beacon beaconTmp = null;
    private int rssiBorder = ActivityMain.rssiBorder;
    private DatabaseHelper databaseHelper;
    private Context context;

    public AdapterBeaconNearbyUser(Context context){
        databaseHelper = DatabaseHelper.getHelper(context);
    }

    @Override
    public ViewHolderBeaconNearbyUser onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_nearby_user, viewGroup, false);
        return new ViewHolderBeaconNearbyUser(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderBeaconNearbyUser viewHolder, int position){
        beaconTmp = DataStore.detectedAndAddedBeaconList.get(position);
        String companyId = beaconTmp.getIdcompany();
        String title = databaseHelper.queryForOneCompany(companyId).getCompany();
        title = title.substring(0, 1);
        viewHolder.tvBeaconNearbyUserIcon.setText(title);
        String companyColor = databaseHelper.queryForOneCompany(beaconTmp.getIdcompany()).getColor(); // default color, WHITE
        if (companyColor.equals("")) {
            companyColor = "FFFFFF"; // if not defined, use white
        }
        viewHolder.tvBeaconNearbyUserIcon.setBackgroundColor(Color.parseColor("#" + companyColor));
        viewHolder.tvBeaconNearbyUserName.setText(beaconTmp.getNickname());
        String attr = Constants.MAJOR + beaconTmp.getMajor() + Constants.MINOR + beaconTmp.getMinor();
        viewHolder.tvBeaconNearbyUserAttributes.setText(attr);
        String rssiStr = beaconTmp.getRssi();
        Log.e(TAG, "onBindViewHolder: getRssi: " + rssiStr);
        if(rssiStr.equals(""))
            rssiStr = "-60";
        if(Integer.parseInt(rssiStr) > rssiBorder){
            viewHolder.tvBeaconNearbyUserIsNearby.setText("Nearby");
            viewHolder.url = (beaconTmp.getUrlnear());
        }
        else{
            viewHolder.tvBeaconNearbyUserIsNearby.setText("Not in proximity");
            viewHolder.url = (beaconTmp.getUrlfar());
        }
    }

    @Override
    public int getItemCount() {
        return DataStore.detectedAndAddedBeaconList.size();
    }
}
