package io.ap1.proximity;

import android.Manifest;
import android.graphics.Color;

/**
 * Created by admin on 09/02/16.
 */
public interface Constants {

    int PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION = 101;
    int PERMISSION_REQUEST_CODE_CAMERA = 102;
    int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103;
    int PERMISSION_REQUEST_CODE_BUNDLE = 110;

    String MAJOR = "major: ";
    String MINOR = " minor: ";

    String USER_LOGIN_KEY_LOGINNAME = "loginName";
    String USER_LOGIN_KEY_LOGINPASSWORD = "loginPassword";
    int COLOR_WHITE = Color.parseColor("#FFFFFF");
    int COLOR_BLUE_LIGHT = Color.parseColor("#04A9CE");
    String PROFILE_IMAGE_PATH_ROOT = "http://159.203.15.85:80/api/CBCCFC6F-B6C9-A38D-FF43-FB8A5C7BD400/v1/files/profileImage/";
    //String PROXIMITY_API_BASE = "http://159.203.15.85:80/api" + ;
}
