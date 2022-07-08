package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LaunchSamsungEdgeConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        try {
            Intent intent = new Intent(this, PhoneProfilesPrefsActivity.class);
            intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "categorySamsungEdgePanelRoot");
            //noinspection deprecation
            startActivityForResult(intent, 100);
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    public void finish() {
        // finish is called before of onStop()

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);

        super.finish();
        overridePendingTransition(0, 0);
    }

}
