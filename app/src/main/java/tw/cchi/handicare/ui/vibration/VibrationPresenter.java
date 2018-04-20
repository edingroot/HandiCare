package tw.cchi.handicare.ui.vibration;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.bluno.BlunoHelper;
import tw.cchi.handicare.ui.base.BasePresenter;

public class VibrationPresenter<V extends VibrationMvpView> extends BasePresenter<V> implements VibrationMvpPresenter<V> {
    private static final String TAG = VibrationPresenter.class.getSimpleName();
    private static final long TIMER_TICK_INTERVAL = 20; // ms

    @Inject AppCompatActivity activity;

    private BlunoHelper blunoHelper;
    private Disposable powerTimer;

    private boolean powered = false;
    private int strength = 0;
    private int initialSeconds = 0;
    private float remainingSeconds = 0;
    private Handler mHandler = new Handler();
    private Runnable strengthSender;
    private long lastStrengthSent = 0;

    @Inject
    public VibrationPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        connectBlunoLibraryService().subscribe(blunoLibraryService -> {
            if (!blunoLibraryService.isDeviceConnected()) {
                getMvpView().showToast(R.string.bluno_not_connected);
                activity.finish();
            } else {
                blunoHelper = new BlunoHelper(blunoLibraryService);
                blunoHelper.setMode(BlunoHelper.OpMode.VIBRATION);
            }
        });
    }

    @Override
    public boolean powerOn() {
        if (!checkDeviceConnected())
            return false;

        // Turn on
        int duration = getMvpView().getDurationSetting();
        if (duration == 0){
            getMvpView().showSnackBar("請設定時間長度");
            return false;
        }

        blunoHelper.setVibrationEnabled(true);
        powered = true;
        strength = 1;

        startPowerTimer(duration);
        getMvpView().setPowerAnimationEnabled(true);
        updateViewDeviceControls();

        return true;
    }

    @Override
    public boolean powerOff() {
        if (!checkDeviceConnected())
            return false;

        blunoHelper.setVibrationEnabled(false);
        powered = false;
        strength = 0;

        stopPowerTimer();
        getMvpView().setPowerAnimationEnabled(false);
        updateViewDeviceControls();

        return true;
    }

    @Override
    public void onCustomStrengthChanged(int progressValue) {
        if (powered) {
            progressValue = progressValue == 0 ? 1 : progressValue;
        } else {
            progressValue = 0;
        }
        strength = progressValue;

        int scaledStrength = (int) (strength * 255.0 / 15);
        if (checkDeviceConnected() && powered && blunoHelper.getVibrationStrength() != scaledStrength) {
            if (strengthSender != null) {
                Log.i(TAG, "Removing strengthSender callback @ strength=" + scaledStrength);
                mHandler.removeCallbacks(strengthSender);
            }

            strengthSender = () -> {
                blunoHelper.setVibrationEnabled(true, scaledStrength);
                strengthSender = null;
            };

            long minInterval = Config.BLUNO_CMD_TRANSMIT_INTERVAL + 100;
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - lastStrengthSent < minInterval) {
                mHandler.postAtTime(strengthSender, lastStrengthSent + minInterval);
            } else {
                lastStrengthSent = currentTime;
                strengthSender.run();
            }
        }

        updateViewDeviceControls();
    }

    private void startPowerTimer(int seconds) {
        initialSeconds = seconds;
        remainingSeconds = seconds;
        getMvpView().setProgress(0, initialSeconds);

        powerTimer = Observable.interval(TIMER_TICK_INTERVAL, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
                remainingSeconds -= (float) TIMER_TICK_INTERVAL / 1000;
                if (remainingSeconds <= 0) {
                    onPowerTimeEnd();
                }

                if (isViewAttached())
                    getMvpView().setProgress(initialSeconds - remainingSeconds, initialSeconds);
            });
    }

    private void stopPowerTimer() {
        initialSeconds = 0;
        remainingSeconds = 0;
        if (isViewAttached())
            getMvpView().setProgress(0, initialSeconds);

        // Stop timer
        if (powerTimer != null)
            powerTimer.dispose();
    }

    private void onPowerTimeEnd() {
        powerOff();
    }

    private void updateViewDeviceControls() {
        if (!powered)
            stopPowerTimer();

        getMvpView().updateDeviceControls(powered, strength);
    }

    private boolean checkDeviceConnected() {
        if (blunoHelper == null || !blunoHelper.isDeviceConnected()) {
            getMvpView().showSnackBar(R.string.bluno_not_connected);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        stopPowerTimer();

        if (powered) {
            powered = false;

            if (checkDeviceConnected()) {
                blunoHelper.setVibrationEnabled(false);
            }
        }
    }

}
