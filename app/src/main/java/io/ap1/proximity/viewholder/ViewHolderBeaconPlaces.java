package io.ap1.proximity.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.ap1.proximity.R;
import io.ap1.proximity.view.ActivityBeaconUrlContent;
import io.ap1.proximity.view.ActivityMain;

/**
 * Created by admin on 16/02/16.
 */
public class ViewHolderBeaconPlaces extends RecyclerView.ViewHolder {

    public TextView tvBeaconPlacesIcon;
    public TextView tvBeaconPlacesName;
    public TextView tvBeaconPlacesAttributes;

    public TextView tvArrowPlaces;
    public RelativeLayout beaconPlacesCell;

    public int selfPosition;
    public String url = "http://ap1.io/";

    public ViewHolderBeaconPlaces(View rootView){
        super(rootView);

        tvBeaconPlacesIcon = (TextView) rootView.findViewById(R.id.tv_beacon_places_icon);
        tvBeaconPlacesName = (TextView) rootView.findViewById(R.id.tv_beacon_places_name);
        tvBeaconPlacesAttributes = (TextView) rootView.findViewById(R.id.tv_beacon_places_attributes);
        //ivArrow = (ImageView) rootView.findViewById(R.id.iv_arrow);
        tvArrowPlaces = (TextView) rootView.findViewById(R.id.tv_places_arrow);
        beaconPlacesCell = (RelativeLayout) rootView.findViewById(R.id.beacon_places_cell);

        beaconPlacesCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                context.startActivity(new Intent(context, ActivityBeaconUrlContent.class).putExtra("url", url));
            }
        });
    }
}
