package tw.cchi.handicare.service.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import static tw.cchi.handicare.service.ble.BLEService.ACTION_DATA_AVAILABLE;
import static tw.cchi.handicare.service.ble.BLEService.ACTION_GATT_CONNECTED;
import static tw.cchi.handicare.service.ble.BLEService.ACTION_GATT_DISCONNECTED;
import static tw.cchi.handicare.service.ble.BLEService.ACTION_GATT_SERVICES_DISCOVERED;
import static tw.cchi.handicare.service.ble.BLEService.EXTRA_DATA;

public class AppBLEGattCallback extends android.bluetooth.BluetoothGattCallback {
    private final static String TAG = AppBLEGattCallback.class.getSimpleName();

    // To tell the onCharacteristicWrite call back function that this is a new characteristic,
    // not the Write Characteristic to the device successfully.
    static final int WRITE_NEW_CHARACTERISTIC = -1;

    // Limited length of the characteristic
    private static final int MAX_CHARACTERISTIC_LENGTH = 17;

    private BLEService bleService;
    // Characteristic is writing or not
    private boolean mIsWritingCharacteristic = false;

    AppBLEGattCallback(BLEService bleService) {
        this.bleService = bleService;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String intentAction;
        System.out.println("AppBLEGattCallback----onConnectionStateChange" + newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            intentAction = ACTION_GATT_CONNECTED;
            bleService.connectionState = BLEService.BLEConnectionState.CONNECTED;
            broadcastUpdate(intentAction);

            Log.i(TAG, "Connected to GATT server.");

            // Attempts to discover services after successful connection.
            if (bleService.mBluetoothGatt.discoverServices()) {
                Log.i(TAG, "Attempting to start service discovery:");

            } else {
                Log.i(TAG, "Attempting to start service discovery:not success");

            }


        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            intentAction = ACTION_GATT_DISCONNECTED;
            bleService.connectionState = BLEService.BLEConnectionState.DISCONNECTED;
            Log.i(TAG, "Disconnected from GATT server.");
            broadcastUpdate(intentAction);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        System.out.println("onServicesDiscovered " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // This block should be synchronized to prevent the function overloading
        synchronized (this) {
            // CharacteristicWrite success
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("onCharacteristicWrite success:" + new String(characteristic.getValue()));
                if (bleService.mCharacteristicRingBuffer.isEmpty()) {
                    mIsWritingCharacteristic = false;
                } else {
                    BLEService.BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = bleService.mCharacteristicRingBuffer.next();
                    if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH) {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));

                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }


                        if (bleService.mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                    } else {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }

                        if (bleService.mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = "";

                        // System.out.print("before pop:");
                        // System.out.println(mCharacteristicRingBuffer.size());
                        bleService.mCharacteristicRingBuffer.pop();
                        // System.out.print("after pop:");
                        // System.out.println(mCharacteristicRingBuffer.size());
                    }
                }
            } else if (status == WRITE_NEW_CHARACTERISTIC) {
                if (!bleService.mCharacteristicRingBuffer.isEmpty() && !mIsWritingCharacteristic) {
                    BLEService.BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = bleService.mCharacteristicRingBuffer.next();

                    if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH) {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }

                        if (bleService.mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                    } else {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }

                        if (bleService.mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                            /*
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[0]);
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[1]);
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[2]);
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[3]);
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[4]);
                            System.out.println(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[5]);
                            */

                        } else {
                            System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }

                        bluetoothGattCharacteristicHelper.mCharacteristicValue = "";

                        // System.out.print("before pop:");
                        // System.out.println(mCharacteristicRingBuffer.size());
                        bleService.mCharacteristicRingBuffer.pop();
                        // System.out.print("after pop:");
                        // System.out.println(mCharacteristicRingBuffer.size());
                    }
                }

                mIsWritingCharacteristic = true;

                // Clear the buffer to prevent the lock of the mIsWritingCharacteristic
                if (bleService.mCharacteristicRingBuffer.isFull()) {
                    bleService.mCharacteristicRingBuffer.clear();
                    mIsWritingCharacteristic = false;
                }

            } else {
                // CharacteristicWrite fail
                bleService.mCharacteristicRingBuffer.clear();
                System.out.println("onCharacteristicWrite fail:" + new String(characteristic.getValue()));
                System.out.println(status);
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            System.out.println("onCharacteristicRead  " + characteristic.getUuid().toString());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor characteristic,
                                  int status) {
        System.out.println("onDescriptorWrite  " + characteristic.getUuid().toString() + " " + status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        // System.out.println("onCharacteristicChanged  " + new String(characteristic.getValue()));
        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        bleService.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // System.out.println("BLEService broadcastUpdate");

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
            bleService.sendBroadcast(intent);
        }
    }
}
