package tw.cchi.handicare.helper.pref;

import android.support.annotation.Nullable;

public interface PreferencesHelper {

//    enum LoggedInMode {
//        LOGGED_OUT,
//        FB,
//        SERVER
//    }

    @Nullable
    String getBTDeviceAddress();

    void setBTDeviceAddress(String btDeviceAddress);

}
