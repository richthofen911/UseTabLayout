package io.ap1.proximity.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.view.ActivityBeaconDetail;
import io.ap1.proximity.view.ActivityMain;

/**
 * Created by admin on 22/02/16.
 */
public class ViewHolderBeaconNearbyAdmin extends RecyclerView.ViewHolder{

    public int selfPosition;

    public TextView tvBeaconNearbyAdminName;
    public TextView tvBeaconNearbyUserAttributes;
    public TextView tvArrowNearbyAdmin;
    public RelativeLayout beaconNearbyAdminCell;

    public String uuid = "unknown";
    public String major = "unknown";
    public String minor = "unknown";
    public String rssi = "unknown";

    public ViewHolderBeaconNearbyAdmin(View rootview){
        super(rootview);

        tvBeaconNearbyAdminName = (TextView) rootview.findViewById(R.id.tv_beacon_nearby_admin_name);
        tvBeaconNearbyUserAttributes = (TextView) rootview.findViewById(R.id.tv_beacon_nearby_admin_attributes);
        tvArrowNearbyAdmin = (TextView) rootview.findViewById(R.id.tv_arrow_nearby_admin);
        beaconNearbyAdminCell = (RelativeLayout) rootview.findViewById(R.id.beacon_nearby_admin_cell);


        beaconNearbyAdminCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ActivityBeaconDetail.class);
                intent.putExtra("uuid", uuid);
                intent.putExtra("major", major);
                intent.putExtra("minor", minor);
                intent.putExtra("rssi", rssi);
                context.startActivity(intent);

                //((ActivityMain)context).startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_ADD_BEACON);
            }
        });

    }
}
