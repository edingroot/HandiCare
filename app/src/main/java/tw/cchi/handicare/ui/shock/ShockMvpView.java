package tw.cchi.handicare.ui.shock;

import tw.cchi.handicare.ui.base.MvpView;

public interface ShockMvpView extends MvpView {

    void setPowerAnimationEnabled(boolean enable);

    void updateDeviceControls(boolean isPowerOn, int strength, int frequency);

    void setProgress(float value, float max);

    /**
     * @return Duration set by user in seconds.
     */
    int getDurationSetting();

}
