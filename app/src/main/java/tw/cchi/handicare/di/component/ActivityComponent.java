package tw.cchi.handicare.di.component;

import dagger.Component;
import tw.cchi.handicare.di.PerActivity;
import tw.cchi.handicare.di.module.ActivityModule;
import tw.cchi.handicare.ui.detection.DetectionActivity;
import tw.cchi.handicare.ui.menu.MenuActivity;
import tw.cchi.handicare.ui.preferences.PreferencesActivity;
import tw.cchi.handicare.ui.shock.ShockActivity;
import tw.cchi.handicare.ui.SplashActivity;
import tw.cchi.handicare.ui.vibration.VibrationActivity;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(SplashActivity activity);

    void inject(MenuActivity activity);

    void inject(PreferencesActivity activity);

    void inject(ShockActivity activity);

    void inject(VibrationActivity activity);

    void inject(DetectionActivity activity);

}
