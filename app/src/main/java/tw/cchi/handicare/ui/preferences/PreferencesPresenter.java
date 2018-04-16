package tw.cchi.handicare.ui.preferences;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.BlunoLibraryService;
import tw.cchi.handicare.helper.pref.PreferencesHelper;
import tw.cchi.handicare.ui.base.BasePresenter;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public class PreferencesPresenter<V extends PreferencesMvpView> extends BasePresenter<V>
    implements PreferencesMvpPresenter<V>, BlunoLibraryService.BleEventListener {

    @Inject MvpApp mvpApp;
    @Inject AppCompatActivity activity;
    @Inject PreferencesHelper preferencesHelper;
    @Inject LeDeviceListAdapter mLeDeviceListAdapter;

    private BlunoLibraryService blunoLibraryService;
    private boolean blunoLibOnResumeCalled = false;

    @Inject
    public PreferencesPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onResumeProcess() {
        if (blunoLibraryService != null) {
            blunoLibraryService.onResumeProcess(activity);
            blunoLibOnResumeCalled = true;
        } else {
            blunoLibOnResumeCalled = false;
        }
    }

    @Override
    public void onPauseProcess() {
        if (blunoLibraryService != null)
            blunoLibraryService.onPauseProcess();
        mLeDeviceListAdapter.clear();
    }

    @Override
    public void loadPrefValues() {
        String btDeviceAddr = preferencesHelper.getBTDeviceAddress();
        if (btDeviceAddr != null) {
            getMvpView().setBluetoothAddr(btDeviceAddr);
        }
    }

    @Override
    public void launchScanDeviceDialog() {
        getBlunoLibraryServiceObservable().subscribe(blunoLibraryService -> {
            blunoLibraryService.attachEventListener(this);

            if (!blunoLibOnResumeCalled)
                blunoLibraryService.onResumeProcess(activity);

            // Check bluetooth enabled
            if (!blunoLibraryService.checkAskEnableBluetooth(activity)) {
                getMvpView().showToast(R.string.error_bluetooth_not_enabled);
                return;
            }

            // Check location enabled
            blunoLibraryService.checkAskEnableLocation(activity).subscribe(result -> {
                if (!result) {
                    getMvpView().showToast(R.string.error_location_not_enabled);
                    return;
                }

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
            getBlunoLibraryServiceObservable().subscribe(blunoLibraryService -> {
                if (blunoLibraryService.connect(device)) {
                    String deviceAddress = device.getAddress();
                    preferencesHelper.setBTDeviceAddress(deviceAddress);
                    getMvpView().setBluetoothAddr(deviceAddress);
                    getMvpView().showSnackBar(R.string.bluno_connected);
                } else {
                    getMvpView().showSnackBar(R.string.error_connect_bluno);
                }
            });
        }
    }

    @Override
    public void onBtDeviceDialogCancel() {
        System.out.println("mBluetoothAdapter.stopLeScan");
        getBlunoLibraryServiceObservable().subscribe(BlunoLibraryService::onScanningDialogCancel);
    }

    private Observable<BlunoLibraryService> getBlunoLibraryServiceObservable() {
        Observable<BlunoLibraryService> observable;

        if (blunoLibraryService == null) {
            observable = Observable.create(emitter -> {
                mvpApp.getBlunoLibraryService(service -> {
                    blunoLibraryService = (BlunoLibraryService) service;
                    emitter.onNext(blunoLibraryService);
                });
            });
        } else {
            observable = Observable.just(blunoLibraryService);
        }

        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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
    public void onSerialReceived(String theString) {
    }

    // --------------------------------------------------------------------------- //


    @Override
    public void onDetach() {
        if (blunoLibraryService != null) {
            blunoLibraryService.detachEventListener();
            
        }
        super.onDetach();
    }
}
