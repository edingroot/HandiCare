package tw.cchi.whisttherapist.ui.menu;

import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.whisttherapist.R;
import tw.cchi.whisttherapist.ui.base.BaseActivity;

public class MenuActivity extends BaseActivity implements MenuMvpView {

    @Inject MenuMvpPresenter<MenuMvpView> presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);
    }

    @OnClick(R.id.btnVibrationMode)
    public void onVibrationModeClick(View v) {
        presenter.launchVibrationMode();
    }

    @OnClick(R.id.btnShockMode)
    public void onShockModeClick(View v) {
        presenter.launchShockMode();
    }

    @OnClick(R.id.btnDetectionMode)
    public void onDetectionModeClick(View v) {
        presenter.launchDetectionMode();
    }

    @OnClick(R.id.btnPreferences)
    public void onPreferencesClick(View v) {
        presenter.launchPreferences();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }
}
