package tw.cchi.handicare.ui.preferences;

import android.os.Bundle;

import tw.cchi.handicare.R;
import tw.cchi.handicare.ui.base.BaseActivity;

public class PreferencesActivity extends BaseActivity implements PreferencesMvpView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }
}
