package tw.cchi.handicare.ui.vibration;

import tw.cchi.handicare.ui.base.MvpView;

public interface VibrationMvpView extends MvpView {

    void updateDeviceControls(boolean isPowerOn, int strength);

    void setProgress(float value, float max);

    /**
     * @return Duration set by user in seconds.
     */
    int getDurationSetting();

}
