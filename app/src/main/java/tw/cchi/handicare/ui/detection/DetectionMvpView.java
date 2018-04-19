package tw.cchi.handicare.ui.detection;

import tw.cchi.handicare.model.ChartParameter;
import tw.cchi.handicare.ui.base.MvpView;

public interface DetectionMvpView extends MvpView {

    void setToggleEnabled(boolean enabled);

    void updateChart(ChartParameter chartParameter);

}
