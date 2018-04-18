package tw.cchi.handicare.ui.vibration;

import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jakewharton.rxbinding2.widget.RxSeekBar;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.R;
import tw.cchi.handicare.ui.base.BaseActivity;

public class VibrationActivity extends BaseActivity implements VibrationMvpView {

    @Inject VibrationMvpPresenter<VibrationMvpView> presenter;

    @BindView(R.id.minutesPicker) NumberPicker minutesPicker;
    @BindView(R.id.secondsPicker) NumberPicker secondsPicker;

    @BindView(R.id.togglePower) ToggleButton togglePower;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;

    @BindView(R.id.seekStrength) SeekBar seekStrength;
    @BindView(R.id.txtStrengthVal) TextView txtStrengthVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);

        initComponents();
    }

    private void initComponents() {
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(10);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        updateDeviceControls(false, 0);

        RxSeekBar.userChanges(seekStrength).subscribe(presenter::onCustomStrengthChanged);
    }

    @OnClick(R.id.togglePower)
    public void togglePower(ToggleButton toggleButton) {
        if (toggleButton.isChecked()) {
            if (!presenter.powerOn())
                toggleButton.setChecked(false);
        } else {
            if (!presenter.powerOff())
                toggleButton.setChecked(true);
        }
    }

    @Override
    public void updateDeviceControls(boolean isPowerOn, int strength) {
        if (isPowerOn) {
            if (!togglePower.isChecked())
                togglePower.setChecked(true);
        } else {
            if (togglePower.isChecked())
                togglePower.setChecked(false);
            strength = 0;
        }
        seekStrength.setProgress(strength);
        txtStrengthVal.setText(String.valueOf(strength));
    }

    @Override
    public void setProgress(float value, float max) {
        circleProgressView.setMaxValue(max);
        circleProgressView.setValue(value);
    }

    @Override
    public int getDurationSetting() {
        return minutesPicker.getValue() * 60 + secondsPicker.getValue();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }
}
