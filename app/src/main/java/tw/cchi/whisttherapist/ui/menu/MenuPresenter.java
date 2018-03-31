package tw.cchi.whisttherapist.ui.menu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.whisttherapist.ui.VibrationActivity;
import tw.cchi.whisttherapist.ui.base.BasePresenter;
import tw.cchi.whisttherapist.ui.preferences.PreferencesActivity;
import tw.cchi.whisttherapist.ui.shock.ShockActivity;

public class MenuPresenter<V extends MenuMvpView> extends BasePresenter<V> implements MenuMvpPresenter<V> {

    @Inject AppCompatActivity activity;

    public MenuPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void launchVibrationMode() {
        activity.startActivity(new Intent(activity, VibrationActivity.class));
    }

    @Override
    public void launchShockMode() {
        activity.startActivity(new Intent(activity, ShockActivity.class));
    }

    @Override
    public void launchDetectionMode() {
    }

    @Override
    public void launchPreferences() {
        activity.startActivity(new Intent(activity, PreferencesActivity.class));
    }

}
