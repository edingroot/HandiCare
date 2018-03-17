package tw.cchi.whisttherapist.di;

import javax.inject.Inject;

import io.reactivex.annotations.Nullable;
import tw.cchi.whisttherapist.ui.shock.ShockMvpPresenter;
import tw.cchi.whisttherapist.ui.shock.ShockMvpView;

public final class PresenterHolder {
    private ShockMvpPresenter<ShockMvpView> mPresenter;

    @Nullable
    public ShockMvpPresenter<ShockMvpView> getmPresenter() {
        return mPresenter;
    }

    public void setmPresenter(ShockMvpPresenter<ShockMvpView> mPresenter) {
        this.mPresenter = mPresenter;
    }
}
