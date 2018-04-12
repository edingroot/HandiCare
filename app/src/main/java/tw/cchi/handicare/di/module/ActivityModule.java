package tw.cchi.handicare.di.module;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.eshock.DeviceAcup;
import tw.cchi.handicare.ui.base.BaseActivity;
import tw.cchi.handicare.ui.detection.shock.DetectionMvpPresenter;
import tw.cchi.handicare.ui.detection.shock.DetectionMvpView;
import tw.cchi.handicare.ui.detection.shock.DetectionPresenter;
import tw.cchi.handicare.ui.menu.MenuMvpPresenter;
import tw.cchi.handicare.ui.menu.MenuMvpView;
import tw.cchi.handicare.ui.menu.MenuPresenter;
import tw.cchi.handicare.ui.shock.ShockMvpPresenter;
import tw.cchi.handicare.ui.shock.ShockMvpView;
import tw.cchi.handicare.ui.shock.ShockPresenter;

@Module
public class ActivityModule {

    private BaseActivity mActivity;

    public ActivityModule(BaseActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    AppCompatActivity provideActivity() {
        return mActivity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return mActivity;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    DeviceAcup provideDeviceAcup() {
        return mActivity.application.mDevAcup;
    }

    @Provides
    MenuMvpPresenter<MenuMvpView> provideMenuPresenter(MenuPresenter<MenuMvpView> presenter) {
        return presenter;
    }

    @Provides
    ShockMvpPresenter<ShockMvpView> provideShockPresenter(ShockPresenter<ShockMvpView> presenter) {
        return presenter;
    }

    @Provides
    DetectionMvpPresenter<DetectionMvpView> provideDetectionPresenter(DetectionPresenter<DetectionMvpView> presenter) {
        return presenter;
    }
}
