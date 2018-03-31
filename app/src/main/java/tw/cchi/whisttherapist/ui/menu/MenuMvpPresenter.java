package tw.cchi.whisttherapist.ui.menu;

import tw.cchi.whisttherapist.ui.base.MvpPresenter;

public interface MenuMvpPresenter<V extends MenuMvpView> extends MvpPresenter<V> {

    void launchVibrationMode();

    void launchShockMode();

    void launchDetectionMode();

    void launchPreferences();

}
