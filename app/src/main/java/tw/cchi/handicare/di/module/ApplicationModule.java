package tw.cchi.handicare.di.module;

import android.app.Application;
import android.content.Context;
import android.hardware.usb.UsbManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import tw.cchi.handicare.Constants;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.di.ApplicationContext;
import tw.cchi.handicare.di.PreferenceInfo;
import tw.cchi.handicare.di.PresenterHolder;

@Module
public class ApplicationModule {

    private final MvpApp mvpApp;
    private final PresenterHolder presenterHolder;

    public ApplicationModule(MvpApp mvpApp) {
        this.mvpApp = mvpApp;
        this.presenterHolder = new PresenterHolder();
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mvpApp;
    }

    @Provides
    Application provideApplication() {
        return mvpApp;
    }

    @Provides
    MvpApp provideMvpApp() {
        return mvpApp;
    }

    @Provides
    @PreferenceInfo
    String providePreferenceName() {
        return Constants.PREF_NAME;
    }

    @Provides
    MvpApp.GlobalVariables provideGlobalVariables() {
        return mvpApp.globalVar;
    }

    @Provides
    UsbManager provideUsbManager() {
        return (UsbManager) provideApplication().getSystemService(Context.USB_SERVICE);
    }

    @Provides
    @Singleton
    PresenterHolder providePresenterHolder() {
        return presenterHolder;
    }

}
