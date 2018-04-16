package tw.cchi.handicare.ui.preferences;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.R;
import tw.cchi.handicare.ui.base.BaseActivity;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public class PreferencesActivity extends BaseActivity
    implements PreferencesMvpView {

    @Inject PreferencesMvpPresenter<PreferencesMvpView> presenter;

    private AlertDialog mScanDeviceDialog;

    @BindView(R.id.txtBluetoothAddr) TextView txtBluetoothAddr;
    @BindView(R.id.btnChangeBtDevice) Button btnChangeBtDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);

        presenter.loadPrefValues();
    }

    @Override
    protected void onPause() {
        presenter.onPauseProcess();
        if (mScanDeviceDialog != null)
            mScanDeviceDialog.dismiss();
        super.onPause();
    }

    @OnClick(R.id.btnChangeBtDevice)
    void onChangeBtClick() {
        presenter.launchScanDeviceDialog();
    }

    @Override
    public void setBluetoothAddr(String addr) {
        txtBluetoothAddr.setText(addr);
    }

    @Override
    public void setScanButtonText(String text) {
        btnChangeBtDevice.setText(text);
    }

    @Override
    public void showScanDeviceDialog(LeDeviceListAdapter mLeDeviceListAdapter) {
        if (mScanDeviceDialog == null) {
            // Initializes and show the scan Device Dialog
            mScanDeviceDialog = new AlertDialog.Builder(this).setTitle("BLE Device Scan...")
                .setAdapter(mLeDeviceListAdapter, (dialog, which) -> {
                    presenter.onBtDeviceDialogSelect(mLeDeviceListAdapter.getDevice(which));
                }).setOnCancelListener(dialog -> {
                    presenter.onBtDeviceDialogCancel();
                    dialog.dismiss();
                }).create();
        }

        mScanDeviceDialog.show();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }
}
