package io.ap1.proximity.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderBeaconNearbyAdmin;

/**
 * Created by admin on 22/02/16.
 */
public class AdapterBeaconNearbyAdmin extends RecyclerView.Adapter<ViewHolderBeaconNearbyAdmin>{
    private Beacon beaconTmp = null;

    @Override
    public ViewHolderBeaconNearbyAdmin onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_nearby_admin, viewGroup, false);
        return new ViewHolderBeaconNearbyAdmin(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderBeaconNearbyAdmin viewHolder, final int position){
        beaconTmp = DataStore.detectedBeaconList.get(position);
        String nickname = beaconTmp.getNickname();
        if(nickname == null)
            nickname = "Inactive";
        if(!nickname.equals("Inactive")){
            viewHolder.tvArrowNearbyAdmin.setVisibility(View.GONE);
            viewHolder.ivBeaconNearbyAdminInfo.setVisibility(View.VISIBLE);
            if(Integer.parseInt(beaconTmp.getRssi()) < 70)
                viewHolder.tvBeaconNearbyAdminStatus.setText("| Registered & In Location");
            else
                viewHolder.tvBeaconNearbyAdminStatus.setText("| Registered");
        }
        viewHolder.tvBeaconNearbyAdminName.setText(nickname);
        String attrs = Constants.MAJOR + beaconTmp.getMajor() + (Constants.MINOR) + beaconTmp.getMinor();
        viewHolder.tvBeaconNearbyUserAttributes.setText(attrs);
        viewHolder.uuid = beaconTmp.getUuid();
        viewHolder.major = beaconTmp.getMajor();
        viewHolder.minor = beaconTmp.getMinor();
        viewHolder.rssi = beaconTmp.getRssi();
        viewHolder.beaconId = beaconTmp.getId();

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.detectedBeaconList.size();
    }
}
