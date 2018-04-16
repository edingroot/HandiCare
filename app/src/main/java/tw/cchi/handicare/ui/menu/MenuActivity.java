package tw.cchi.handicare.ui.menu;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import tw.cchi.handicare.R;
import tw.cchi.handicare.ui.base.BaseActivity;

@RuntimePermissions
public class MenuActivity extends BaseActivity implements MenuMvpView {

    @Inject MenuMvpPresenter<MenuMvpView> presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);

        MenuActivityPermissionsDispatcher.callStartServicesWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION})
    void callStartServices() {
        presenter.startServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MenuActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.handleBlunoActivityResult(requestCode, resultCode);
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
