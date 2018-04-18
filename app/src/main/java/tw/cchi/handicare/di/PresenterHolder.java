package tw.cchi.handicare.di;

import io.reactivex.annotations.Nullable;
import tw.cchi.handicare.ui.shock.ShockMvpPresenter;

/**
 * For using presenter instances in classes injected in activities, prevent dependency cycles.
 */
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
