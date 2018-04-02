package tw.cchi.handicare.ui.shock;

import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxSeekBar;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import tw.cchi.handicare.R;
import tw.cchi.handicare.component.ModeSelectionView;
import tw.cchi.handicare.ui.base.BaseActivity;

public class ShockActivity extends BaseActivity implements ShockMvpView {

    @Inject ShockMvpPresenter<ShockMvpView> mPresenter;

    @BindView(R.id.modeSelectionView) ModeSelectionView modeSelectionView;
    @BindView(R.id.minutesPicker) NumberPicker minutesPicker;
    @BindView(R.id.secondsPicker) NumberPicker secondsPicker;

    @BindView(R.id.togglePower) ToggleButton togglePower;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;

    @BindView(R.id.seekStrength) SeekBar seekStrength;
    @BindView(R.id.txtStrengthVal) TextView txtStrengthVal;
    @BindView(R.id.seekFreq) SeekBar seekFreq;
    @BindView(R.id.txtFreqVal) TextView txtFreqVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shock);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        mPresenter.onAttach(this);

        initComponents();
    }

    private void initComponents() {
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(10);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        updateDeviceControls(false, 0, 0);

        modeSelectionView.setOnSelectionChangeListener(new ModeSelectionView.OnSelectionChangeListener() {
            @Override
            public void onChange(int selectedIndex) {
                if (selectedIndex != -1)
                    mPresenter.onModeSelectionChanged(selectedIndex);
            }
        });

        RxCompoundButton.checkedChanges(togglePower).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isChecked) throws Exception {
                if (isChecked)
                    mPresenter.powerOn();
                else
                    mPresenter.powerOff();
            }
        });

        RxSeekBar.userChanges(seekStrength).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer value) throws Exception {
                mPresenter.onCustomStrengthChanged(value);
            }
        });

        RxSeekBar.userChanges(seekFreq).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer value) throws Exception {
                mPresenter.onCustomFrequencyChanged(value);
            }
        });
    }

    @Override
    public void updateDeviceControls(boolean isPowerOn, int strength, int frequency) {
        if (isPowerOn) {
            togglePower.setChecked(true);
        } else {
            togglePower.setChecked(false);
            strength = 0;
            frequency = 0;
            setModeSelection(-1);
        }
        seekStrength.setProgress(strength);
        seekFreq.setProgress(frequency);
        txtStrengthVal.setText(String.valueOf(strength));
        txtFreqVal.setText(String.valueOf(frequency));
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
    public int getModeSelection() {
        return modeSelectionView.getSelectedIndex();
    }

    @Override
    public void setModeSelection(int index) {
        modeSelectionView.setSelectedIndex(index);
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

}
