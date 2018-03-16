package tw.cchi.whisttherapist.di.component;

import dagger.Component;
import tw.cchi.whisttherapist.di.PerActivity;
import tw.cchi.whisttherapist.di.module.ActivityModule;
import tw.cchi.whisttherapist.ui.MenuActivity;
import tw.cchi.whisttherapist.ui.shock.ShockActivity;
import tw.cchi.whisttherapist.ui.SplashActivity;
import tw.cchi.whisttherapist.ui.VibrationActivity;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MenuActivity activity);

    void inject(ShockActivity activity);

    void inject(SplashActivity activity);

    void inject(VibrationActivity activity);

}
