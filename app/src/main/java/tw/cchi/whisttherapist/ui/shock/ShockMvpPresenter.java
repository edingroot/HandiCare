package tw.cchi.whisttherapist.ui.shock;

import tw.cchi.whisttherapist.base.MvpPresenter;

public interface ShockMvpPresenter<V extends ShockMvpView> extends MvpPresenter<V> {

    void onModeSelectionChanged(int selectedIndex);

    void powerOn();

    void powerOff();

    void onCustomStrengthChanged(int progressValue);

    void onCustomFrequencyChanged(int progressValue);

}
