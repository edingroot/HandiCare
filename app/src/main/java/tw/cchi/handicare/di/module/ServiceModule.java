package tw.cchi.handicare.di.module;

import android.app.Service;
import android.os.Handler;
import android.os.Looper;

import dagger.Module;
import dagger.Provides;
import tw.cchi.handicare.di.MainLooper;

@Module
public class ServiceModule {

    private final Service mService;

    public ServiceModule(Service service) {
        mService = service;
    }

    @Provides
    @MainLooper
    Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
