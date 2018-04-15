package tw.cchi.handicare;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import javax.inject.Inject;

import tw.cchi.handicare.device.BlunoLibraryService;
import tw.cchi.handicare.di.component.ApplicationComponent;
import tw.cchi.handicare.di.component.DaggerApplicationComponent;
import tw.cchi.handicare.di.module.ApplicationModule;
import tw.cchi.handicare.device.eshock.DeviceAcup;

public class MvpApp extends Application {

    public GlobalVariables globalVar = new GlobalVariables();
    @Inject public DeviceAcup mDevAcup;

    private ApplicationComponent mApplicationComponent;
    private ServiceConnection blunoLibraryServiceConnection;
    private BlunoLibraryService blunoLibraryService;
    private boolean blunoLibraryServiceConnected;

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

    public void getBlunoLibraryService(final OnGotServiceListener listener) {
        if (blunoLibraryServiceConnected) {
            listener.gotService(blunoLibraryService);
        } else {
            blunoLibraryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    blunoLibraryService = ((BlunoLibraryService.ServiceBinder) service).getService();
                    listener.gotService(blunoLibraryService);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    blunoLibraryService = null;
                    blunoLibraryServiceConnected = false;
                }
            };

            blunoLibraryServiceConnected = bindService(
                new Intent(
                    this, BlunoLibraryService.class),
                    blunoLibraryServiceConnection, BIND_AUTO_CREATE
            );
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (blunoLibraryServiceConnected)
            unbindService(blunoLibraryServiceConnection);
    }

    public class GlobalVariables {
        public boolean bPower = false;
        public boolean bUsb = false;
        public int nX = 1; // Strength: 1~15
        public int nY = 1; // Frequency: 1~15
        public int nZ = 1;
    }

    public interface OnGotServiceListener {
        void gotService(Service service);
    }

}
