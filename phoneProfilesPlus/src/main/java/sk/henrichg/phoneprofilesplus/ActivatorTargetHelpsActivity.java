package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivatorTargetHelpsActivity extends AppCompatActivity {

    public static ActivatorTargetHelpsActivity activity;
    //public static ActivateProfileActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        activity = this;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /*if (ActivateProfileActivity.getInstance() == null) {
            finish();
            return;
        }*/

        GlobalGUIRoutines.setTheme(this, true, true, false);
        GlobalGUIRoutines.setLanguage(this);

        Intent intent = new Intent("ShowActivatorTargetHelpsBroadcastReceiver");
        intent.putExtra(ActivateProfileActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //ActivateProfileActivity.getInstance().showTargetHelps();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
