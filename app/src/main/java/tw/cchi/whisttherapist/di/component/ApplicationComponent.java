package tw.cchi.whisttherapist.di.component;

import android.app.Application;
import android.content.Context;
import android.hardware.usb.UsbManager;

import javax.inject.Singleton;

import dagger.Component;
import tw.cchi.whisttherapist.MvpApp;
import tw.cchi.whisttherapist.di.PresenterHolder;
import tw.cchi.whisttherapist.di.module.ApplicationModule;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MvpApp app);

    // ----- Methods below are used by Dagger implementation of ActivityComponent ----- //
    Context context();

    Application application();

    MvpApp mvpApp();

    MvpApp.GlobalVariables globalVariables();

    UsbManager usbManager();

    PresenterHolder presenterHolder();

}
