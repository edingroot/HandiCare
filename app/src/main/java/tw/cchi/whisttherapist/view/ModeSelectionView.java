package tw.cchi.whisttherapist.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.cchi.whisttherapist.R;

public class ModeSelectionView extends ConstraintLayout {
    private static final float BUTTON_ZOOM_SCALE = 1.5f;
    private static final int BUTTON_ZOOM_DURATION = 80;

    private OnSelectionChangeListener onSelectionChangeListener;
    private OnClickListener onButtonClickListener;
    private int selectedIndex = -1;
    private ImageView[] buttons;

    @BindView(R.id.imgLight) ImageView imgLight;
    @BindView(R.id.imgNormal) ImageView imgNormal;
    @BindView(R.id.imgIntense) ImageView imgIntense;

    public ModeSelectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View rootView = inflate(context, R.layout.view_mode_selection, this);
        ButterKnife.bind(this, rootView);
        initialize();
    }

    public ModeSelectionView(Context context) {
        super(context);
        View rootView = inflate(context, R.layout.view_mode_selection, this);
        ButterKnife.bind(this, rootView);
        initialize();
    }

    private void initialize() {
        buttons = new ImageView[]{imgLight, imgNormal, imgIntense};

        onButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = -1;

                for (int i = 0; i < buttons.length; i++) {
                    if (v.equals(buttons[i]))
                        newIndex = i;
                }
                if (newIndex == -1 || selectedIndex == newIndex)
                    return;

                setSelectedIndex(newIndex);
            }
        };

        imgLight.setOnClickListener(onButtonClickListener);
        imgNormal.setOnClickListener(onButtonClickListener);
        imgIntense.setOnClickListener(onButtonClickListener);
    }

    /**
     *
     * @param newIndex index = 0, 1, 2; set to -1 to deselect all
     */
    public void setSelectedIndex(int newIndex) {
        // Zoom out animation for the previous selected button
        if (selectedIndex != -1) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    BUTTON_ZOOM_SCALE, 1f, BUTTON_ZOOM_SCALE, 1f, ScaleAnimation.RELATIVE_TO_SELF,
                    0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(BUTTON_ZOOM_DURATION);
            scaleAnimation.setFillAfter(true);
            buttons[selectedIndex].startAnimation(scaleAnimation);
        }

        // Zoom in animation for the new selected button
        if (newIndex != -1) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    1f, BUTTON_ZOOM_SCALE, 1f, BUTTON_ZOOM_SCALE, ScaleAnimation.RELATIVE_TO_SELF,
                    0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(BUTTON_ZOOM_DURATION);
            scaleAnimation.setFillAfter(true);
            buttons[newIndex].startAnimation(scaleAnimation);
        }

        // Fire event
        onSelectionChangeListener.onChange(newIndex);
        selectedIndex = newIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        this.onSelectionChangeListener = onSelectionChangeListener;
    }

    public interface OnSelectionChangeListener {
        void onChange(int selectedIndex);
    }
}
