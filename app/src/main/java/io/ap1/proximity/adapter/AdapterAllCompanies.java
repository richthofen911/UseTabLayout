package io.ap1.proximity.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ap1.libbeaconmanagement.Utils.DataStore;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderCompany;

/**
 * Created by admin on 04/03/16.
 */
public class AdapterAllCompanies extends RecyclerView.Adapter<ViewHolderCompany> {
    @Override
    public ViewHolderCompany onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beacon_in_list_nearby_admin, viewGroup, false);
        return new ViewHolderCompany(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderCompany viewHolder, final int position){

        viewHolder.tvCompanyToBeSelected.setText("unknown");
        viewHolder.tvCompanyInfo.setText("undefined");

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.detectedBeaconList.size();
    }
}
