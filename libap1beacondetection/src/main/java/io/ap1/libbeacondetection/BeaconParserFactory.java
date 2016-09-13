package io.ap1.libbeacondetection;

import org.altbeacon.beacon.BeaconParser;

/**
 * Created by admin on 13/05/16.
 */
public class BeaconParserFactory {

    public static BeaconParser create(int typeOfBeacon){
        switch (typeOfBeacon){
            case BeaconParserType.IBEACON:
                return new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
            default:
                return null;
        }
    }
}
