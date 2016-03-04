package io.ap1.proximity;

import android.Manifest;
import android.graphics.Color;

/**
 * Created by admin on 09/02/16.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    static final String MAJOR = "major: ";
    String MINOR = " minor: ";
    int COLOR_WHITE = Color.parseColor("#FFFFFF");
    int COLOR_BLUE_LIGHT = Color.parseColor("#04A9CE");

}
