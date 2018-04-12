package tw.cchi.handicare.ui.menu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.ui.VibrationActivity;
import tw.cchi.handicare.ui.base.BasePresenter;
import tw.cchi.handicare.ui.detection.shock.DetectionActivity;
import tw.cchi.handicare.ui.preferences.PreferencesActivity;
import tw.cchi.handicare.ui.shock.ShockActivity;

public class MenuPresenter<V extends MenuMvpView> extends BasePresenter<V> implements MenuMvpPresenter<V> {

    @Inject AppCompatActivity activity;

    @Inject
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
        activity.startActivity(new Intent(activity, DetectionActivity.class));
    }

    @Override
    public void launchPreferences() {
        activity.startActivity(new Intent(activity, PreferencesActivity.class));
    }

}
