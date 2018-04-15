package tw.cchi.handicare.ui.preferences;

import android.os.Bundle;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.cchi.handicare.R;
import tw.cchi.handicare.helper.pref.PreferencesHelper;
import tw.cchi.handicare.ui.base.BaseActivity;

public class PreferencesActivity extends BaseActivity implements PreferencesMvpView {

    @Inject PreferencesMvpPresenter<PreferencesMvpView> presenter;
    @Inject PreferencesHelper preferencesHelper;

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

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }
}
