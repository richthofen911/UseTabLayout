package io.ap1.proximity.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
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

    public TextView tvBeaconNearbyAdminName;
    public TextView tvBeaconNearbyAdminStatus;
    public TextView tvBeaconNearbyUserAttributes;
    public TextView tvArrowNearbyAdmin;
    public ImageView ivBeaconNearbyAdminInfo;
    public RelativeLayout beaconNearbyAdminCell;

    public String uuid = "unknown";
    public String major = "unknown";
    public String minor = "unknown";
    public String rssi = "unknown";
    public String beaconId = "unknown";
    public String nickname = "unknown";
    public String idcompany = "unknown";
    public String urlnear = "unknown";
    public String urlfar = "unknown";
    public String notifytext = "unknown";
    public String notifytitle = "unknown";
    public String lat = "unknown";
    public String lng = "unknown";
    public String macaddress = "unknown";

    public ViewHolderBeaconNearbyAdmin(View rootview){
        super(rootview);

        tvBeaconNearbyAdminName = (TextView) rootview.findViewById(R.id.tv_beacon_nearby_admin_name);
        tvBeaconNearbyAdminStatus = (TextView) rootview.findViewById(R.id.tv_beacon_nearby_admin_status);
        tvBeaconNearbyUserAttributes = (TextView) rootview.findViewById(R.id.tv_beacon_nearby_admin_attributes);
        tvArrowNearbyAdmin = (TextView) rootview.findViewById(R.id.tv_arrow_nearby_admin);
        ivBeaconNearbyAdminInfo = (ImageView) rootview.findViewById(R.id.iv_nearby_admin_info);
        beaconNearbyAdminCell = (RelativeLayout) rootview.findViewById(R.id.beacon_nearby_admin_cell);

        Context context = rootview.getContext();
        final Activity activity = (ActivityMain)context;

        tvArrowNearbyAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ActivityBeaconDetail.class);
                intent.putExtra("uuid", uuid);
                intent.putExtra("major", major);
                intent.putExtra("minor", minor);
                intent.putExtra("rssi", rssi);
                intent.putExtra("addOrDel", "add");
                //intent.putExtra("position", getAdapterPosition());
                intent.putExtra("position", getLayoutPosition());

                ActivityMain.instance.stopScanning();

                //context.startActivity(intent);
                //((ActivityMain)context).startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_AD_BEACON); // AD means add/delete
                activity.startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_AD_BEACON);
            }
        });

        ivBeaconNearbyAdminInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ActivityBeaconDetail.class);
                intent.putExtra("uuid", uuid);
                intent.putExtra("major", major);
                intent.putExtra("minor", minor);
                intent.putExtra("rssi", rssi);
                intent.putExtra("id", beaconId);
                intent.putExtra("nickname", nickname);
                intent.putExtra("idcompany", idcompany);
                intent.putExtra("urlnear", urlnear);
                intent.putExtra("urlfar", urlfar);
                intent.putExtra("notifytitle", notifytitle);
                intent.putExtra("notifytext", notifytext);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("addOrDel", "del");
                //intent.putExtra("position", getAdapterPosition());
                intent.putExtra("position", getLayoutPosition());

                ActivityMain.instance.stopScanning();

                activity.startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_AD_BEACON);
            }
        });

    }
}
