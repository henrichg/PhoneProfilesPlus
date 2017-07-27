package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.Bundle;

public class ActivatorTargetHelpsActivity extends Activity {

    public static ActivatorTargetHelpsActivity activity;
    public static ActivateProfileActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        GlobalGUIRoutines.setTheme(this, true, true);

        if (activatorActivity == null) {
            finish();
            return;
        }

        activatorActivity.showTargetHelps();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
