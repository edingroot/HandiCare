package tw.cchi.handicare.ui.menu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.BlunoLibraryService;
import tw.cchi.handicare.helper.pref.PreferencesHelper;
import tw.cchi.handicare.ui.VibrationActivity;
import tw.cchi.handicare.ui.base.BasePresenter;
import tw.cchi.handicare.ui.detection.DetectionActivity;
import tw.cchi.handicare.ui.preferences.PreferencesActivity;
import tw.cchi.handicare.ui.shock.ShockActivity;

public class MenuPresenter<V extends MenuMvpView> extends BasePresenter<V> implements MenuMvpPresenter<V> {

    @Inject MvpApp mvpApp;
    @Inject AppCompatActivity activity;
    @Inject PreferencesHelper preferencesHelper;

    private BlunoLibraryService blunoLibraryService;
    private boolean blunoLibOnResumeCalled = false;

    @Inject
    public MenuPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void startServices() {
        activity.startService(new Intent(activity, BlunoLibraryService.class));

        // Check if bluetooth & location enabled and auto connect device if available
        String savedDeviceAddress = preferencesHelper.getBTDeviceAddress();
        connectBlunoLibraryService().subscribe(blunoLibraryService -> {
            if (!blunoLibOnResumeCalled)
                blunoLibraryService.onResumeProcess(activity);

            // Check if location enabled
            blunoLibraryService.checkAskEnableLocation(activity).subscribe(result -> {
                if (!result) {
                    getMvpView().showToast(R.string.error_location_not_enabled);
                    return;
                }

                if (savedDeviceAddress != null) {
                    switch (blunoLibraryService.getConnectionState()) {
                        case isNull:
                        case isToScan:
                            blunoLibraryService.connect("-", savedDeviceAddress);
                            break;
                    }
                }
            });
        });
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

    private Observable<BlunoLibraryService> connectBlunoLibraryService() {
        Observable<BlunoLibraryService> observable;

        if (blunoLibraryService == null) {
            observable = Observable.create(emitter -> {
                mvpApp.getBlunoLibraryService(service -> {
                    blunoLibraryService = (BlunoLibraryService) service;
                    emitter.onNext(blunoLibraryService);
                });
            });
        } else {
            observable = Observable.just(blunoLibraryService);
        }

        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
