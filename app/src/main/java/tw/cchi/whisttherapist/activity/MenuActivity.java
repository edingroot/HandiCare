package tw.cchi.whisttherapist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;
import tw.cchi.whisttherapist.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
    }

    public void onVibrationModeClick(View v) {
        startActivity(new Intent(MenuActivity.this, VibrationActivity.class));
    }

    public void onShockModeClick(View v) {
        startActivity(new Intent(MenuActivity.this, ShockActivity.class));
    }

    public void onDetectionModeClick(View v) {

    }
}
