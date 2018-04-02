package tw.cchi.handicare.di;

import io.reactivex.annotations.Nullable;
import tw.cchi.handicare.ui.shock.ShockMvpPresenter;

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
