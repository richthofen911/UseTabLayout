package io.ap1.proximity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.ap1.proximity.view.FragmentChat;
import io.ap1.proximity.view.FragmentMap;
import io.ap1.proximity.view.FragmentNearby;
import io.ap1.proximity.view.FragmentPlaces;

/**
 * Created by admin on 09/02/16.
 */
public class AdapterFragmentPager extends FragmentPagerAdapter {
    private static final String[] listTitle = {"Chat", "All", "Places", "Map"};

    public AdapterFragmentPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return listTitle.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentChat();
            case 1:
                return new FragmentNearby();
            case 2:
                return new FragmentPlaces();
            case 3:
                return new FragmentMap();
            default:
                return null;
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {

        return listTitle[position];
    }

}
