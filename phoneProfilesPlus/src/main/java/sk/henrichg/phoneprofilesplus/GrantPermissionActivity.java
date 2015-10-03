package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GrantPermissionActivity extends Activity {

    private long profile_id;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        intent = getIntent();
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);

    }

    @Override
    protected void onStart()
    {
        super.onStart();


        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
