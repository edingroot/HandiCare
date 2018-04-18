package tw.cchi.handicare.ui.preferences;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.service.bluno.BlunoLibraryService;
import tw.cchi.handicare.ui.base.BasePresenter;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public class PreferencesPresenter<V extends PreferencesMvpView> extends BasePresenter<V>
    implements PreferencesMvpPresenter<V>, BlunoLibraryService.BleEventListener {

    @Inject MvpApp mvpApp;
    @Inject AppCompatActivity activity;
    @Inject LeDeviceListAdapter mLeDeviceListAdapter;

    @Inject
    public PreferencesPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onPauseProcess() {
        if (blunoLibraryService != null)
            blunoLibraryService.stopScanningLeDevice(true);
        mLeDeviceListAdapter.clear();
    }

    @Override
    public void loadPrefValues() {
        String btDeviceAddr = preferencesHelper.getBTDeviceAddress();
        if (btDeviceAddr != null) {
            getMvpView().setBluetoothAddr(btDeviceAddr);
        }

        // Display current connection state
        connectBlunoLibraryService().subscribe(blunoLibraryService ->
            onConnectionStateChange(blunoLibraryService.getConnectionState())
        );
    }

    @Override
    public void launchScanDeviceDialog() {
        connectBlunoLibraryService().subscribe(blunoLibraryService -> {
            blunoLibraryService.checkAskEnableCapabilities(activity).subscribe(result -> {
                if (!result)
                    return;

                blunoLibraryService.attachEventListener(this);

                switch (blunoLibraryService.getConnectionState()) {
                    case isNull:
                    case isToScan:
                        getMvpView().showScanDeviceDialog(mLeDeviceListAdapter);
                        break;
                }
                blunoLibraryService.onScanningDialogOpen(mLeDeviceListAdapter);
            });
        });
    }

    @Override
    public void onBtDeviceDialogSelect(BluetoothDevice device) {
        if (device != null) {
            connectBlunoLibraryService().subscribe(blunoLibraryService -> {
                if (blunoLibraryService.connect(device)) {
                    String deviceAddress = device.getAddress();
                    preferencesHelper.setBTDeviceAddress(deviceAddress);
                    getMvpView().setBluetoothAddr(deviceAddress);
                    getMvpView().showToast(R.string.bluno_connected);
                } else {
                    getMvpView().showSnackBar(R.string.error_connect_bluno);
                }
            });
        }
    }

    @Override
    public void onBtDeviceDialogCancel() {
        System.out.println("mBluetoothAdapter.stopLeScan");
        connectBlunoLibraryService().subscribe(BlunoLibraryService::onScanningDialogCancel);
    }

    // ----------- BlunoLibraryService.BleEventListener Implementation ----------- //
    @Override
    public void onConnectionStateChange(BlunoLibraryService.DeviceConnectionState deviceConnectionState) {
        switch (deviceConnectionState) {
            case isConnected:
                getMvpView().setScanButtonText("Connected");
                break;
            case isConnecting:
                getMvpView().setScanButtonText("Connecting");
                break;
            case isToScan:
                getMvpView().setScanButtonText("Scan");
                break;
            case isScanning:
                getMvpView().setScanButtonText("Scanning");
                break;
            case isDisconnecting:
                getMvpView().setScanButtonText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String message) {
    }

    // --------------------------------------------------------------------------- //

    @Override
    public void onDetach() {
        if (blunoLibraryService != null) {
            blunoLibraryService.detachEventListener(this);
        }
        super.onDetach();
    }
}
