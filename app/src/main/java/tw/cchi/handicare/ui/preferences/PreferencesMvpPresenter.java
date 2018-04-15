
package tw.cchi.handicare.ui.preferences;

import android.bluetooth.BluetoothDevice;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface PreferencesMvpPresenter<V extends PreferencesMvpView> extends MvpPresenter<V> {

    void onResumeProcess();

    void onPauseProcess();

    void loadPrefValues();

    void launchScanDeviceDialog();

    void onBtDeviceDialogSelect(BluetoothDevice device);

    void onBtDeviceDialogCancel();

}
