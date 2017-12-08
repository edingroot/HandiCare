package tw.cchi.whisttherapist;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import tw.cchi.whisttherapist.electro.AcupStorage;
import tw.cchi.whisttherapist.electro.DeviceAcup;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "tw.cchi.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new UsbBroadcastReceiver();
    public DeviceAcup mDevAcup;
    private GlobalVariable globalVar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.globalVar = (GlobalVariable) getApplicationContext();
        this.mDevAcup = new DeviceAcup(
                this.globalVar, this,
                (UsbManager) getSystemService(Context.USB_SERVICE)
        );

        IntentFilter filterAttachedDetached = new IntentFilter();
        filterAttachedDetached.addAction(ACTION_USB_PERMISSION);
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filterAttachedDetached.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mUsbReceiver, filterAttachedDetached);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.globalVar.bPower) {
            this.globalVar.bPower = false;
            this.mDevAcup.commWithUsbDevice();
        }
//        bInit = false;
        unregisterReceiver(this.mUsbReceiver);
    }


    class UsbBroadcastReceiver extends BroadcastReceiver {
        public UsbBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            UsbDevice device;
            String action = intent.getAction();

            if (action.equals(MainActivity.ACTION_USB_PERMISSION)) {
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
                                        MainActivity.this,
                                        0,
                                        new Intent(MainActivity.ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT
                                )
                        );
                    } else if (device != null) {
                        Log.d("1", "ATTACHED-" + device);
                    }
                }

            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");
                    if (device != null) {
                        Log.d("1", "DEATTCHED-" + device);
                    }
                    globalVar.bUsb = false;
                    globalVar.bPower = false;
                    globalVar.nZ = 0;
                    globalVar.nY = 0;
                    globalVar.nX = 0;
                }

            } else if ("android.intent.action.BATTERY_CHANGED".equals(action) && intent.getIntExtra("level", 0) < 30) {
                // TODO
            }
        }
    }

}
