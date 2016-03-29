package io.ap1.proximity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.ap1.proximity.MyBackendlessUser;

/**
 * Created by admin on 29/03/16.
 */
public class AppDataStore {
    public static ArrayList<MyBackendlessUser> userList = new ArrayList<>();
    public static HashMap<String, String> duplicateCheck = new HashMap<>();
    public static final SimpleDateFormat myDateFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
}
