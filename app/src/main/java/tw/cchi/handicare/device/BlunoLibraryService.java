package tw.cchi.handicare.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.di.ApplicationContext;
import tw.cchi.handicare.di.component.DaggerServiceComponent;
import tw.cchi.handicare.di.component.ServiceComponent;
import tw.cchi.handicare.service.ble.BLEService;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public class BlunoLibraryService extends Service {
    private final static String TAG = BlunoLibraryService.class.getSimpleName();

    public enum DeviceConnectionState {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}
    public static final int REQUEST_ENABLE_BT = 1;

    private static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    private static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    private static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";

    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    private DeviceConnectionState mDeviceConnectionState = DeviceConnectionState.isNull;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    @Inject @ApplicationContext Context mainContext;
    private IBinder mBinder;
    private BLEService mBLEService;
    private boolean connected = false;

    private BleEventListener eventListener;
    private BleEventListener serviceEventListener;

    private int mBaudrate = 115200;
    private String mPassword = "AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback mLeScanCallback;
    private boolean mScanning = false;

    private String mDeviceName;
    private String mDeviceAddress;
    private Handler mHandler = new Handler();

    public BlunoLibraryService() {
        this.mBinder = new ServiceBinder();
        this.eventListener = serviceEventListener = new BleEventListener() {
            @Override
            public void onConnectionStateChange(DeviceConnectionState deviceConnectionState) {
                Log.i(TAG, "[Service] onConnectionStateChange: " + deviceConnectionState);
            }

            @Override
            public void onSerialReceived(String theString) {
                Log.i(TAG, "[Service] onSerialReceived: " + theString);
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceComponent component = DaggerServiceComponent.builder()
            .applicationComponent(((MvpApp) getApplication()).getComponent())
            .build();
        component.inject(this);

        if (!initiate()) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void attachEventListener(BleEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void detachEventListener() {
        this.eventListener = serviceEventListener;
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

    public boolean connect(BluetoothDevice device) {
        stopScanningLeDevice();

        if (device.getName() == null || device.getAddress() == null) {
            mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
            return false;
        }

        System.out.println("onListItemClick " + device.getName());
        System.out.println("Device Name:" + device.getName() + "   " + "Device Name:" + device.getAddress());

        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        if (mBLEService.connect(mDeviceAddress)) {
            Log.d(TAG, "Connect request success");
            mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isConnecting;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
            mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
            return true;
        } else {
            Log.d(TAG, "Connect request fail");
            mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isToScan;
            eventListener.onConnectionStateChange(mDeviceConnectionState);
            return false;
        }
    }

    public void disconnect() {
        if (mBLEService != null)
            mBLEService.disconnect();
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public Observable<Boolean> checkAskEnableLocation(Activity activity) {
        return Observable.<Boolean>create(emitter -> {

            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> emitter.onNext(false))
                .build();
            googleApiClient.connect();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(
                    LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(5 * 1000)
                        .setFastestInterval(2 * 1000)
                );
            builder.setAlwaysShow(true); // this is the key ingredient

            PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            pendingResult.setResultCallback(result -> {
                final Status status = result.getStatus();
                // final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        emitter.onNext(true);
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied,
                        // but could be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult()
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        emitter.onNext(false);
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        emitter.onNext(false);
                        break;
                }
            });

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public DeviceConnectionState getConnectionState() {
        return mDeviceConnectionState;
    }

    /**
     * Ensures Bluetooth is enabled on the device.
     * If Bluetooth is not currently enabled, fire an intent to display a dialog
     * asking the user to grant permission to enable it.
     */
    public boolean checkAskEnableBluetooth(Activity activity) {
        // Ensures Bluetooth is enabled on the device.
        // If Bluetooth is not currently enabled, fire an intent to display a dialog
        // asking the user to grant permission to enable it.
        if (!isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        } else {
            return true;
        }
    }

    public void scanLeDevice(LeDeviceListAdapter mLeDeviceListAdapter) {
        // Device scan callback
        mLeScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                // System.out.println("mLeScanCallback onLeScan run ");
                new Handler(Looper.getMainLooper()).post(() -> {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        // Stop scanning after a pre-defined scan period
        System.out.println("mBluetoothAdapter.startLeScan");

        if (mLeDeviceListAdapter != null) {
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        if (!mScanning) {
            mScanning = true;
            // mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
        }
    }

    public void stopScanningLeDevice() {
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
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
        mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isToScan;
        eventListener.onConnectionStateChange(mDeviceConnectionState);
        stopScanningLeDevice();
    }

    public void onScanningDialogOpen(LeDeviceListAdapter mLeDeviceListAdapter) {
        switch (getConnectionState()) {
            case isNull:
                mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isScanning;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                scanLeDevice(mLeDeviceListAdapter);
                break;

            case isToScan:
                mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isScanning;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                scanLeDevice(mLeDeviceListAdapter);
                break;

            case isConnected:
                disconnect();
                mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
                // mBLEService.close();
                mDeviceConnectionState = BlunoLibraryService.DeviceConnectionState.isDisconnecting;
                eventListener.onConnectionStateChange(mDeviceConnectionState);
                break;
        }
    }

    // -------------------- Activity Events --------------------

    public void onResumeProcess(Activity activity) {
        System.out.println("BlUNOActivity onResume");
        checkAskEnableBluetooth(activity);
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

    // ------------------------------------------------------------

    @Override
    public void onDestroy() {
        stopScanningLeDevice();
        onStopProcess();
        onDestroyProcess();
        disconnect();
        super.onDestroy();
    }

    // -------------------- Code to manage Service lifecycle --------------------
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBLEService = ((BLEService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e(TAG, getString(R.string.error_bluetooth_unable_init));
                Toast.makeText(mainContext, getString(R.string.error_bluetooth_unable_init), Toast.LENGTH_SHORT).show();
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

    public class ServiceBinder extends Binder {
        public BlunoLibraryService getService() {
            return BlunoLibraryService.this;
        }
    }
    
}
