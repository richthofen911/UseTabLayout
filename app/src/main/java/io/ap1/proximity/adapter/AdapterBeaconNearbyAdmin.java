package io.ap1.proximity.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ap1.libap1beaconmngt.Ap1Beacon;
import io.ap1.libap1beaconmngt.DataStore;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderBeaconNearbyAdmin;

/**
 * Created by admin on 22/02/16.
 */
public class AdapterBeaconNearbyAdmin extends RecyclerView.Adapter<ViewHolderBeaconNearbyAdmin>{
    private final static String TAG = "AdapterNearbyAdmin";

    private Ap1Beacon beaconTmp = null;

    @Override
    public ViewHolderBeaconNearbyAdmin onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_nearby_admin, viewGroup, false);
        return new ViewHolderBeaconNearbyAdmin(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderBeaconNearbyAdmin viewHolder, final int position){
        viewHolder.setIsRecyclable(false);
        beaconTmp = DataStore.detectedBeaconList.get(position);
        String nickname = beaconTmp.getNickname();
        if(nickname == null)
            nickname = "Inactive"; // it means it's not a registered Ap1 Beacon
        else{
            viewHolder.tvArrowNearbyAdmin.setVisibility(View.GONE);
            viewHolder.ivBeaconNearbyAdminInfo.setVisibility(View.VISIBLE);
            if(Integer.parseInt(beaconTmp.getRssi()) > -60)
                viewHolder.tvBeaconNearbyAdminStatus.setText("| Registered & In Location");
            else
                viewHolder.tvBeaconNearbyAdminStatus.setText("| Registered");
        }
        viewHolder.tvBeaconNearbyAdminName.setText(nickname);
        String attrs = Constants.MAJOR + beaconTmp.getMajor() + (Constants.MINOR) + beaconTmp.getMinor();
        viewHolder.tvBeaconNearbyUserAttributes.setText(attrs);
        viewHolder.uuid = beaconTmp.getUuid();
        Log.e(TAG, "onBindViewHolder: " + viewHolder.uuid);
        viewHolder.major = beaconTmp.getMajor();
        viewHolder.minor = beaconTmp.getMinor();
        viewHolder.rssi = beaconTmp.getRssi();
        viewHolder.beaconId = beaconTmp.getId();
        viewHolder.nickname = beaconTmp.getNickname();
        viewHolder.idcompany = beaconTmp.getIdcompany();
        viewHolder.urlnear = beaconTmp.getUrlnear();
        viewHolder.urlfar = beaconTmp.getUrlfar();
        viewHolder.lat = beaconTmp.getLat();
        viewHolder.lng = beaconTmp.getLng();
        viewHolder.notifytitle = beaconTmp.getNotifytitle();
        viewHolder.notifytext = beaconTmp.getNotifytext();
        viewHolder.macaddress = beaconTmp.getMacaddress();

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.detectedBeaconList.size();
    }
}
