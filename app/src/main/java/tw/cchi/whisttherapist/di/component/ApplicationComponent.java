package tw.cchi.whisttherapist.di.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import tw.cchi.whisttherapist.MvpApp;
import tw.cchi.whisttherapist.di.module.ApplicationModule;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MvpApp app);

    Context context();

    Application application();

    MvpApp mvpApp();

}
