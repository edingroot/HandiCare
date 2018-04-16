package tw.cchi.handicare.di.component;

import android.app.Application;
import android.content.Context;
import android.hardware.usb.UsbManager;

import javax.inject.Singleton;

import dagger.Component;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.device.eshock.DeviceAcup;
import tw.cchi.handicare.di.ApplicationContext;
import tw.cchi.handicare.di.PresenterHolder;
import tw.cchi.handicare.di.module.ApplicationModule;
import tw.cchi.handicare.helper.pref.PreferencesHelper;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MvpApp app);

    // ----- Methods below are used by Dagger implementation of ActivityComponent ----- //
    @ApplicationContext
    Context context();

    Application application();

    MvpApp mvpApp();

    PreferencesHelper preferenceHelper();

    MvpApp.GlobalVariables globalVariables();

    UsbManager usbManager();

    PresenterHolder presenterHolder();

}
