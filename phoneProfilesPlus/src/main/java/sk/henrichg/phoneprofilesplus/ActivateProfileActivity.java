package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

public class ActivateProfileActivity extends AppCompatActivity {

    private static ActivateProfileActivity instance;

    float popupWidth;
    float popupHeight;

    private Toolbar toolbar;
    private ImageView eventsRunStopIndicator;

    public static final String PREF_START_TARGET_HELPS = "activate_profiles_activity_start_target_helps";

    @SuppressLint("NewApi")
    @SuppressWarnings({ "deprecation" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        instance = this;

        PPApplication.loadPreferences(getApplicationContext());
        GlobalGUIRoutines.setTheme(this, true, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

    // set window dimensions ----------------------------------------------------------

        Display display = getWindowManager().getDefaultDisplay();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        float popupMaxHeight;
        int actionBarHeight;

        // display dimensions
        popupWidth = display.getWidth();
        popupMaxHeight = display.getHeight();
        popupHeight = 0;
        actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        // set max. dimensions for display orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //popupWidth = Math.round(popupWidth / 100f * 50f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 50f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add header height
        if (PPApplication.applicationActivatorHeader)
            popupHeight = popupHeight + 64f * scale;

        // add toolbar height
        popupHeight = popupHeight + (25f + 1f + 3f) * scale;

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
        int profileCount = dataWrapper.getDatabaseHandler().getProfilesCount(true);
        dataWrapper.invalidateDataWrapper();

        if (!PPApplication.applicationActivatorGridLayout)
        {
            // add list items height
            popupHeight = popupHeight + (50f * scale * profileCount); // item
            popupHeight = popupHeight + (5f * scale * (profileCount-1)); // divider
        }
        else
        {
            // add grid items height
            int modulo = profileCount % 3;
            profileCount = profileCount / 3;
            if (modulo > 0)
                ++profileCount;
            popupHeight = popupHeight + (85f * scale * profileCount); // item
            popupHeight = popupHeight + (5f * scale * (profileCount-1)); // divider
        }

        popupHeight = popupHeight + (20f * scale); // listview padding

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));


    //-----------------------------------------------------------------------------------

        //Debug.startMethodTracing("phoneprofiles");

    // Layout ---------------------------------------------------------------------------------

        //requestWindowFeature(Window.FEATURE_ACTION_BAR);

        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        setContentView(R.layout.activity_activate_profile);

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onCreate - setContnetView");

        toolbar = (Toolbar)findViewById(R.id.act_prof_tollbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.title_activity_activator);

        eventsRunStopIndicator = (ImageView)findViewById(R.id.act_prof_run_stop_indicator);
        eventsRunStopIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), ActivateProfileActivity.this);
                popup.showOnAnchor(eventsRunStopIndicator, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT);
            }
        });

        refreshGUI(false);

    //-----------------------------------------------------------------------------------------		

    }

    public static ActivateProfileActivity getInstance()
    {
        return instance;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (instance == this)
            instance = null;
    }

    @Override
    protected void onResume()
    {
        //Debug.stopMethodTracing();
        super.onResume();
        if (instance == null)
        {
            instance = this;
            refreshGUI(false);
        }

        startTargetHelpsActivity();
    }

    @Override
    protected void onDestroy()
    {
    //	Debug.stopMethodTracing();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_activate_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // change global events run/stop menu item title
        MenuItem menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(PPApplication.getGlobalEventsRuning(getApplicationContext()));
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_edit_profiles:
            Intent intent = new Intent(getApplicationContext(), EditorProfilesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
            getApplicationContext().startActivity(intent);

            finish();

            return true;
        case R.id.menu_restart_events:
            DataWrapper dataWrapper = getDataWrapper();
            if (dataWrapper != null) {
                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                // ignoruj manualnu aktivaciu profilu
                // a odblokuj forceRun eventy
                PPApplication.logE("$$$ restartEvents", "from ActivateProfileActivity.onOptionsItemSelected menu_restart_events");
                dataWrapper.restartEventsWithAlert(this);
                dataWrapper.invalidateDataWrapper();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        //setContentView(R.layout.activity_phone_profiles);
        GlobalGUIRoutines.reloadActivity(this, false);
    }
    */

    public void refreshGUI(boolean refreshIcons)
    {
        setEventsRunStopIndicator();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
        if (fragment != null)
        {
            ((ActivateProfileListFragment)fragment).refreshGUI(refreshIcons);
        }
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
        if (fragment != null)
            return ((ActivateProfileListFragment)fragment).dataWrapper;
        else
            return null;
    }

    public void setEventsRunStopIndicator()
    {
        if (PPApplication.getGlobalEventsRuning(getApplicationContext()))
        {
            if (PPApplication.getEventsBlocked(getApplicationContext()))
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
            else
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
        }
        else
            eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stoppped);
    }

    private void startTargetHelpsActivity() {
        SharedPreferences preferences = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            ActivatorTargetHelpsActivity.activatorActivity = this;
            Intent intent = new Intent(this, ActivatorTargetHelpsActivity.class);
            startActivity(intent);

        }
    }

    public void showTargetHelps() {
        final SharedPreferences preferences = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true)) {

            Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.commit();

                TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                final Display display = getWindowManager().getDefaultDisplay();
                int popupLeft = (int) (display.getWidth() - popupWidth) / 2;
                int popupTop = (int) (display.getHeight() - popupHeight) / 2;

                getTheme().resolveAttribute(R.attr.actionEventsRestartIcon, tv, true);
                final Drawable restartEventsIcon = ContextCompat.getDrawable(this, tv.resourceId);
                int iconWidth = restartEventsIcon.getIntrinsicWidth(); //GlobalGUIRoutines.dpToPx(30);
                final Rect restartEventsTarget = new Rect(0, 0, restartEventsIcon.getIntrinsicWidth(), restartEventsIcon.getIntrinsicHeight());
                restartEventsTarget.offset((popupLeft+(int)popupWidth) - (/*iconWidth + */GlobalGUIRoutines.dpToPx(50)) * 2 /*- GlobalGUIRoutines.dpToPx(30)*/, popupTop+GlobalGUIRoutines.dpToPx(35));
                restartEventsIcon.setBounds(0, 0, GlobalGUIRoutines.dpToPx(35), GlobalGUIRoutines.dpToPx(35));

                getTheme().resolveAttribute(R.attr.actionEditProfilesIcon, tv, true);
                final Drawable actionEditProfilesIcon = ContextCompat.getDrawable(this, tv.resourceId);
                final Rect actionEditProfilesTarget = new Rect(0, 0, actionEditProfilesIcon.getIntrinsicWidth(), actionEditProfilesIcon.getIntrinsicHeight());
                actionEditProfilesTarget.offset((popupLeft+(int)popupWidth) - (/*iconWidth + */GlobalGUIRoutines.dpToPx(50))/* - GlobalGUIRoutines.dpToPx(30)*/, popupTop+GlobalGUIRoutines.dpToPx(35));
                actionEditProfilesIcon.setBounds(0, 0, GlobalGUIRoutines.dpToPx(35), GlobalGUIRoutines.dpToPx(35));

                int circleColor = 0xFFFFFF;
                if (PPApplication.applicationTheme.equals("dark"))
                    circleColor = 0x7F7F7F;

                final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);
                if (PPApplication.getGlobalEventsRuning(getApplicationContext()))
                    sequence.targets(
                            TapTarget.forBounds(actionEditProfilesTarget, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_ppp))
                                    .icon(actionEditProfilesIcon, true)
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forBounds(restartEventsTarget, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                    .icon(restartEventsIcon, true)
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(2)
                    );
                else
                    sequence.targets(
                            TapTarget.forBounds(actionEditProfilesTarget, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_ppp))
                                    .icon(actionEditProfilesIcon, true)
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1)
                    );

                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            ((ActivateProfileListFragment)fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.commit();
                    }
                });
                sequence.start();
            }
            else {
                Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            ((ActivateProfileListFragment)fragment).showTargetHelps();
                        }
                    }
                }, 500);
            }
        }
        else {
            if (ActivatorTargetHelpsActivity.activity != null) {
                Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
            }
        }
    }

}
