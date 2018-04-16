// Ref: https://github.com/MindorksOpenSource/android-mvp-architecture

package tw.cchi.handicare.ui.base;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import tw.cchi.handicare.MvpApp;
import tw.cchi.handicare.service.bluno.BlunoLibraryService;
import tw.cchi.handicare.helper.pref.PreferencesHelper;

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * onAttach() and onDetach(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
public class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private final CompositeDisposable mCompositeDisposable;
    private V mMvpView;

    @Inject public PreferencesHelper preferencesHelper;
    @Inject MvpApp application;
    public BlunoLibraryService blunoLibraryService;

    @Inject
    public BasePresenter(CompositeDisposable compositeDisposable) {
        this.mCompositeDisposable = compositeDisposable;
    }

    @Override
    public void onAttach(V mvpView) {
        this.mMvpView = mvpView;
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        mMvpView = null;
    }

    @Override
    public boolean isViewAttached() {
        return mMvpView != null;
    }

    public V getMvpView() {
        return mMvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.onAttach(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }

    // ------------------------------------------------------------------------------------- //

    protected Observable<BlunoLibraryService> connectBlunoLibraryService() {
        Observable<BlunoLibraryService> observable;

        if (blunoLibraryService == null) {
            observable = Observable.create(emitter -> {
                application.getBlunoLibraryService(service -> {
                    blunoLibraryService = (BlunoLibraryService) service;
                    emitter.onNext(blunoLibraryService);
                });
            });
        } else {
            observable = Observable.just(blunoLibraryService);
        }

        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    protected boolean autoConnectBluno() {
        String savedDeviceAddress = preferencesHelper.getBTDeviceAddress();
        if (savedDeviceAddress == null)
            return false;

        switch (blunoLibraryService.getConnectionState()) {
            case isNull:
            case isToScan:
                blunoLibraryService.connect("-", savedDeviceAddress);
                break;
        }

        return true;
    }
}
