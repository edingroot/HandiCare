package tw.cchi.handicare.helper.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import tw.cchi.handicare.di.ApplicationContext;
import tw.cchi.handicare.di.PreferenceInfo;

@Singleton
public class AppPreferencesHelper implements PreferencesHelper {
    private static final String KEY_BT_DEVICE_ADDR = "KEY_BT_DEVICE_ADDR";

    private final SharedPreferences mPrefs;

    @Inject
    public AppPreferencesHelper(@ApplicationContext Context context,
                                @PreferenceInfo String prefFileName) {
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    }

    @Nullable
    public String getBTDeviceAddress() {
        return mPrefs.getString(KEY_BT_DEVICE_ADDR, null);
    }

    public void setBTDeviceAddress(String btDeviceAddress) {
        mPrefs.edit().putString(KEY_BT_DEVICE_ADDR, btDeviceAddress).apply();
    }

}
