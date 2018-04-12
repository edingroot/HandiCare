package tw.cchi.handicare.ui.detection.shock;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.ui.base.BasePresenter;

public class DetectionPresenter<V extends DetectionMvpView> extends BasePresenter<V> implements DetectionMvpPresenter<V> {

    @Inject @ActivityContext Context context;

    @Inject
    public DetectionPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
