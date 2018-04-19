package tw.cchi.handicare.ui.detection;

import android.os.Bundle;
import android.widget.ToggleButton;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.R;
import tw.cchi.handicare.component.MultiChartView;
import tw.cchi.handicare.model.ChartParameter;
import tw.cchi.handicare.ui.base.BaseActivity;

public class DetectionActivity extends BaseActivity implements DetectionMvpView {

    @Inject DetectionMvpPresenter<DetectionMvpView> presenter;

    @BindView(R.id.toggleEnable) ToggleButton toggleEnable;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;
    @BindView(R.id.emgChartView) MultiChartView emgChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        getActivityComponent().inject(this);
        setUnBinder(ButterKnife.bind(this));
        presenter.onAttach(this);
    }

    @OnClick(R.id.toggleEnable)
    void toggleEnableClick(ToggleButton toggleButton) {
        if (toggleButton.isChecked()) {
            if (!presenter.enableDetection()) {
                toggleButton.setChecked(false);
            }
        } else {
            presenter.disableDetection();
        }
    }

    @Override
    public void setToggleEnabled(boolean enabled) {
        toggleEnable.setChecked(enabled);
    }

    @Override
    public void updateChart(ChartParameter<? extends Number> chartParameter) {
        emgChartView.updateChart(chartParameter);
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }
}
