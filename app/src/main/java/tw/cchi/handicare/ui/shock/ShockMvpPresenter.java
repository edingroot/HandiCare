package tw.cchi.handicare.ui.shock;

import io.reactivex.Observable;
import tw.cchi.handicare.ui.base.MvpPresenter;

public interface ShockMvpPresenter<V extends ShockMvpView> extends MvpPresenter<V> {

    boolean powerOn();

    boolean powerOff();

    Observable<Boolean> switchUsbMode(boolean isUsbMode);

    void onCustomStrengthChanged(int progressValue);

    void onCustomFrequencyChanged(int progressValue);

    void updateViewDeviceControls();

    boolean isUsbMode();

}
