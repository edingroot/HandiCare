package tw.cchi.handicare.ui.detection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.R;
import tw.cchi.handicare.component.MultiChartView;
import tw.cchi.handicare.model.ChartParameter;
import tw.cchi.handicare.ui.base.BaseActivity;

public class DetectionActivity extends BaseActivity implements DetectionMvpView {

    @Inject DetectionMvpPresenter<DetectionMvpView> presenter;

    @BindView(R.id.toggleEnable) ToggleButton toggleEnable;
    @BindView(R.id.imgPowerAnimation) ImageView imgPowerAnimation;
    @BindView(R.id.circleProgressView) CircleProgressView circleProgressView;
    @BindView(R.id.emgChartView) MultiChartView emgChartView;

    private Timer powerAnimationTimer;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

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
    public void setPowerAnimationEnabled(boolean enable) {
        // Animate imgPowerAnimation
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() ->
                    imgPowerAnimation.animate().setDuration(Config.POWER_ANIMATION_DELAY).alpha(0.3f).withEndAction(() -> {
                        if (imgPowerAnimation == null) return;
                        imgPowerAnimation.animate().setDuration(Config.POWER_ANIMATION_DELAY).alpha(1);
                    })
                );
            }
        };

        if (enable) {
            powerAnimationTimer = new Timer();
            powerAnimationTimer.schedule(timerTask, 0, 2 * Config.POWER_ANIMATION_DELAY);
        } else {
            if (powerAnimationTimer != null)
                powerAnimationTimer.cancel();
        }
    }

    @Override
    public void updateChart(ChartParameter<? extends Number> chartParameter) {
        emgChartView.updateChart(chartParameter);
    }

    @Override
    protected void onDestroy() {
        if (powerAnimationTimer != null)
            powerAnimationTimer.cancel();
        presenter.onDetach();
        super.onDestroy();
    }
}
