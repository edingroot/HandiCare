package tw.cchi.whisttherapist.ui.shock;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.whisttherapist.Constants;
import tw.cchi.whisttherapist.MvpApp;
import tw.cchi.whisttherapist.R;
import tw.cchi.whisttherapist.base.BasePresenter;
import tw.cchi.whisttherapist.eshock.AcupStorage;
import tw.cchi.whisttherapist.eshock.DeviceAcup;

public class ShockPresenter<V extends ShockMvpView> extends BasePresenter<V> implements ShockMvpPresenter<V> {
    private static final String ACTION_USB_PERMISSION = "tw.cchi.USB_PERMISSION";
    private static final long TIMER_TICK_INTERVAL = 20; // ms

    @Inject MvpApp.GlobalVariables globalVar;
    @Inject DeviceAcup mDevAcup;
    @Inject Context context;

    private BroadcastReceiver mUsbReceiver = new UsbBroadcastReceiver();
    private Handler taskHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private float remainingSeconds = 0;
    private int initialSeconds = 0;
    private boolean timerRunning = false;

    @Inject
    public ShockPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
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

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                remainingSeconds -= (float) TIMER_TICK_INTERVAL / 1000;
                if (remainingSeconds <= 0) {
                    timerRunning = false;
                    onPowerTimeEnd();
                }

                if (timerRunning) {
                    getMvpView().setProgress(initialSeconds - remainingSeconds, initialSeconds);
                    taskHandler.postDelayed(timerRunnable, TIMER_TICK_INTERVAL);
                }
            }
        };
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
            getMvpView().showMessage(R.string.usb_not_found);

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
            getMvpView().showMessage(R.string.usb_not_found);
        
        mDevAcup.powerOff();
        stopPowerTimer();
        
        updateViewDeviceControls();
    }

    @Override
    public void onCustomStrengthChanged(int progressValue) {
        if (globalVar.bPower) {
            progressValue = progressValue == 0 ? 1 : progressValue;
            mDevAcup.setStrength(progressValue);
        } else {
            progressValue = 0;
        }
        updateViewDeviceControls();
    }

    @Override
    public void onCustomFrequencyChanged(int progressValue) {
        if (globalVar.bPower) {
            progressValue = progressValue == 0 ? 1 : progressValue;
            mDevAcup.setStrength(progressValue);
        } else {
            progressValue = 0;
        }
        updateViewDeviceControls();
    }

    private void startPowerTimer(int seconds) {
        timerRunning = true;
        initialSeconds = seconds;
        remainingSeconds = seconds;
        getMvpView().setProgress(0, initialSeconds);
        taskHandler.postDelayed(timerRunnable, TIMER_TICK_INTERVAL);
    }

    private void stopPowerTimer() {
        timerRunning = false;
        initialSeconds = 0;
        remainingSeconds = 0;
        getMvpView().setProgress(0, initialSeconds);
        taskHandler.removeCallbacks(timerRunnable);
    }

    private void onPowerTimeEnd() {
        mDevAcup.powerOff();
        updateViewDeviceControls();
    }
    
    private void updateViewDeviceControls() {
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


    class UsbBroadcastReceiver extends BroadcastReceiver {
        public UsbBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            UsbDevice device;
            String action = intent.getAction();
            if (action == null) return;

            if (action.equals(ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false)) {
                        if (device != null) {
                            Log.d("1", "PERMISSION-" + device);
                        }

                        try {
                            Thread.sleep(1000);

                            if (mDevAcup.getTheTargetDevice() != null) {
                                if (mDevAcup.mUsbDevice.getInterfaceCount() > 0) {
                                    mDevAcup.mUsbInterface = mDevAcup.mUsbDevice.getInterface(0);
                                    mDevAcup.mEndpointRead = mDevAcup.mUsbInterface.getEndpoint(0);
                                    mDevAcup.mEndpointWrite = mDevAcup.mUsbInterface.getEndpoint(1);
                                }
                                globalVar.bPower = false;
                                globalVar.nX = 0;
                                globalVar.nY = 0;
                                globalVar.nZ = 0;
                                mDevAcup.commWithUsbDevice();

                                if (AcupStorage.nDeviceType != 0) {
                                    mDevAcup.commWithUsbDevice(11);
                                    mDevAcup.commWithUsbDevice(12);
                                }
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Thread was interrupted");
                            return;
                        }
                    }
                }

            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");

                    if (!intent.getBooleanExtra("permission", false)) {
                        mDevAcup.mUsbManager.requestPermission(
                                device,
                                PendingIntent.getBroadcast(
                                        context,
                                        0,
                                        new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT
                                )
                        );
                    } else if (device != null) {
                        Log.d("1", "ATTACHED-" + device);
                        mDevAcup.connect();
                    }
                }

            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");
                    if (device != null) {
                        Log.d("1", "DEATTCHED-" + device);
                    }
                    mDevAcup.disconnect();
                    updateViewDeviceControls();
                }

            } else if ("android.intent.action.BATTERY_CHANGED".equals(action) && intent.getIntExtra("level", 0) < 30) {
                // TODO
            }
        }
    }
}
