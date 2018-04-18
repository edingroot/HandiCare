package tw.cchi.handicare.ui.shock;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface ShockMvpPresenter<V extends ShockMvpView> extends MvpPresenter<V> {

    boolean powerOn();

    boolean powerOff();

    void onCustomStrengthChanged(int progressValue);

    void onCustomFrequencyChanged(int progressValue);

    void updateViewDeviceControls();

}
