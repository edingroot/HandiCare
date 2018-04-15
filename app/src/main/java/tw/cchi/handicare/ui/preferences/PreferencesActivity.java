package tw.cchi.handicare.ui.preferences;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.BlunoLibrary;
import tw.cchi.handicare.helper.pref.PreferencesHelper;
import tw.cchi.handicare.ui.base.BaseActivity;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

import static tw.cchi.handicare.device.BlunoLibrary.DeviceConnectionState.isConnected;
import static tw.cchi.handicare.device.BlunoLibrary.DeviceConnectionState.isConnecting;
import static tw.cchi.handicare.device.BlunoLibrary.DeviceConnectionState.isDisconnecting;
import static tw.cchi.handicare.device.BlunoLibrary.DeviceConnectionState.isScanning;
import static tw.cchi.handicare.device.BlunoLibrary.DeviceConnectionState.isToScan;

public class PreferencesActivity extends BaseActivity
    implements PreferencesMvpView, BlunoLibrary.BleEventListener {

    @Inject PreferencesMvpPresenter<PreferencesMvpView> presenter;
    @Inject PreferencesHelper preferencesHelper;

    @Inject LeDeviceListAdapter mLeDeviceListAdapter = null;
    private BlunoLibrary blunoLibrary;
    private AlertDialog mScanDeviceDialog;

    @BindView(R.id.txtBluetoothAddr) TextView txtBluetoothAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);

        fillPrefValues();
    }

    private void fillPrefValues() {
        String btDeviceAddr = preferencesHelper.getBTDeviceAddress();
        if (btDeviceAddr != null) {
            txtBluetoothAddr.setText(btDeviceAddr);
        }
    }

    @OnClick(R.id.btnChangeBtDevice)
    void onChangeBtClick() {

    }

    @Override
    public void onConnectionStateChange(BlunoLibrary.DeviceConnectionState deviceConnectionState) {
//        switch (deviceConnectionState) {
//            case isConnected:
//                buttonScan.setText("Connected");
//                break;
//            case isConnecting:
//                buttonScan.setText("Connecting");
//                break;
//            case isToScan:
//                buttonScan.setText("Scan");
//                break;
//            case isScanning:
//                buttonScan.setText("Scanning");
//                break;
//            case isDisconnecting:
//                buttonScan.setText("isDisconnecting");
//                break;
//            default:
//                break;
//        }
    }

    @Override
    public void onSerialReceived(String theString) {

    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }

}
