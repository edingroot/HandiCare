package tw.cchi.handicare.device.bluno;

import android.widget.Toast;

import io.reactivex.disposables.Disposable;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.service.bluno.BlunoLibraryService;

public class BlunoHelper implements BlunoLibraryService.BleEventListener, Disposable {
    public enum OpMode {STANDBY, VIBRATION, SHOCK, DETECTION}
    private enum OpCode {CHANGE_MODE, SET_PARAMS}
    private static final String SPLITTER = ",";

    private BlunoLibraryService blunoLibraryService;
    private DetectionDataListener detectionDataListener;
    private static final Object btWriteLock = new Object();
    private boolean disposed = false;

    // Device states
    private OpMode currentMode = OpMode.STANDBY;
    private boolean vibrationEnabled = false;
    private boolean shockEnabled = false;
    private boolean detectionEnabled = false;
    private int vibrationStrength = 150; // 0-255
    private int readErrorCount = 0;

    public BlunoHelper(BlunoLibraryService blunoLibraryService) {
        this.blunoLibraryService = blunoLibraryService;
        this.blunoLibraryService.attachEventListener(this);
    }

    public boolean isDeviceConnected() {
        return blunoLibraryService != null && blunoLibraryService.isDeviceConnected();
    }

    public boolean resetDeviceState() {
        return setMode(OpMode.STANDBY) &&
            (!vibrationEnabled || setVibrationEnabled(false, vibrationStrength)) &&
            (!shockEnabled || setShockEnabled(false)) &&
            (!detectionEnabled || setDetectionEnabled(false));
    }

    public OpMode getMode() {
        return currentMode;
    }

    public boolean setMode(OpMode opMode) {
        return sendCommand(OpCode.CHANGE_MODE, opMode.ordinal());
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public boolean setVibrationEnabled(boolean enabled) {
        return setVibrationEnabled(enabled, vibrationStrength);
    }

    public int getVibrationStrength() {
        return vibrationStrength;
    }

    public boolean setVibrationEnabled(boolean enabled, int strength) {
        if (currentMode != OpMode.VIBRATION && !setMode(OpMode.VIBRATION))
            return false;
        return sendCommand(OpCode.SET_PARAMS, OpMode.VIBRATION.ordinal(), enabled ? 1 : 0, strength);
    }

    public boolean isShockEnabled() {
        return shockEnabled;
    }

    public boolean setShockEnabled(boolean enabled) {
        if (currentMode != OpMode.SHOCK && !setMode(OpMode.SHOCK))
            return false;
        return sendCommand(OpCode.SET_PARAMS, OpMode.SHOCK.ordinal(), enabled ? 1 : 0);
    }

    public boolean isDetectionEnabled() {
        return detectionEnabled;
    }

    public boolean setDetectionEnabled(boolean enabled) {
        if (currentMode != OpMode.DETECTION && !setMode(OpMode.DETECTION))
            return false;
        return sendCommand(OpCode.SET_PARAMS, OpMode.DETECTION.ordinal(), enabled ? 1 : 0);
    }

    public void setDetectionDataListener(DetectionDataListener detectionDataListener) {
        this.detectionDataListener = detectionDataListener;
    }

    public void removeDetectionDataListener() {
        this.detectionDataListener = null;
    }

    private boolean sendCommand(OpCode opCode, Object... args) {
        if (!isDeviceConnected())
            return false;

        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append(opCode.ordinal());

        switch (opCode) {
            case CHANGE_MODE: {
                if (args.length != 1)
                    return false;

                cmdBuilder.append(SPLITTER).append(args[0]);
                break;
            }
            case SET_PARAMS: {
                if (args.length < 2)
                    return false;

                for (int i = 0; i < args.length; i++) {
                    cmdBuilder.append(SPLITTER).append(args[i]);
                }
                break;
            }
        }
        cmdBuilder.append("\n");

        new Thread(() -> {
            synchronized (btWriteLock) {
                blunoLibraryService.serialSend(cmdBuilder.toString());

                try {
                    Thread.sleep(Config.BLUNO_CMD_TRANSMIT_INTERVAL);
                } catch (InterruptedException e) { }
            }
        }).start();

        return true;
    }

    private boolean readBlunoOutput(String line) {
        String[] tokens = line.split(SPLITTER);
        int[] iTokens = new int[tokens.length];

        if (tokens.length < 1) {
            return false;
        } else {
            try {
                for (int i = 0; i < tokens.length; i++)
                    iTokens[i] = Integer.parseInt(tokens[i].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                if (++readErrorCount > Config.BLUNO_READ_ERROR_DISCONN_THRESHOLD) {
                    Toast.makeText(blunoLibraryService.getApplicationContext(), "請重開裝置電源！", Toast.LENGTH_LONG).show();
                    blunoLibraryService.disconnect();
                    readErrorCount = 0;
                }
                return false;
            }
        }

        // OpMode
        if (iTokens[0] >= OpMode.values().length)
            return false;
        else
            currentMode = OpMode.values()[iTokens[0]];

        switch (currentMode) {
            case VIBRATION:
                if (iTokens.length == 2) {
                    vibrationEnabled = iTokens[1] == 1;
                } else {
                    return false;
                }
                break;

            case SHOCK:
                if (iTokens.length == 2) {
                    shockEnabled = iTokens[1] == 1;
                } else {
                    return false;
                }
                break;

            case DETECTION:
                if (iTokens.length == 3) {
                    detectionEnabled = iTokens[1] == 1;
                    if (detectionEnabled && detectionDataListener != null)
                        detectionDataListener.onDataReceive(iTokens[2]);
                } else {
                    return false;
                }
                break;
        }

        return true;
    }

    @Override
    public void onConnectionStateChange(BlunoLibraryService.DeviceConnectionState deviceConnectionState) {
    }

    @Override
    public void onSerialReceived(String message) {
        readBlunoOutput(message);
    }

    @Override
    public void dispose() {
        if (blunoLibraryService != null)
            blunoLibraryService.detachEventListener(this);

        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public interface DetectionDataListener {
        /**
         * Only triggered while detection is enabled.
         */
        void onDataReceive(int rawValue);
    }
}
