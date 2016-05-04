package io.ap1.proximity.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Company;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderCompanyList;

/**
 * Created by admin on 04/03/16.
 */
public class AdapterCompanyList extends RecyclerView.Adapter<ViewHolderCompanyList> {
    private ArrayList<Company> companyList;

    private Company companyTmp;

    public AdapterCompanyList (ArrayList<Company> companyList){
        this.companyList = companyList;
    }

    public void setDataSource(ArrayList<Company> companyList){
        this.companyList = companyList;
    }

    @Override
    public ViewHolderCompanyList onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.company_in_list, viewGroup, false);
        return new ViewHolderCompanyList(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderCompanyList viewHolder, final int position){
        viewHolder.setIsRecyclable(false);
        companyTmp = companyList.get(position);
        String companyName = companyTmp.getCompany();

        viewHolder.tvCompanyToBeSelected.setText(companyName);
        viewHolder.id = companyTmp.getId();
        viewHolder.color = companyTmp.getColor();
        viewHolder.lat = companyTmp.getLat();
        viewHolder.lng = companyTmp.getLng();
        viewHolder.hash = companyTmp.getHash();
        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }
}
