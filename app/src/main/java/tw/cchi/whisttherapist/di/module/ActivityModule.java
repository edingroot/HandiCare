package tw.cchi.whisttherapist.di.module;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.whisttherapist.base.BaseActivity;
import tw.cchi.whisttherapist.eshock.DeviceAcup;
import tw.cchi.whisttherapist.ui.ShockPresenter;
import tw.cchi.whisttherapist.ui.ShockView;

@Module
public class ActivityModule {

    private BaseActivity mActivity;

    public ActivityModule(BaseActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    Context provideContext() {
        return mActivity;
    }

    @Provides
    AppCompatActivity provideActivity() {
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
    ShockPresenter<ShockView> provideShockPresenter(ShockPresenter<ShockView> presenter) {
        return presenter;
    }
}
