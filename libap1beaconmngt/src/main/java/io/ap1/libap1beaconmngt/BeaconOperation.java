package io.ap1.libap1beaconmngt;

/**
 * This class contains all beacon operations definition.
 * Created by richthofen80 on 10/2/15.
 */
public class BeaconOperation {
    public static boolean equals (Ap1Beacon beacon1, Ap1Beacon beacon2){
        return ((beacon1.getUuid().replace("-", "").equals(beacon2.getUuid().replace("-", ""))) &&
        beacon1.getMajor().equals(beacon2.getMajor()) &&
        beacon1.getMinor().equals(beacon2.getMinor()));
    }
}