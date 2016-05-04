package io.ap1.proximity;

import android.graphics.Color;

/**
 * Created by admin on 09/02/16.
 */
public interface Constants {

    int PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION = 101;
    int PERMISSION_REQUEST_CODE_CAMERA = 102;
    int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103;
    int PERMISSION_REQUEST_CODE_BUNDLE = 110;

    int INTENT_REQUEST_CODE_ADD_COMPANY = 11;  // Add and Edit company use this same intent request code
    int INTENT_REQUEST_CODE_AD_BEACON = 12;  // AD_BEACON means Add Or Delete Beacon

    String MAJOR = "major: ";
    String MINOR = " minor: ";

    String USER_LOGIN_KEY_LOGINNAME = "loginName";
    String USER_LOGIN_KEY_LOGINPASSWORD = "loginPassword";
    int COLOR_WHITE = Color.parseColor("#FFFFFF");
    int COLOR_BLUE_LIGHT = Color.parseColor("#04A9CE");
    String PROFILE_IMAGE_PATH_ROOT = "http://159.203.15.85:80/api/CBCCFC6F-B6C9-A38D-FF43-FB8A5C7BD400/v1/files/profileImage/";

    String API_PATH_GET_BEACONS = "/getAllBeacons_bidv2_a.php";
    String API_PATH_ADD_BEACON = "/addBeaconv7_a.php";
    String API_PATH_DELETE_BEACON = "/deleteBeaconv4_a.php";

    String API_PATH_ADD_COMPANY = "/addCompany_a.php";
    String API_PATH_EDIT_COMPANY = "/editCompany_a.php";
    String API_PATH_DELETE_COMPANY = "/deleteCompany_a.php";
    String API_PATH_GET_COMPANIES = "/getAllCompanies_bidv2_a.php";
}
