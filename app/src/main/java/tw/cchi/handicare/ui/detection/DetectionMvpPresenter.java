package tw.cchi.handicare.ui.detection;

import tw.cchi.handicare.ui.base.MvpPresenter;

public interface DetectionMvpPresenter<V extends DetectionMvpView> extends MvpPresenter<V> {

    boolean enableDetection();

}
