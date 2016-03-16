package io.ap1.proximity.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.ap1.libbeaconmanagement.Company;
import io.ap1.proximity.R;

/**
 * Created by admin on 04/03/16.
 */
public class ViewHolderCompanyList extends RecyclerView.ViewHolder{
    public TextView tvCompanyToBeSelected;
    public TextView tvCompanyInfo;

    public int selfPosition;
    public String url = "http://ap1.io/";

    public ViewHolderCompanyList(View rootView){
        super(rootView);

        tvCompanyToBeSelected = (TextView) rootView.findViewById(R.id.tv_company_in_list_name);
        tvCompanyInfo = (TextView) rootView.findViewById(R.id.tv_company_in_list_info);

        tvCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //v.getContext().startActivity(ActivityMain.intentShowBeaconUrlContent.putExtra("url", url));
            }
        });
    }
}
