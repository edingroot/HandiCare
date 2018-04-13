package tw.cchi.handicare.ui.detection;

import android.os.Bundle;
import android.widget.ToggleButton;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import tw.cchi.handicare.R;
import tw.cchi.handicare.component.MultiChartView;
import tw.cchi.handicare.ui.base.BaseActivity;

public class DetectionActivity extends BaseActivity implements DetectionMvpView {

    @Inject DetectionMvpPresenter<DetectionMvpView> mPresenter;

    @BindView(R.id.togglePower) ToggleButton togglePower;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;
    @BindView(R.id.emgChartView) MultiChartView emgChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        mPresenter.onAttach(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

}
