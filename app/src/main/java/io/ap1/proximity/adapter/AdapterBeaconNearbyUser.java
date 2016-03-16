package io.ap1.proximity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.libbeaconmanagement.Utils.DatabaseHelper;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.view.ActivityMain;
import io.ap1.proximity.viewholder.ViewHolderBeaconNearbyUser;

/**
 * Created by admin on 22/02/16.
 */
public class AdapterBeaconNearbyUser extends RecyclerView.Adapter<ViewHolderBeaconNearbyUser>{
    private Beacon beaconTmp = null;
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
    public void onBindViewHolder(ViewHolderBeaconNearbyUser viewHolder, final int position){
        beaconTmp = DataStore.detectedAndRegisteredBeaconList.get(position);

        String title = databaseHelper.queryForOneCompany(beaconTmp.getIdcompany()).getCompany();
        title = title.substring(0, 1);
        viewHolder.tvBeaconNearbyUserIcon.setText(title);
        viewHolder.tvBeaconNearbyUserIcon.setBackgroundColor(Color.parseColor("#" + databaseHelper.queryForOneCompany(beaconTmp.getIdcompany()).getColor()));
        viewHolder.tvBeaconNearbyUserName.setText(beaconTmp.getNickname());
        String attr = Constants.MAJOR + beaconTmp.getMinor() + Constants.MINOR + beaconTmp.getMinor();
        viewHolder.tvBeaconNearbyUserAttributes.setText(attr);
        if(Integer.parseInt(beaconTmp.getRssi()) > rssiBorder){
            viewHolder.tvBeaconNearbyUserInProximity.setText("Nearby");
            viewHolder.url = (beaconTmp.getUrlnear());
        }
        else{
            viewHolder.tvBeaconNearbyUserInProximity.setText("Not in proximity");
            viewHolder.url = (beaconTmp.getUrlfar());
        }

    }

    @Override
    public int getItemCount() {
        return DataStore.detectedAndRegisteredBeaconList.size();
    }
}
