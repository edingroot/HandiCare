package tw.cchi.whisttherapist.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.cchi.whisttherapist.R;

public class ModeSelectionView extends ConstraintLayout {

    @BindView(R.id.imgLight) ImageView imgLight;
    @BindView(R.id.imgNormal) ImageView imgNormal;
    @BindView(R.id.imgIntense) ImageView imgIntense;

    public ModeSelectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View rootView = inflate(context, R.layout.view_mode_selection, this);
        ButterKnife.bind(this, rootView);
    }

    public ModeSelectionView(Context context) {
        super(context);
        View rootView = inflate(context, R.layout.view_mode_selection, this);
        ButterKnife.bind(this, rootView);
    }
}
