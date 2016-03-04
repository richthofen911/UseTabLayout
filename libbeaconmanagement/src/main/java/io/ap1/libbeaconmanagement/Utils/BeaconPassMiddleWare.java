package io.ap1.libbeaconmanagement.Utils;

import android.util.Log;

import io.ap1.libbeaconmanagement.Beacon;

/**
 * Created by admin on 14/10/15.
 */
public class BeaconPassMiddleWare {
    private static Beacon currentBeacon;
    private static Beacon cacheBeacon;

    public static void pushBeacon(Beacon thisBeacon){ //always use this method before using popTheBeacon
        if(thisBeacon != null){
            currentBeacon = thisBeacon;
            cacheBeacon = currentBeacon;
        }else {
            Log.e("don't push ", "null beacon");
        }
    }

    public static Beacon popBeacon(){
        if(currentBeacon != null){
            currentBeacon = null;
            Log.e("beacon poped", cacheBeacon.getUuid());
            return cacheBeacon;
        }else{
            Log.e("pushTheBeacon ", "is needed");
            return null;
        }
    }
}
