package io.ap1.libbeacondetection;

/**
 * Created by admin on 13/05/16.
 */
public interface BeaconParserType {
    int IBEACON = 21;
    int EDDYSTONE_BEACON = 22;

    // Detect the Eddystone main identifier (UID) frame:
    // beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
    // Detect the Eddystone telemetry (TLM) frame:
    //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
    // Detect the Eddystone URL frame:
    //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
}
