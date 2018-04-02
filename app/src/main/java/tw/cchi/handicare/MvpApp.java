package tw.cchi.handicare;

import android.app.Application;

import javax.inject.Inject;

import tw.cchi.handicare.di.component.ApplicationComponent;
import tw.cchi.handicare.di.component.DaggerApplicationComponent;
import tw.cchi.handicare.di.module.ApplicationModule;
import tw.cchi.handicare.eshock.DeviceAcup;


public class MvpApp extends Application {
    public class GlobalVariables {
        public boolean bPower = false;
        public boolean bUsb = false;
        public int nX = 1; // Strength: 1~15
        public int nY = 1; // Frequency: 1~15
        public int nZ = 1;
    }

    public GlobalVariables globalVar = new GlobalVariables();
    @Inject public DeviceAcup mDevAcup;

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this)).build();
        mApplicationComponent.inject(this);
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }


    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }
}
