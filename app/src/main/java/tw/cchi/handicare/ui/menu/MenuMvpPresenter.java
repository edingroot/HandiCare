package tw.cchi.handicare.ui.menu;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface MenuMvpPresenter<V extends MenuMvpView> extends MvpPresenter<V> {

    void startServices();

    void handleBlunoActivityResult(int requestCode, int resultCode);

    boolean tryResetBlunoState();

    void launchVibrationMode();

    void launchShockMode();

    void launchDetectionMode();

    void launchPreferences();

}
