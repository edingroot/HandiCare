package tw.cchi.handicare.ui.preferences;

import tw.cchi.handicare.ui.base.MvpView;
import tw.cchi.handicare.ui.preferences.adapter.LeDeviceListAdapter;

public interface PreferencesMvpView extends MvpView {

    void setBluetoothAddr(String addr);

    void setScanButtonText(String text);

    void showScanDeviceDialog(LeDeviceListAdapter mLeDeviceListAdapter);

}
