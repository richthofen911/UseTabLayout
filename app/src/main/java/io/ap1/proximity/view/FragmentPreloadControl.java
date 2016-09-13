package io.ap1.proximity.view;

import android.support.v4.app.Fragment;

/**
 * Created by admin on 02/03/16.
 */
public abstract class FragmentPreloadControl extends Fragment {
    protected boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()){
            isVisible = true;
            onVisible();
        }else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible(){
        lazyLoad();
    }

    protected void onInvisible(){

    }

    protected abstract void lazyLoad();
}
