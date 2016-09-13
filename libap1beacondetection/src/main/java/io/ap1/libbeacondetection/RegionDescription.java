package io.ap1.libbeacondetection;

import android.support.annotation.NonNull;

import org.altbeacon.beacon.Identifier;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by admin on 11/05/16.
 */
public class RegionDescription implements Serializable{
    private String uniqueName;
    private Identifier uuid;
    private Identifier major;
    private Identifier minor;

    public RegionDescription(@NonNull String uniqueName, String uuid, int major, int minor){
        this.uniqueName = uniqueName;
        if(uuid != null)
            this.uuid = Identifier.fromUuid(UUID.fromString(uuid));
        else
            this.uuid = null;
        if(major > 0)
            this.major = Identifier.fromInt(major);
        if(minor > 0)
            this.minor = Identifier.fromInt(minor);
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public Identifier getUuid() {
        return uuid;
    }

    public Identifier getMajor() {
        return major;
    }

    public Identifier getMinor(){
        return minor;
    }
}
