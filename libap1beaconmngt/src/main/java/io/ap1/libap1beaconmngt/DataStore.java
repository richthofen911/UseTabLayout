package io.ap1.libap1beaconmngt;

import java.util.ArrayList;

/**
 * Created by admin on 01/03/16.
 */
public class DataStore {

    public static ArrayList<Ap1Beacon> detectedBeaconList = new ArrayList<>();
    public static ArrayList<Ap1Beacon> detectedAndAddedBeaconList = new ArrayList<>();
    public static ArrayList<Ap1Beacon> registeredAndGroupedBeaconList = new ArrayList<>(); // grouped by company
    public static ArrayList<Ap1Beacon> beaconAllList = new ArrayList<>(); // all the beacons having the same idparent, grouped by company id
    public static ArrayList<Company> companyInList = new ArrayList<>();
    public static String urlBase = "http://159.203.15.175/filemaker";
}
