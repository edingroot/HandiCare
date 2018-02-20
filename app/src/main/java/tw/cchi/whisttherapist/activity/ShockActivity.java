package tw.cchi.whisttherapist.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.iwgang.countdownview.CountdownView;
import tw.cchi.whisttherapist.Constants;
import tw.cchi.whisttherapist.GlobalVariable;
import tw.cchi.whisttherapist.R;
import tw.cchi.whisttherapist.eshock.AcupStorage;
import tw.cchi.whisttherapist.eshock.DeviceAcup;

public class ShockActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "tw.cchi.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new UsbBroadcastReceiver();
    public DeviceAcup mDevAcup;
    private GlobalVariable globalVar;

    @BindView(R.id.countdownShockPower) CountdownView countdownShockPower;
    @BindView(R.id.multiToggleShockMode) MultiStateToggleButton multiToggleShockMode;

    @BindView(R.id.seekStrength) SeekBar seekStrength;
    @BindView(R.id.txtStrengthVal) TextView txtStrengthVal;
    @BindView(R.id.seekFreq) SeekBar seekFreq;
    @BindView(R.id.txtFreqVal) TextView txtFreqVal;
    @BindView(R.id.togglePower) ToggleButton togglePower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shock);
        ButterKnife.bind(this);

        this.globalVar = (GlobalVariable) getApplicationContext();
        this.mDevAcup = new DeviceAcup(
                this.globalVar, this,
                (UsbManager) getSystemService(Context.USB_SERVICE)
        );

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
        multiToggleShockMode.setElements(Constants.SHOCK_MODE_TITLES);
        multiToggleShockMode.setOnValueChangedListener(new org.honorato.multistatetogglebutton.ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {
                mDevAcup.setStrength(Constants.SHOCK_MODE_STRENGTHS[position]);
                mDevAcup.setFrequency(Constants.SHOCK_MODE_FREQS[position]);
                updateDeviceControls();
            }
        });

        togglePower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mDevAcup.connect())
                    Toast.makeText(ShockActivity.this, getString(R.string.usb_not_found), Toast.LENGTH_SHORT).show();

                if (isChecked) {
                    boolean[] selectedStatus = new boolean[Constants.SHOCK_MODE_TITLES.length];
                    selectedStatus[0] = true;
                    multiToggleShockMode.setStates(selectedStatus);
                    countdownShockPower.start(Constants.SHOCK_POWER_COUNTDOWN_SECONDS * 1000);
                    mDevAcup.powerOn();
                } else {
                    mDevAcup.powerOff();
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
                if (globalVar.bPower) {
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
                if (globalVar.bPower) {
                    progressValue = progressValue == 0 ? 1 : progressValue;
                    mDevAcup.setFrequency(progressValue);
                } else {
                    progressValue = 0;
                }
                updateDeviceControls();
            }
        });

        countdownShockPower.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                mDevAcup.powerOff();
                updateDeviceControls();
            }
        });

        updateDeviceControls();
    }

    private void updateDeviceControls() {
        int strength, frequency;

        if (globalVar.bPower) {
            togglePower.setChecked(true);
            setShockModeToggleState(true);
            strength = globalVar.nX;
            frequency = globalVar.nY;
        } else {
            togglePower.setChecked(false);
            setShockModeToggleState(false);
            countdownShockPower.stop();
            countdownShockPower.updateShow(0);
            strength = 0;
            frequency = 0;
        }
        seekStrength.setProgress(strength);
        seekFreq.setProgress(frequency);
        txtStrengthVal.setText(String.valueOf(strength));
        txtFreqVal.setText(String.valueOf(frequency));
    }

    private void setShockModeToggleState(boolean enabled) {
        if (enabled) {
            multiToggleShockMode.setColorRes(R.color.colorPrimary, android.R.color.white);
            multiToggleShockMode.setEnabled(true);
        } else {
            multiToggleShockMode.setStates(new boolean[Constants.SHOCK_MODE_TITLES.length]);
            multiToggleShockMode.setColorRes(R.color.inactiveGrayDark, R.color.inactiveGray);
            multiToggleShockMode.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.globalVar.bPower) {
            this.globalVar.bPower = false;
            this.mDevAcup.commWithUsbDevice();
        }
        unregisterReceiver(this.mUsbReceiver);
    }


    class UsbBroadcastReceiver extends BroadcastReceiver {
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
                                globalVar.bPower = false;
                                globalVar.nX = 0;
                                globalVar.nY = 0;
                                globalVar.nZ = 0;
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
