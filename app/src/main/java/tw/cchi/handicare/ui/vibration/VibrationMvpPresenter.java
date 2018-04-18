package tw.cchi.handicare.ui.vibration;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface VibrationMvpPresenter<V extends VibrationMvpView> extends MvpPresenter<V> {

    boolean powerOn();

    boolean powerOff();

    void onCustomStrengthChanged(int progressValue);

}
