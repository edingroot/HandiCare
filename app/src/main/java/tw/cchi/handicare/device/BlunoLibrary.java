package tw.cchi.handicare.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import tw.cchi.handicare.service.ble.BLEService;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public class BlunoLibrary {
    private final static String TAG = BlunoLibrary.class.getSimpleName();

    public enum DeviceConnectionState {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}
    public static final int REQUEST_ENABLE_BT = 1;

    private static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    private static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    private static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";

    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    private DeviceConnectionState mDeviceConnectionState = DeviceConnectionState.isNull;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    private BLEService mBLEService;
    private Context mainContext;
    private boolean connected = false;

    private BleEventListener eventListener;

    private int mBaudrate = 115200;
    private String mPassword = "AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;

    private String mDeviceName;
    private String mDeviceAddress;
    private Handler mHandler = new Handler();

    public BlunoLibrary(Context mainContext, BleEventListener eventListener) {
        this.mainContext = mainContext;
        this.eventListener = eventListener;
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void serialSend(String theString) {
        if (mDeviceConnectionState == DeviceConnectionState.isConnected) {
            mSCharacteristic.setValue(theString);
            mBLEService.writeCharacteristic(mSCharacteristic);
        }
    }

    private void serialBegin(int baud) {
        mBaudrate = baud;
        mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";
    }

    private void startBluetoothService() {
        Intent gattServiceIntent = new Intent(mainContext, BLEService.class);
        mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public boolean initiate() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!mainContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)
            return false;

        serialBegin(115200);
        startBluetoothService();

        return true;
    }

    public void connect(BluetoothDevice device) {
        stopScanningLeDevice();

        if (device.getName() == null || device.getAddress() == null) {
            mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
        } else {
            System.out.println("onListItemClick " + device.getName());
            System.out.println("Device Name:" + device.getName() + "   " + "Device Name:" + device.getAddress());

            mDeviceName = device.getName();
            mDeviceAddress = device.getAddress();

            if (mBLEService.connect(mDeviceAddress)) {
                Log.d(TAG, "Connect request success");
                mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isConnecting;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
            } else {
                Log.d(TAG, "Connect request fail");
                mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isToScan;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
            }
        }
    }

    public void disconnect() {
        if (mBLEService != null)
            mBLEService.disconnect();
    }

    public boolean isConnected() {
        return connected;
    }

    public DeviceConnectionState getConnectionState() {
        return mDeviceConnectionState;
    }

    public void scanLeDevice(LeDeviceListAdapter mLeDeviceListAdapter) {
        // Device scan callback
        BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanRecord) -> {
            ((Activity) mainContext).runOnUiThread(() -> {
                // System.out.println("mLeScanCallback onLeScan run ");
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            });
        };

        // Stop scanning after a pre-defined scan period
        System.out.println("mBluetoothAdapter.startLeScan");

        if (mLeDeviceListAdapter != null) {
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        if (!mScanning) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    public void stopScanningLeDevice() {
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan((bluetoothDevice, i, bytes) -> {});
        }
    }

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        mModelNumberCharacteristic = null;
        mSerialPortCharacteristic = null;
        mCommandCharacteristic = null;
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid=" + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.equals(ModelNumberStringUUID)) {
                    mModelNumberCharacteristic = gattCharacteristic;
                    System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristic.getUuid().toString());
                } else if (uuid.equals(SerialPortUUID)) {
                    mSerialPortCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                } else if (uuid.equals(CommandUUID)) {
                    mCommandCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }

        if (mModelNumberCharacteristic == null || mSerialPortCharacteristic == null || mCommandCharacteristic == null) {
            Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
            mDeviceConnectionState = DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
        } else {
            mSCharacteristic = mModelNumberCharacteristic;
            mBLEService.setCharacteristicNotification(mSCharacteristic, true);
            mBLEService.readCharacteristic(mSCharacteristic);
        }
    }


    // -------------------- For Device Scanning Dialog --------------------

    public void onScanningDialogCancel() {
        mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isToScan;
        eventListener.onConnectionStateChange(mDeviceConnectionState);
        stopScanningLeDevice();
    }

    public void onScanningDialogOpen(LeDeviceListAdapter mLeDeviceListAdapter) {
        switch (getConnectionState()) {
            case isNull:
                mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isScanning;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                scanLeDevice(mLeDeviceListAdapter);
                break;

            case isToScan:
                mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isScanning;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                scanLeDevice(mLeDeviceListAdapter);
                break;

            case isConnected:
                disconnect();
                mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
                // mBLEService.close();
                mDeviceConnectionState = BlunoLibrary.DeviceConnectionState.isDisconnecting;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                break;
        }
    }

    // -------------------- Activity Events --------------------

    public void onResumeProcess() {
        System.out.println("BlUNOActivity onResume");

        // Ensures Bluetooth is enabled on the device.
        // If Bluetooth is not currently enabled, fire an intent to display a dialog
        // asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void onPauseProcess() {
        System.out.println("BLUNOActivity onPause");

        stopScanningLeDevice();

        mainContext.unregisterReceiver(mGattUpdateReceiver);
        mDeviceConnectionState = DeviceConnectionState.isToScan;
        eventListener.onConnectionStateChange(mDeviceConnectionState);

        if (mBLEService != null) {
            mBLEService.disconnect();
            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
//			mBLEService.close();
        }
        mSCharacteristic = null;

    }

    public void onStopProcess() {
        System.out.println("MiUnoActivity onStop");
        if (mBLEService != null) {
//			mBLEService.disconnect();
//          mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
            mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
            mBLEService.close();
        }
        mSCharacteristic = null;
    }

    public void onDestroyProcess() {
        mainContext.unbindService(mServiceConnection);
        mBLEService = null;
    }


    // Code to manage Service lifecycle
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBLEService = ((BLEService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                ((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBLEService = null;
        }
    };

    private Runnable mConnectingOverTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDeviceConnectionState == DeviceConnectionState.isConnecting)
                mDeviceConnectionState = DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
            mBLEService.close();
        }
    };

    private Runnable mDisonnectingOverTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDeviceConnectionState == DeviceConnectionState.isDisconnecting)
                mDeviceConnectionState = DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
            mBLEService.close();
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action=" + action);

            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                mHandler.removeCallbacks(mConnectingOverTimeRunnable);

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                mDeviceConnectionState = DeviceConnectionState.isToScan;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
                mBLEService.close();

            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                for (BluetoothGattService gattService : mBLEService.getSupportedGattServices()) {
                    System.out.println("ACTION_GATT_SERVICES_DISCOVERED  " +
                        gattService.getUuid().toString());
                }
                getGattServices(mBLEService.getSupportedGattServices());

            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (mSCharacteristic == mModelNumberCharacteristic) {
                    if (intent.getStringExtra(BLEService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
                        mBLEService.setCharacteristicNotification(mSCharacteristic, false);
                        mSCharacteristic = mCommandCharacteristic;
                        mSCharacteristic.setValue(mPassword);
                        mBLEService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic.setValue(mBaudrateBuffer);
                        mBLEService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic = mSerialPortCharacteristic;
                        mBLEService.setCharacteristicNotification(mSCharacteristic, true);
                        mDeviceConnectionState = DeviceConnectionState.isConnected;
                        eventListener.onConnectionStateChange(mDeviceConnectionState);
                    } else {
                        Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
                        mDeviceConnectionState = DeviceConnectionState.isToScan;
                        eventListener.onConnectionStateChange(mDeviceConnectionState);
                    }

                } else if (mSCharacteristic == mSerialPortCharacteristic) {
                    eventListener.onSerialReceived(intent.getStringExtra(BLEService.EXTRA_DATA));
                }

                System.out.println("displayData " + intent.getStringExtra(BLEService.EXTRA_DATA));
//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BLEService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());
            }
        }
    };
    
    public interface BleEventListener {
        void onConnectionStateChange(DeviceConnectionState deviceConnectionState);
        
        void onSerialReceived(String theString);
    }
    
}
