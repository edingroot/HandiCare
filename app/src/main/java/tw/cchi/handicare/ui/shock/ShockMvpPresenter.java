package tw.cchi.handicare.ui.shock;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface ShockMvpPresenter<V extends ShockMvpView> extends MvpPresenter<V> {

    void powerOn();

    void powerOff();

    void onCustomStrengthChanged(int progressValue);

    void onCustomFrequencyChanged(int progressValue);

    void updateViewDeviceControls();

}
