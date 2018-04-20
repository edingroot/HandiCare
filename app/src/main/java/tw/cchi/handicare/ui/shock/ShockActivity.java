package tw.cchi.handicare.ui.shock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jakewharton.rxbinding2.widget.RxSeekBar;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.R;
import tw.cchi.handicare.ui.base.BaseActivity;
import tw.cchi.handicare.utils.CommonUtils;

public class ShockActivity extends BaseActivity implements ShockMvpView {

    @Inject ShockMvpPresenter<ShockMvpView> presenter;

    @BindView(R.id.minutesPicker) NumberPicker minutesPicker;
    @BindView(R.id.secondsPicker) NumberPicker secondsPicker;

    @BindView(R.id.togglePower) ToggleButton togglePower;
    @BindView(R.id.imgPowerAnimation) ImageView imgPowerAnimation;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;

    @BindView(R.id.seekStrength) SeekBar seekStrength;
    @BindView(R.id.txtStrengthVal) TextView txtStrengthVal;
    @BindView(R.id.seekFreq) SeekBar seekFreq;
    @BindView(R.id.txtFreqVal) TextView txtFreqVal;

    private Timer powerAnimationTimer;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shock);

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
        updateDeviceControls(false, 0, 0);

        // Avoid event triggered while rendering view
        new Handler(getMainLooper()).postDelayed(() -> {
                RxSeekBar.userChanges(seekStrength).subscribe(presenter::onCustomStrengthChanged);
                RxSeekBar.userChanges(seekFreq).subscribe(presenter::onCustomFrequencyChanged);
            }, 100
        );
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
    public void setPowerAnimationEnabled(boolean enable) {
        // Animate imgPowerAnimation
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() ->
                    imgPowerAnimation.animate().setDuration(Config.POWER_ANIMATION_DELAY).alpha(0.0f).withEndAction(() -> {
                        if (imgPowerAnimation == null) return;
                        imgPowerAnimation.animate().setDuration(Config.POWER_ANIMATION_DELAY).alpha(1);
                    })
                );
            }
        };

        if (enable) {
            powerAnimationTimer = new Timer();
            powerAnimationTimer.schedule(timerTask, 0, 2 * Config.POWER_ANIMATION_DELAY);
        } else {
            if (powerAnimationTimer != null)
                powerAnimationTimer.cancel();
        }
    }

    @Override
    public void updateDeviceControls(boolean isPowerOn, int strength, int frequency) {
        if (isPowerOn) {
            if (!togglePower.isChecked())
                togglePower.setChecked(true);
        } else {
            if (togglePower.isChecked())
                togglePower.setChecked(false);
            strength = 0;
            frequency = 0;
        }
        seekStrength.setProgress(strength);
        seekFreq.setProgress(frequency);
        txtStrengthVal.setText(CommonUtils.padLeft(String.valueOf(strength), '0', 2));
        txtFreqVal.setText(CommonUtils.padLeft(String.valueOf(frequency), '0', 2));
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
        if (powerAnimationTimer != null)
            powerAnimationTimer.cancel();
        presenter.onDetach();
        super.onDestroy();
    }
}
