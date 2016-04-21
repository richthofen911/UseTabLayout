package io.ap1.proximity.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.ap1.proximity.R;

/**
 * Created by admin on 22/02/16.
 */
public class ViewHolderBeaconNearbyUser extends RecyclerView.ViewHolder{

    public int selfPosition;

    public TextView tvBeaconNearbyUserIcon;
    public TextView tvBeaconNearbyUserName;
    public TextView tvBeaconNearbyUserAttributes;
    public TextView tvBeaconNearbyUserIsNearby;
    public TextView tvArrowNearbyUser;
    public boolean isNearby;
    public RelativeLayout beaconNearbyUserCell;

    public String url = "http://ap1.io/";

    public ViewHolderBeaconNearbyUser(View rootView){
        super(rootView);

        tvBeaconNearbyUserIcon = (TextView) rootView.findViewById(R.id.tv_beacon_places_icon);
        tvBeaconNearbyUserName = (TextView) rootView.findViewById(R.id.tv_beacon_places_name);
        tvBeaconNearbyUserAttributes = (TextView) rootView.findViewById(R.id.tv_beacon_places_attributes);
        tvBeaconNearbyUserIsNearby = (TextView) rootView.findViewById(R.id.tv_beacon_nearby_user_is_nearby);
        beaconNearbyUserCell = (RelativeLayout) rootView.findViewById(R.id.beacon_nearby_user_cell);
    }
}
