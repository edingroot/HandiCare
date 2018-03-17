package tw.cchi.whisttherapist.di;

import io.reactivex.annotations.Nullable;
import tw.cchi.whisttherapist.ui.shock.ShockMvpPresenter;

public final class PresenterHolder {
    private ShockMvpPresenter shockMvpPresenter;

    @Nullable
    public ShockMvpPresenter getShockMvpPresenter() {
        return shockMvpPresenter;
    }

    public void setShockMvpPresenter(ShockMvpPresenter shockMvpPresenter) {
        this.shockMvpPresenter = shockMvpPresenter;
    }

}
