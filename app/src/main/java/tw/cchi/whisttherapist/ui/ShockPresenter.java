package tw.cchi.whisttherapist.ui;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.whisttherapist.base.BasePresenter;
import tw.cchi.whisttherapist.base.MvpPresenter;

public class ShockPresenter<V extends ShockView> extends BasePresenter<V> implements MvpPresenter<V> {

    @Inject
    public ShockPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

}
