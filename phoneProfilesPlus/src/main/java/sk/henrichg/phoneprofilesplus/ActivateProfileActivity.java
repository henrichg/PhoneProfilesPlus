package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import me.drakeet.support.toast.ToastCompat;

public class ActivateProfileActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    private Toolbar toolbar;
    private ImageView eventsRunStopIndicator;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profiles_activity_start_target_helps";

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            PPApplication.logE("[IN_BROADCAST] ActivateProfileActivity.refreshGUIBroadcastReceiver", "xxx");
            //boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            ActivateProfileActivity.this.refreshGUI(/*refresh,*//*true,*/  refreshIcons);
        }
    };

    static final String EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY = "show_target_helps_for_activity";
    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            PPApplication.logE("[IN_BROADCAST] ActivateProfileActivity.showTargetHelpsBroadcastReceiver", "xxx");
            if (ActivateProfileActivity.this.isFinishing()) {
                if (ActivatorTargetHelpsActivity.activity != null)
                    ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
                return;
            }
            if (ActivateProfileActivity.this.isDestroyed()) {
                if (ActivatorTargetHelpsActivity.activity != null)
                    ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
                return;
            }

            if (ApplicationPreferences.prefActivatorActivityStartTargetHelps ||
                    ApplicationPreferences.prefActivatorFragmentStartTargetHelps ||
                    ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

                boolean forActivity = intent.getBooleanExtra(EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, false);
                if (forActivity)
                    ActivateProfileActivity.this.showTargetHelps();
                else {
                    Fragment fragment = ActivateProfileActivity.this.getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
                    if (fragment != null) {
                        ((ActivateProfileListFragment) fragment).showTargetHelps();
                    }
                }
            }
            else {
                if (ActivatorTargetHelpsActivity.activity != null)
                    ActivatorTargetHelpsActivity.activity.finish();
                ActivatorTargetHelpsActivity.activity = null;
            }
        }
    };

    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            PPApplication.logE("[IN_BROADCAST] ActivateProfileActivity.finishBroadcastReceiver", "xxx");
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(PPApplication.ACTION_FINISH_ACTIVITY)) {
                    String what = intent.getStringExtra(PPApplication.EXTRA_WHAT_FINISH);
                    if (what.equals("activator")) {
                        try {
                            ActivateProfileActivity.this.setResult(Activity.RESULT_CANCELED);
                            ActivateProfileActivity.this.finishAffinity();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
            }
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //PPApplication.logE("ActivateProfileActivity.onCreate", "xxx");

        GlobalGUIRoutines.setTheme(this, true, true/*, false*/, true);
        //GlobalGUIRoutines.setLanguage(this);

    // set window dimensions - not needed, Activator uses Dialog theme ------------------------------

    /*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        int actionBarHeight;

        // display dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float popupWidth = displayMetrics.widthPixels;
        float popupMaxHeight = displayMetrics.heightPixels;
        //Display display = getWindowManager().getDefaultDisplay();
        //float popupWidth = display.getWidth();
        //popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true))
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

        boolean applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout(getApplicationContext());

        // add header height
        //if (ApplicationPreferences.applicationActivatorHeader(getApplicationContext())) {
            if (!applicationActivatorGridLayout)
                popupHeight = popupHeight + 50f * scale;
            else
                popupHeight = popupHeight + 59f * scale;
        //}

        // add toolbar height
        popupHeight = popupHeight + (25f + 1f + 3f) * scale;

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
        int profileCount = DatabaseHandler.getInstance(getApplicationContext()).getProfilesCount(true);
        dataWrapper.invalidateDataWrapper();

        if (profileCount > 0) {
            if (!applicationActivatorGridLayout) {
                // add list items height
                popupHeight = popupHeight + (60f * scale * profileCount); // item
                popupHeight = popupHeight + (1f * scale * (profileCount)); // divider

                popupHeight = popupHeight + (20f * scale); // listView padding
            } else {
                // add grid items height
                int modulo = profileCount % 3;
                profileCount = profileCount / 3;
                if (modulo > 0)
                    ++profileCount;
                popupHeight = popupHeight + (85f * scale * profileCount); // item
                popupHeight = popupHeight + (1f * scale * (profileCount - 1)); // divider

                popupHeight = popupHeight + (24f * scale); // gridView margin
            }
        }
        else
            popupHeight = popupHeight + 60f * scale; // for empty TextView

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));
    */

    //-----------------------------------------------------------------------------------

        //Debug.startMethodTracing("phoneprofilesplus");

    // Layout ---------------------------------------------------------------------------------

        //requestWindowFeature(Window.FEATURE_ACTION_BAR);

        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        setContentView(R.layout.activity_activate_profile);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (showNotStartedToast()) {
            finish();
            return;
        }
        else
        if (doServiceStart) {
            finish();
            return;
        }

        activityStarted = true;

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onCreate - setContentView");

        toolbar = findViewById(R.id.act_prof_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.title_activity_activator);

        eventsRunStopIndicator = findViewById(R.id.act_prof_run_stop_indicator);
        TooltipCompat.setTooltipText(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title));
        eventsRunStopIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFinishing()) {
                    RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), ActivateProfileActivity.this);

                    View contentView = popup.getContentView();
                    contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int popupWidth = contentView.getMeasuredWidth();
                    //int popupHeight = contentView.getMeasuredHeight();
                    //Log.d("ActivateProfileActivity.eventsRunStopIndicator.onClick","popupWidth="+popupWidth);
                    //Log.d("ActivateProfileActivity.eventsRunStopIndicator.onClick","popupHeight="+popupHeight);

                    int[] runStopIndicatorLocation = new int[2];
                    eventsRunStopIndicator.getLocationOnScreen(runStopIndicatorLocation);
                    //eventsRunStopIndicator.getLocationInWindow(runStopIndicatorLocation);

                    int x = 0;
                    int y = 0;

                    if (runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth < 0)
                        x = -(runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth);

                    popup.setClippingEnabled(false); // disabled for draw outside activity
                    popup.showOnAnchor(eventsRunStopIndicator, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                            RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
                }
            }
        });

        getApplicationContext().registerReceiver(finishBroadcastReceiver, new IntentFilter(PPApplication.ACTION_FINISH_ACTIVITY));
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }

        //PPApplication.logE("ActivateProfileActivity.onStart", "xxx");

        if (activityStarted) {
            Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
            intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "editor");
            getApplicationContext().sendBroadcast(intent);

            LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivatorGUIBroadcastReceiver"));
            LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ShowActivatorTargetHelpsBroadcastReceiver"));

            refreshGUI(/*true,*/ false);
        }
        else {
            if (!isFinishing())
                finish();
        }

        //-----------------------------------------------------------------------------------------

    }

    private boolean showNotStartedToast() {
        //PPApplication.logE("[APP START] ActivateProfileActivity.showNotStartedToast", "xxx");
        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced)*/;
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        if (!fullyStarted) {
            if ((PPApplication.startTimeOfApplicationStart > 0) &&
                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
                // clear all opened activities
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
            else {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            return true;
        }
        /*//boolean fullyStarted = true;
        if (applicationStarted) {
            //PhoneProfilesService instance = PhoneProfilesService.getInstance();
            //fullyStarted = instance.getApplicationFullyStarted();
            boolean fullyStarted = PPApplication.applicationFullyStarted;
            applicationStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);
        }
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            boolean fullyStarted = PPApplication.applicationFullyStarted;
            if (!fullyStarted)
                text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }*/
        return false;
    }

    private boolean startPPServiceWhenNotStarted() {
        // this is for list widget header
        if (!PPApplication.getApplicationStarted(true)) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("EditorProfilesActivity.onStart", "application is not started");
                PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
            }*/
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            PPApplication.startPPService(this, serviceIntent/*, true*/);
            return true;
        } else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EditorProfilesActivity.onStart", "application is started");
                    PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
                }*/
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.startPPService(this, serviceIntent/*, true*/);
                return true;
            }
            //else {
            //    PPApplication.logE("EditorProfilesActivity.onStart", "application and service is started");
            //}
        }

        return false;
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        if (targetHelpsSequenceStarted) {
            if (ActivatorTargetHelpsActivity.activity != null)
                ActivatorTargetHelpsActivity.activity.finish();
            targetHelpsSequenceStarted = false;
        }
    }
    */

    @Override
    protected void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);

        if (targetHelpsSequenceStarted) {
            if (ActivatorTargetHelpsActivity.activity != null)
                ActivatorTargetHelpsActivity.activity.finish();
            ActivatorTargetHelpsActivity.activity = null;
            targetHelpsSequenceStarted = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            getApplicationContext().unregisterReceiver(finishBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbar.inflateMenu(R.menu.activator_top_bar);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        // change global events run/stop menu item title
        MenuItem menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(Event.getGlobalEventsRunning());
            menuItem.setEnabled(PPApplication.getApplicationStarted(true));
        }

        return ret;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_edit_profiles) {
            Intent intent = new Intent(getApplicationContext(), EditorProfilesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
            getApplicationContext().startActivity(intent);

            finish();

            return true;
        }
        else
        if (itemId == R.id.menu_restart_events) {
            DataWrapper dataWrapper = getDataWrapper();
            if (dataWrapper != null) {
                //dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                // ignore manual profile activation
                // and unblock forceRun events
                //PPApplication.logE("$$$ restartEvents", "from ActivateProfileActivity.onOptionsItemSelected menu_restart_events");
                dataWrapper.restartEventsWithAlert(this);
            }
            return true;
        }
        else {
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

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile && (getDataWrapper() != null)) {
                    Profile profile = getDataWrapper().getProfileById(profileId, false, false, mergedProfile);
                    getDataWrapper().activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    private void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons)
    {
        //runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
                setEventsRunStopIndicator();
                invalidateOptionsMenu();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);

                if (fragment != null) {
                    ((ActivateProfileListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons);
                }
        //    }
        //});
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
        if (fragment != null)
            return ((ActivateProfileListFragment)fragment).activityDataWrapper;
        else
            return null;
    }

    public void setEventsRunStopIndicator()
    {
        //boolean whiteTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true).equals("white");
        if (Event.getGlobalEventsRunning())
        {
            //if (ApplicationPreferences.prefEventsBlocked) {
            if (Event.getEventsBlocked(this.getApplicationContext())) {
                //if (whiteTheme)
                //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation_white);
                //else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
            }
            else {
                //if (whiteTheme)
                //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running_white);
                //else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
            }
        }
        else {
            //if (whiteTheme)
            //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stopped_white);
            //else
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stopped);
        }
    }

    public void startTargetHelpsActivity() {
        if (ApplicationPreferences.prefActivatorActivityStartTargetHelps ||
                ApplicationPreferences.prefActivatorFragmentStartTargetHelps ||
                ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.d("ActivateProfilesActivity.startTargetHelpsActivity", "xxx");

            //ActivatorTargetHelpsActivity.activatorActivity = this;
            Intent intent = new Intent(this, ActivatorTargetHelpsActivity.class);
            startActivity(intent);
        }
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        boolean startTargetHelps = ApplicationPreferences.prefActivatorActivityStartTargetHelps;

        if (startTargetHelps ||
                ApplicationPreferences.prefActivatorFragmentStartTargetHelps ||
                ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (startTargetHelps) {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();
                ApplicationPreferences.prefActivatorActivityStartTargetHelps = false;

                //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
                //boolean tintTarget = !appTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);
                List<TapTarget> targets = new ArrayList<>();
                //noinspection IfStatementWithIdenticalBranches
                if (Event.getGlobalEventsRunning()) {
                    int id = 1;
                    try {
                        View editorActionView = toolbar.findViewById(R.id.menu_edit_profiles);
                        targets.add(
                                TapTarget.forView(editorActionView, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_ppp))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                    try {
                        View restartEventsActionView = toolbar.findViewById(R.id.menu_restart_events);
                        targets.add(
                                TapTarget.forView(restartEventsActionView, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }

                    sequence.targets(targets);
                }
                else {
                    int id = 1;
                    try {
                        View editorActionView = toolbar.findViewById(R.id.menu_edit_profiles);
                        targets.add(
                                TapTarget.forView(editorActionView, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_ppp))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }

                    sequence.targets(targets);
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        targetHelpsSequenceStarted = false;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            ((ActivateProfileListFragment)fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                        final Handler handler = new Handler(getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileActivity.showTargetHelps (1)");

                                if (ActivatorTargetHelpsActivity.activity != null) {
                                    //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                                    try {
                                        ActivatorTargetHelpsActivity.activity.finish();
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                    ActivatorTargetHelpsActivity.activity = null;
                                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                                }
                            }
                        }, 500);

                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.apply();
                        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = false;
                        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                //final Context context = getApplicationContext();
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileActivity.showTargetHelps (2)");

                        PPApplication.logE("[LOCAL_BROADCAST_CALL] ActivateProfileActivity.showTargetHelps", "xxx");
                        Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".ShowActivatorTargetHelpsBroadcastReceiver");
                        intent.putExtra(ActivateProfileActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, false);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        /*if (ActivateProfileActivity.getInstance() != null) {
                            Fragment fragment = ActivateProfileActivity.getInstance().getFragmentManager().findFragmentById(R.id.activate_profile_list);
                            if (fragment != null) {
                                ((ActivateProfileListFragment) fragment).showTargetHelps();
                            }
                        }*/
                    }
                }, 500);
            }
        }
        else {
            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileActivity.showTargetHelps (3)");

                    if (ActivatorTargetHelpsActivity.activity != null) {
                        //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                        try {
                            ActivatorTargetHelpsActivity.activity.finish();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                        ActivatorTargetHelpsActivity.activity = null;
                        //ActivatorTargetHelpsActivity.activatorActivity = null;
                    }
                }
            }, 500);
        }
    }

}
