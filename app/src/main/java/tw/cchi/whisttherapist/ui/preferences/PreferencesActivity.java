package tw.cchi.whisttherapist.ui.preferences;

import android.os.Bundle;

import tw.cchi.whisttherapist.R;
import tw.cchi.whisttherapist.ui.base.BaseActivity;

public class PreferencesActivity extends BaseActivity implements PreferencesMvpView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }
}
