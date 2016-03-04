package io.ap1.proximity.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderBeaconPlaces;

/**
 * Created by admin on 16/02/16.
 */
public class AdapterBeaconPlaces extends RecyclerView.Adapter<ViewHolderBeaconPlaces>{
    private Beacon beaconTmp = null;

    @Override
    public ViewHolderBeaconPlaces onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_places, viewGroup, false);
        return new ViewHolderBeaconPlaces(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderBeaconPlaces beaconInPlace, final int position){
        beaconTmp = DataStore.beaconInAllPlacesList.get(position);
        if(!beaconTmp.getNickname().equals("groupDivider")){
            String title = beaconTmp.getCompanyname();
            title = title.substring(0, 1);
            beaconInPlace.tvBeaconPlacesIcon.setText(title);
            beaconInPlace.tvBeaconPlacesIcon.setBackgroundColor(Color.parseColor("#" + beaconTmp.getColor()));
            beaconInPlace.tvBeaconPlacesName.setText(beaconTmp.getNickname());
            beaconInPlace.tvBeaconPlacesAttributes.setText(Constants.MAJOR + beaconTmp.getMinor() + Constants.MINOR + beaconTmp.getMinor());
            beaconInPlace.tvArrowPlaces.setText(">");
            beaconInPlace.url = (DataStore.beaconInAllPlacesList.get(position).getUrlfar());
        }else {
            beaconInPlace.beaconPlacesCell.setClickable(false);
            beaconInPlace.tvBeaconPlacesIcon.setText("");
            beaconInPlace.tvBeaconPlacesIcon.setBackgroundColor(Constants.COLOR_WHITE);
            beaconInPlace.tvBeaconPlacesAttributes.setText("");
            beaconInPlace.tvBeaconPlacesName.setText(beaconTmp.getCompanyname());
        }

        beaconInPlace.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.beaconInAllPlacesList.size();
    }
}
