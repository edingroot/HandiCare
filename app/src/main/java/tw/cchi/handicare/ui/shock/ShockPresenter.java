package tw.cchi.handicare.ui.shock;

import android.content.Context;
import android.content.IntentFilter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.Constants;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.di.PresenterHolder;
import tw.cchi.handicare.eshock.DeviceAcup;
import tw.cchi.handicare.receiver.UsbBroadcastReceiver;
import tw.cchi.handicare.ui.base.BasePresenter;

import static tw.cchi.handicare.Constants.ACTION_USB_PERMISSION;

public class ShockPresenter<V extends ShockMvpView> extends BasePresenter<V> implements ShockMvpPresenter<V> {
    private static final long TIMER_TICK_INTERVAL = 20; // ms

    @Inject MvpApp.GlobalVariables globalVar;
    @Inject DeviceAcup mDevAcup;
    @Inject UsbBroadcastReceiver mUsbReceiver;
    @Inject @ActivityContext Context context;

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

        // Register broadcast receiver events
        IntentFilter filterAttachedDetached = new IntentFilter();
        filterAttachedDetached.addAction(ACTION_USB_PERMISSION);
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filterAttachedDetached.addAction("android.intent.action.BATTERY_CHANGED");
        context.registerReceiver(mUsbReceiver, filterAttachedDetached);
    }

    @Override
    public void onModeSelectionChanged(int selectedIndex) {
        if (mDevAcup.isConnected()) {
            mDevAcup.setStrength(Constants.SHOCK_MODE_STRENGTHS[selectedIndex]);
            mDevAcup.setFrequency(Constants.SHOCK_MODE_FREQS[selectedIndex]);
            updateViewDeviceControls();
        }
    }

    @Override
    public void powerOn() {
        if (!mDevAcup.connect())
            getMvpView().showSnackBar(R.string.usb_not_found);

        // Turn on
        int duration = getMvpView().getDurationSetting();
        if (duration == 0) return;

        startPowerTimer(duration);
        if (getMvpView().getModeSelection() == -1)
            getMvpView().setModeSelection(0);
        mDevAcup.powerOn();

        updateViewDeviceControls();
    }

    @Override
    public void powerOff() {
        if (!mDevAcup.connect())
            getMvpView().showSnackBar(R.string.usb_not_found);

        mDevAcup.powerOff();
        stopPowerTimer();

        updateViewDeviceControls();
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
        mDevAcup.powerOff();
        updateViewDeviceControls();
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
            mDevAcup.commWithUsbDevice();
        }
        context.unregisterReceiver(this.mUsbReceiver);
    }

}
