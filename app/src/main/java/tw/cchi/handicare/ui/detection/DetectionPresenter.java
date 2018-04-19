package tw.cchi.handicare.ui.detection;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.Config;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.bluno.BlunoHelper;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.model.ChartParameter;
import tw.cchi.handicare.ui.base.BasePresenter;
import tw.cchi.handicare.utils.port.CircularFifoQueue;

public class DetectionPresenter<V extends DetectionMvpView> extends BasePresenter<V>
    implements DetectionMvpPresenter<V>, BlunoHelper.DetectionDataListener {
    private static final String TAG = DetectionPresenter.class.getSimpleName();

    @Inject @ActivityContext Context context;
    @Inject AppCompatActivity activity;

    private BlunoHelper blunoHelper;
    private ChartParameter<Float> chartParameter;
    private CircularFifoQueue<Float> dataPoints = new CircularFifoQueue<>(Config.DETECTION_CHART_POINTS);
    private final List<Float> zeros;

    @Inject
    public DetectionPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);

        zeros = new ArrayList<>(Config.DETECTION_CHART_POINTS);
        for (int i = 0; i < Config.DETECTION_CHART_POINTS; i++) {
            zeros.add(0f);
        }
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        connectBlunoLibraryService().subscribe(blunoLibraryService -> {
           if (!blunoLibraryService.isDeviceConnected()) {
               getMvpView().showToast(R.string.bluno_not_connected);
               activity.finish();
           } else {
               blunoHelper = new BlunoHelper(blunoLibraryService);
               blunoHelper.setMode(BlunoHelper.OpMode.DETECTION);
           }
        });

        chartParameter = new ChartParameter<>(ChartParameter.ChartType.MULTI_LINE_CURVE);
        chartParameter.setAlpha(0.8f);
        chartParameter.addNumbersArray("EMG", zeros.toArray(new Float[zeros.size()]));
    }

    @Override
    public boolean enableDetection() {
        dataPoints.clear();
        dataPoints.addAll(zeros);
        chartParameter.updateNumbersArray(0, dataPoints.toArray(new Float[dataPoints.size()]));

        if (checkDeviceConnected() && blunoHelper.setDetectionEnabled(true)) {
            blunoHelper.setDetectionDataListener(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean disableDetection() {
        if (checkDeviceConnected() && blunoHelper.setDetectionEnabled(false)) {
            blunoHelper.removeDetectionDataListener();
            return true;
        } else {
            return false;
        }
    }

    // BlunoHelper.DetectionDataListener
    @Override
    public void onDataReceive(int rawValue) {
        Log.i(TAG, "onDataReceive: " + rawValue);

        if (!isViewAttached())
            return;

        dataPoints.add((float) rawValue);
        chartParameter.updateNumbersArray(0, dataPoints.toArray(new Float[dataPoints.size()]));
        getMvpView().updateChart(chartParameter);
    }

    private boolean checkDeviceConnected() {
        if (blunoHelper == null || !blunoHelper.isDeviceConnected()) {
            getMvpView().showSnackBar(R.string.bluno_not_connected);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (blunoHelper != null)
            blunoHelper.dispose();
    }
}
