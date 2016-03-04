package io.ap1.proximity.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ap1.proximity.R;
import io.ap1.proximity.adapter.AdapterBeaconPlaces;

public class FragmentPlaces extends FragmentPreloadControl {

    private static final String TAG = "Fragment Places";


    private RecyclerView recyclerViewBeaconPlaces;
    private LinearLayoutManager linearLayoutManager;

    private Toolbar toolbar;

    public FragmentPlaces() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG,  "onCreateView");
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        toolbar = ((ActivityMain)getActivity()).toolbar;


        recyclerViewBeaconPlaces = (RecyclerView) view.findViewById(R.id.recyclerView_beacon_places);
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerViewBeaconPlaces.setLayoutManager(linearLayoutManager);
        recyclerViewBeaconPlaces.setHasFixedSize(true);
        recyclerViewBeaconPlaces.setAdapter(((ActivityMain)getActivity()).adapterBeaconPlaces);

        return view;
    }

    @Override
    protected void lazyLoad(){
        Log.e(TAG, "onVisibleLazyLoad");
        toolbar = ((ActivityMain) getActivity()).toolbar;
        toolbar.setTitle("By Company");
    }

    @Override
    protected void onInvisible(){
        Log.e(TAG, "onInvisible");
        //if (toolbar != null)
        //    toolbar.removeView(connect);
    }

}
