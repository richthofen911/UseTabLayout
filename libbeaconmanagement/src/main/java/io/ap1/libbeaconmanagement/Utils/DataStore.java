package io.ap1.libbeaconmanagement.Utils;

import java.util.ArrayList;

import io.ap1.libbeaconmanagement.Beacon;

/**
 * Created by admin on 01/03/16.
 */
public class DataStore {

    public static ArrayList<Beacon> detectedBeaconList = new ArrayList<>();
    public static ArrayList<Beacon> detectedAndRegisteredBeaconList = new ArrayList<>();
    public static ArrayList<Beacon> beaconInAllPlacesList = new ArrayList<>();
    public static String urlBase = "http://159.203.15.175/filemaker";
}
