package tw.cchi.handicare.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import javax.inject.Inject;

import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.di.PresenterHolder;
import tw.cchi.handicare.device.eshock.AcupStorage;
import tw.cchi.handicare.device.eshock.DeviceAcup;
import tw.cchi.handicare.ui.shock.ShockMvpPresenter;

import static tw.cchi.handicare.Constants.ACTION_USB_PERMISSION;

public class UsbBroadcastReceiver extends BroadcastReceiver {

    @Inject MvpApp.GlobalVariables globalVar;
    @Inject DeviceAcup mDevAcup;

    @Inject PresenterHolder presenterHolder;

    @Inject
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

                ShockMvpPresenter shockMvpPresenter = presenterHolder.getShockMvpPresenter();
                if (shockMvpPresenter != null && shockMvpPresenter.isViewAttached()) {
                    shockMvpPresenter.updateViewDeviceControls();
                }
            }

        } else if ("android.intent.action.BATTERY_CHANGED".equals(action) && intent.getIntExtra("level", 0) < 30) {
            // TODO
        }
    }
}
