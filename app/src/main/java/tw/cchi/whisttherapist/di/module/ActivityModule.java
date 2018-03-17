package tw.cchi.whisttherapist.di.module;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.whisttherapist.di.ActivityContext;
import tw.cchi.whisttherapist.eshock.DeviceAcup;
import tw.cchi.whisttherapist.ui.base.BaseActivity;
import tw.cchi.whisttherapist.ui.shock.ShockMvpPresenter;
import tw.cchi.whisttherapist.ui.shock.ShockMvpView;
import tw.cchi.whisttherapist.ui.shock.ShockPresenter;

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
    ShockMvpPresenter<ShockMvpView> provideShockPresenter(ShockPresenter<ShockMvpView> presenter) {
        return presenter;
    }
}
