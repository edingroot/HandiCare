package tw.cchi.whisttherapist.ui.shock;

import tw.cchi.whisttherapist.base.MvpView;

public interface ShockMvpView extends MvpView {

    void updateDeviceControls(boolean isPowerOn, int strength, int frequency);

    void setProgress(float value, float max);

    /**
     * @return Duration set by user in seconds.
     */
    int getDurationSetting();

    int getModeSelection();

    void setModeSelection(int index);

}
