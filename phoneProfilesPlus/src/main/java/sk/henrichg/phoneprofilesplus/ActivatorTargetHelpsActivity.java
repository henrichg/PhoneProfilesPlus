package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivatorTargetHelpsActivity extends AppCompatActivity {

    static volatile ActivatorTargetHelpsActivity activity;
    //static ActivatorActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Window window = getWindow();

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        activity = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        /*if (ActivatorActivity.getInstance() == null) {
            finish();
            return;
        }*/

        //GlobalGUIRoutines.setTheme(this, true, true/*, false*/);
        //GlobalGUIRoutines.setLanguage(this);

//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] ActivatorTargetHelpsActivity.onStart", "xxx");
        Intent intent = new Intent(ActivatorActivity.ACTION_SHOW_ACTIVATOR_TARGET_HELPS_BROADCAST_RECEIVER);
        intent.putExtra(ActivatorActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //ActivatorActivity.getInstance().showTargetHelps();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
