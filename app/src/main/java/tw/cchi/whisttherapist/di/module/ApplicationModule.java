package tw.cchi.whisttherapist.di.module;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import tw.cchi.whisttherapist.MvpApp;

@Module
public class ApplicationModule {

    private final MvpApp mvpApp;

    public ApplicationModule(MvpApp mvpApp) {
        this.mvpApp = mvpApp;
    }

    @Provides
    Context provideContext() {
        return mvpApp;
    }

    @Provides
    Application provideApplication() {
        return mvpApp;
    }

    @Provides
    MvpApp provideMvcApp() {
        return mvpApp;
    }

}
