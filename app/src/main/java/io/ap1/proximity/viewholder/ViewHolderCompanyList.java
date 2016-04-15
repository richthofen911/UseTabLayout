package io.ap1.proximity.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.view.ActivityCompanyDetails;
import io.ap1.proximity.view.ActivityCompanyList;

/**
 * Created by admin on 04/03/16.
 */
public class ViewHolderCompanyList extends RecyclerView.ViewHolder{
    public TextView tvCompanyToBeSelected;
    public TextView tvCompanyInfo;

    public int selfPosition;
    public String id;
    public String color;
    public String lat;
    public String lng;
    public String hash;
    public String url = "http://ap1.io/";

    public ViewHolderCompanyList(View rootView){
        super(rootView);

        tvCompanyToBeSelected = (TextView) rootView.findViewById(R.id.tv_company_in_list_name);
        tvCompanyInfo = (TextView) rootView.findViewById(R.id.tv_company_in_list_info);

        /*
        tvCompanyToBeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("company", tvCompanyToBeSelected.getText().toString());
                resultIntent.putExtra("id", id);
                Activity hostActivity = (ActivityCompanyList)v.getContext();
                hostActivity.setResult(hostActivity.RESULT_OK, resultIntent);
                hostActivity.finish();
            }
        });
        */
        tvCompanyToBeSelected.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete_company:
                                ((ActivityCompanyList)v.getContext()).deleteCompany(hash);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.inflate(R.menu.delete_company);
                popup.show();
                return true;
            }
        });

        tvCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((ActivityCompanyList)v.getContext(), ActivityCompanyDetails.class);
                intent.putExtra("company", tvCompanyToBeSelected.getText().toString());
                intent.putExtra("color", color);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("hash", hash);
                intent.putExtra("addOrEdit", "edit");
                ((ActivityCompanyList) v.getContext()).startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_ADD_COMPANY);
            }
        });
    }
}
