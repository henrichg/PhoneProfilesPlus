package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class ActivatorTargetHelpsActivity extends Activity {

    public static ActivatorTargetHelpsActivity activity;
    public static ActivateProfileActivity activatorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

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
        activatorActivity.showTargetHelps();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
