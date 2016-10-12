package io.ap1.proximity.view;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.ap1.libap1beaconmngt.ServiceBeaconManagement;
import io.ap1.proximity.R;

public class FragmentNearby extends FragmentPreloadControl {

    private static final String TAG = "FragmentNearbyUser";
    public RecyclerView recyclerViewBeaconNearby;
    private Toolbar toolbar;
    private TextView tvToolbarEnd;

    public ServiceBeaconManagement beaconManagementService;

    private static FragmentNearby instance;

    public FragmentNearby() {
        // Required empty public constructor
    }

    public static FragmentNearby getInstance(){
        if(instance == null)
            instance = new FragmentNearby();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.e(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        toolbar = ((ActivityMain)getActivity()).toolbar;

        recyclerViewBeaconNearby = (RecyclerView) view.findViewById(R.id.recyclerView_beacon_nearby);

        beaconManagementService = ((ActivityMain)getActivity()).serviceMyBeaconMngt;
        if(beaconManagementService != null)
            beaconManagementService.setRecyclerView(recyclerViewBeaconNearby);
        else
            Log.e(TAG, "onCreateView: beacon service is null");
        recyclerViewBeaconNearby.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerViewBeaconNearby.setHasFixedSize(true);
        recyclerViewBeaconNearby.setAdapter(((ActivityMain)getActivity()).adapterBeaconNearbyUser);

        return view;
    }

    @Override
    protected void lazyLoad(){
        Log.e(TAG, "onVisibleLazyLoad");

        recyclerViewBeaconNearby.setAdapter(((ActivityMain) getActivity()).adapterBeaconNearbyUser);
        toolbar = ((ActivityMain)getActivity()).toolbar;
        toolbar.setTitle("Nearby");
        tvToolbarEnd = ((ActivityMain)getActivity()).tvToolbarEnd;
        tvToolbarEnd.setVisibility(View.VISIBLE);
        tvToolbarEnd.setText("Admin");
        tvToolbarEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hiddenPermission = tvToolbarEnd.getText().toString();
                // hiddenPermission equals to User means current permission is Admin
                if(hiddenPermission.equals("User")){
                    tvToolbarEnd.setText("Admin");
                    // swap to permission User
                    recyclerViewBeaconNearby.setAdapter(null);
                    recyclerViewBeaconNearby.setAdapter(((ActivityMain)getActivity()).adapterBeaconNearbyUser);

                }else if(hiddenPermission.equals("Admin")){ // which means current permission is User
                    tvToolbarEnd.setText("User");
                    // swap to permission Admin
                    recyclerViewBeaconNearby.setAdapter(null);
                    recyclerViewBeaconNearby.setAdapter(((ActivityMain)getActivity()).adapterBeaconNearbyAdmin);
                }else {
                    Toast.makeText(getContext(), "Role error", Toast.LENGTH_SHORT).show();
                }
               // recyclerViewBeaconNearby.getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onInvisible() {
        Log.e(TAG, "onInvisible");
        if(tvToolbarEnd != null)
            tvToolbarEnd.setVisibility(View.GONE);
    }

    public RecyclerView accessRecyclerView(){
        return recyclerViewBeaconNearby;
    }
}
