package tw.cchi.handicare.ui.preferences;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.ui.base.BasePresenter;

public class PreferencesPresenter<V extends PreferencesMvpView> extends BasePresenter<V> implements PreferencesMvpPresenter<V> {

    @Inject AppCompatActivity activity;

    @Inject
    public PreferencesPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

}
