package tw.cchi.handicare.ui.detection;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import tw.cchi.handicare.R;
import tw.cchi.handicare.device.bluno.BlunoHelper;
import tw.cchi.handicare.di.ActivityContext;
import tw.cchi.handicare.ui.base.BasePresenter;

public class DetectionPresenter<V extends DetectionMvpView> extends BasePresenter<V>
    implements DetectionMvpPresenter<V>, BlunoHelper.OnDetectionDataReceiveListener {
    private static final String TAG = DetectionPresenter.class.getSimpleName();

    @Inject @ActivityContext Context context;
    @Inject AppCompatActivity activity;

    private BlunoHelper blunoHelper;

    @Inject
    public DetectionPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        connectBlunoLibraryService().subscribe(blunoLibraryService -> {
           if (!blunoLibraryService.isDeviceConnected()) {
               getMvpView().showSnackBar(R.string.bluno_not_connected);
               activity.finish();
           } else {
               blunoHelper = new BlunoHelper(blunoLibraryService);
               blunoHelper.setMode(BlunoHelper.OpMode.DETECTION);
           }
        });
    }

    @Override
    public boolean enableDetection() {
        return blunoHelper != null && blunoHelper.setDetectionEnabled(true);
    }

    @Override
    public boolean disableDetection() {
        return blunoHelper != null && blunoHelper.setDetectionEnabled(false);
    }

    // BlunoHelper.OnDetectionDataReceiveListener
    @Override
    public void onDataReceive(int rawValue) {
        Log.i(TAG, "onDataReceive: " + rawValue);
        // TODO
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (blunoHelper != null)
            blunoHelper.dispose();
    }
}
