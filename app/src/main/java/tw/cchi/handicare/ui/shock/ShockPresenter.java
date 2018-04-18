package tw.cchi.handicare.ui.shock;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.bluno.BlunoHelper;
import tw.cchi.handicare.device.eshock.DeviceAcup;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.di.PresenterHolder;
import tw.cchi.handicare.receiver.UsbBroadcastReceiver;
import tw.cchi.handicare.ui.base.BasePresenter;

import static tw.cchi.handicare.Constants.ACTION_USB_PERMISSION;

public class ShockPresenter<V extends ShockMvpView> extends BasePresenter<V> implements ShockMvpPresenter<V> {
    private static final long TIMER_TICK_INTERVAL = 20; // ms

    @Inject MvpApp.GlobalVariables globalVar;
    @Inject DeviceAcup mDevAcup;
    @Inject UsbBroadcastReceiver mUsbReceiver;
    @Inject AppCompatActivity activity;
    @Inject @ActivityContext Context context;

    private BlunoHelper blunoHelper;
    private Disposable powerTimer;
    private int initialSeconds = 0;
    private float remainingSeconds = 0;

    @Inject
    public ShockPresenter(CompositeDisposable compositeDisposable, @NonNull PresenterHolder presenterHolder) {
        super(compositeDisposable);
        presenterHolder.setShockMvpPresenter(this);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        if (Config.SHOCK_USB_MODE) {
            // Register broadcast receiver events
            IntentFilter filterAttachedDetached = new IntentFilter();
            filterAttachedDetached.addAction(ACTION_USB_PERMISSION);
            filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
            filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            filterAttachedDetached.addAction("android.intent.action.BATTERY_CHANGED");
            context.registerReceiver(mUsbReceiver, filterAttachedDetached);
        } else {
            connectBlunoLibraryService().subscribe(blunoLibraryService -> {
                if (!blunoLibraryService.isDeviceConnected()) {
                    getMvpView().showSnackBar(R.string.bluno_not_connected);
                    activity.finish();
                } else {
                    blunoHelper = new BlunoHelper(blunoLibraryService);
                    blunoHelper.setMode(BlunoHelper.OpMode.SHOCK);
                }
            });
        }
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

        if (Config.SHOCK_USB_MODE) {
            mDevAcup.powerOn();
        } else {
            blunoHelper.setShockEnabled(true);

            // Update state
            globalVar.bPower = true;
            globalVar.nX = globalVar.nY = 1;
        }

        startPowerTimer(duration);
        updateViewDeviceControls();

        return true;
    }

    @Override
    public boolean powerOff() {
        if (!checkDeviceConnected())
            return false;

        if (Config.SHOCK_USB_MODE) {
            mDevAcup.powerOff();
        } else {
            blunoHelper.setShockEnabled(false);

            globalVar.bPower = false;
            globalVar.nX = globalVar.nY = globalVar.nZ = 0;
        }

        stopPowerTimer();
        updateViewDeviceControls();

        return true;
    }

    @Override
    public void onCustomStrengthChanged(int progressValue) {
        if (globalVar.bPower) {
            progressValue = progressValue == 0 ? 1 : progressValue;
        } else {
            progressValue = 0;
        }
        mDevAcup.setStrength(progressValue);
        updateViewDeviceControls();
    }

    @Override
    public void onCustomFrequencyChanged(int progressValue) {
        if (globalVar.bPower) {
            progressValue = progressValue == 0 ? 1 : progressValue;
        } else {
            progressValue = 0;
        }
        mDevAcup.setFrequency(progressValue);
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

                getMvpView().setProgress(initialSeconds - remainingSeconds, initialSeconds);
            });
    }

    private void stopPowerTimer() {
        initialSeconds = 0;
        remainingSeconds = 0;
        getMvpView().setProgress(0, initialSeconds);

        // Stop timer
        if (powerTimer != null)
            powerTimer.dispose();
    }

    private void onPowerTimeEnd() {
        powerOff();
    }

    public void updateViewDeviceControls() {
        boolean powerOn = globalVar.bPower;

        if (!powerOn)
            stopPowerTimer();

        getMvpView().updateDeviceControls(powerOn, globalVar.nX, globalVar.nY);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (globalVar.bPower) {
            globalVar.bPower = false;

            if (checkDeviceConnected()) {
                if (Config.SHOCK_USB_MODE) {
                    mDevAcup.commWithUsbDevice();
                } else {
                    blunoHelper.setShockEnabled(false);
                    blunoHelper.setMode(BlunoHelper.OpMode.STANDBY);
                }
            }
        }

        if (Config.SHOCK_USB_MODE)
            context.unregisterReceiver(this.mUsbReceiver);
    }

    private boolean checkDeviceConnected() {
        if (Config.SHOCK_USB_MODE) {
            if (!mDevAcup.connect()) {
                getMvpView().showSnackBar(R.string.usb_not_found);
                return false;
            }
        } else {
            if (blunoHelper == null || !blunoHelper.isDeviceConnected()) {
                getMvpView().showSnackBar(R.string.bluno_not_connected);
                return false;
            }
        }

        return true;
    }

}
