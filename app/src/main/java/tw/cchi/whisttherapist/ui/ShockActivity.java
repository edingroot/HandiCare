package tw.cchi.whisttherapist.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import tw.cchi.whisttherapist.Constants;
import tw.cchi.whisttherapist.MvpApp;
import tw.cchi.whisttherapist.R;
import tw.cchi.whisttherapist.base.BaseActivity;
import tw.cchi.whisttherapist.component.ModeSelectionView;
import tw.cchi.whisttherapist.eshock.AcupStorage;
import tw.cchi.whisttherapist.eshock.DeviceAcup;

public class ShockActivity extends BaseActivity {
    private static final String ACTION_USB_PERMISSION = "tw.cchi.USB_PERMISSION";
    private static final long TIMER_TICK_INTERVAL = 20; // ms

    @Inject private MvpApp application;
    @Inject private DeviceAcup mDevAcup;
    @Inject private BroadcastReceiver mUsbReceiver;

    private Handler taskHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private float remainingSeconds = 0;
    private int initialSeconds = 0;
    private boolean timerRunning = false;

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
        ButterKnife.bind(this);

        // Register broadcast receiver events
        IntentFilter filterAttachedDetached = new IntentFilter();
        filterAttachedDetached.addAction(ACTION_USB_PERMISSION);
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filterAttachedDetached.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filterAttachedDetached.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mUsbReceiver, filterAttachedDetached);

        initComponents();
    }

    private void initComponents() {
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(10);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        modeSelectionView.setOnSelectionChangeListener(new ModeSelectionView.OnSelectionChangeListener() {
            @Override
            public void onChange(int selectedIndex) {
                if (selectedIndex == -1)
                    return;

                if (mDevAcup.isConnected()) {
                    mDevAcup.setStrength(Constants.SHOCK_MODE_STRENGTHS[selectedIndex]);
                    mDevAcup.setFrequency(Constants.SHOCK_MODE_FREQS[selectedIndex]);
                    updateDeviceControls();
                }
            }
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                remainingSeconds -= (float) TIMER_TICK_INTERVAL / 1000;
                if (remainingSeconds <= 0) {
                    timerRunning = false;
                    onPowerTimeEnd();
                }

                if (timerRunning) {
                    circleProgressView.setValue(initialSeconds - remainingSeconds);
                    taskHandler.postDelayed(timerRunnable, TIMER_TICK_INTERVAL);
                }
            }
        };

        togglePower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mDevAcup.connect())
                    Toast.makeText(ShockActivity.this, getString(R.string.usb_not_found), Toast.LENGTH_SHORT).show();

                if (isChecked) {
                    // Turn on
                    int seconds = minutesPicker.getValue() * 60 + secondsPicker.getValue();
                    if (seconds == 0)
                        return;

                    startPowerTimer(seconds);
                    if (modeSelectionView.getSelectedIndex() == -1)
                        modeSelectionView.setSelectedIndex(0);
                    mDevAcup.powerOn();
                } else {
                    // Turn off
                    mDevAcup.powerOff();
                    stopPowerTimer();
                }

                updateDeviceControls();
            }
        });

        seekStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (application.globalVar.bPower) {
                    progressValue = progressValue == 0 ? 1 : progressValue;
                    mDevAcup.setStrength(progressValue);
                } else {
                    progressValue = 0;
                }
                updateDeviceControls();
            }
        });

        seekFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (application.globalVar.bPower) {
                    progressValue = progressValue == 0 ? 1 : progressValue;
                    mDevAcup.setFrequency(progressValue);
                } else {
                    progressValue = 0;
                }
                updateDeviceControls();
            }
        });

        updateDeviceControls();
    }

    private void startPowerTimer(int seconds) {
        timerRunning = true;
        initialSeconds = seconds;
        remainingSeconds = seconds;
        circleProgressView.setMaxValue(initialSeconds);
        circleProgressView.setValue(0);
        taskHandler.postDelayed(timerRunnable, TIMER_TICK_INTERVAL);
    }

    private void stopPowerTimer() {
        timerRunning = false;
        initialSeconds = 0;
        remainingSeconds = 0;
        circleProgressView.setValue(0);
        taskHandler.removeCallbacks(timerRunnable);
    }

    private void onPowerTimeEnd() {
        mDevAcup.powerOff();
        updateDeviceControls();
    }

    private void updateDeviceControls() {
        int strength, frequency;

        if (application.globalVar.bPower) {
            togglePower.setChecked(true);
            strength = application.globalVar.nX;
            frequency = application.globalVar.nY;
        } else {
            togglePower.setChecked(false);
            stopPowerTimer();
            strength = 0;
            frequency = 0;
            modeSelectionView.setSelectedIndex(-1);
        }
        seekStrength.setProgress(strength);
        seekFreq.setProgress(frequency);
        txtStrengthVal.setText(String.valueOf(strength));
        txtFreqVal.setText(String.valueOf(frequency));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (application.globalVar.bPower) {
            application.globalVar.bPower = false;
            this.mDevAcup.commWithUsbDevice();
        }
        unregisterReceiver(this.mUsbReceiver);
    }

    @Override
    protected void setUp() {

    }


    class UsbBroadcastReceiver extends BroadcastReceiver {
        @Inject
        public UsbBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            UsbDevice device;
            String action = intent.getAction();

            if (action.equals(ShockActivity.ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false)) {
                        if (device != null) {
                            Log.d("1", "PERMISSION-" + device);
                        }

                        try {
                            Thread.sleep(1000);

                            if (mDevAcup.getTheTargetDevice() != null) {
                                if (mDevAcup.mUsbDevice.getInterfaceCount() > 0) {
                                    mDevAcup.mUsbInterface = mDevAcup.mUsbDevice.getInterface(0);
                                    mDevAcup.mEndpointRead = mDevAcup.mUsbInterface.getEndpoint(0);
                                    mDevAcup.mEndpointWrite = mDevAcup.mUsbInterface.getEndpoint(1);
                                }
                                application.globalVar.bPower = false;
                                application.globalVar.nX = 0;
                                application.globalVar.nY = 0;
                                application.globalVar.nZ = 0;
                                mDevAcup.commWithUsbDevice();

                                if (AcupStorage.nDeviceType != 0) {
                                    mDevAcup.commWithUsbDevice(11);
                                    mDevAcup.commWithUsbDevice(12);
                                }
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Thread was interrupted");
                            return;
                        }
                    }
                }

            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");

                    if (!intent.getBooleanExtra("permission", false)) {
                        mDevAcup.mUsbManager.requestPermission(
                                device,
                                PendingIntent.getBroadcast(
                                        ShockActivity.this,
                                        0,
                                        new Intent(ShockActivity.ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT
                                )
                        );
                    } else if (device != null) {
                        Log.d("1", "ATTACHED-" + device);
                        mDevAcup.connect();
                    }
                }

            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                synchronized (this) {
                    device = intent.getParcelableExtra("device");
                    if (device != null) {
                        Log.d("1", "DEATTCHED-" + device);
                    }
                    mDevAcup.disconnect();
                    updateDeviceControls();
                }

            } else if ("android.intent.action.BATTERY_CHANGED".equals(action) && intent.getIntExtra("level", 0) < 30) {
                // TODO
            }
        }
    }

}
