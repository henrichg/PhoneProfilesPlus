package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

public class ActivatorActivity extends AppCompatActivity
                                implements RefreshGUIActivatorEditorListener,
                                           ShowTargetHelpsActivatorEditorListener,
                                           FinishActivityActivatorEditorListener
{

    private boolean activityStarted = false;
    boolean firstStartOfPPP = false;
    boolean privacyPolicyDisplayed = false;

    private Toolbar toolbar;
    private ImageView eventsRunStopIndicator;

    static final String ACTION_SHOW_ACTIVATOR_TARGET_HELPS_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ShowActivatorTargetHelpsBroadcastReceiver";

    //boolean targetHelpsSequenceStarted;

    static private class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

        private final RefreshGUIActivatorEditorListener listener;

        public RefreshGUIBroadcastReceiver(RefreshGUIActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.refreshGUIFromListener(intent);
        }
    }
    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver(this);

    static final String EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY = "show_target_helps_for_activity";
    static private class ShowTargetHelpsBroadcastReceiver extends BroadcastReceiver {
        private final ShowTargetHelpsActivatorEditorListener listener;

        public ShowTargetHelpsBroadcastReceiver(ShowTargetHelpsActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.showTargetHelpsFromListener(intent);
        }
    }
    private ShowTargetHelpsBroadcastReceiver showTargetHelpsBroadcastReceiver = new ShowTargetHelpsBroadcastReceiver(this);

    static private class FinishActivityBroadcastReceiver extends BroadcastReceiver {
        private final FinishActivityActivatorEditorListener listener;

        public FinishActivityBroadcastReceiver(FinishActivityActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.finishActivityFromListener(intent);
        }
    }
    private FinishActivityBroadcastReceiver finishBroadcastReceiver = new FinishActivityBroadcastReceiver(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, true, true, true, false, false, false);

        super.onCreate(savedInstanceState);

        firstStartOfPPP = PPApplicationStatic.getSavedVersionCode(getApplicationContext()) == 0;

        //GlobalGUIRoutines.setLanguage(this);

    //-----------------------------------------------------------------------------------

        //Debug.startMethodTracing("phoneprofilesplus");

    // Layout ---------------------------------------------------------------------------------

        //requestWindowFeature(Window.FEATURE_ACTION_BAR);

        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        setContentView(R.layout.activity_activator);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivatorActivity.onCreate - setContentView");

        if (ApplicationPreferences.applicationActivatorIncreaseBrightness) {
            PPApplication.brightnessInternalChange = true;
            Window win = getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
//            int actualBightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            int actualBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            //if (actualBightnessMode != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            if (actualBrightness <
                    ProfileStatic.convertPercentsToBrightnessManualValue(15, getApplicationContext())) {
                layoutParams.screenBrightness = ProfileStatic.convertPercentsToBrightnessManualValue(35, getApplicationContext()) / (float) 255;
                win.setAttributes(layoutParams);
            }
            //}
        }

        toolbar = findViewById(R.id.act_prof_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.title_activity_activator);

        eventsRunStopIndicator = findViewById(R.id.act_prof_run_stop_indicator);
        TooltipCompat.setTooltipText(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title));
        eventsRunStopIndicator.setOnClickListener(view -> {
            if (!isFinishing()) {
                RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), ActivatorActivity.this);

                View contentView = popup.getContentView();
                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = contentView.getMeasuredWidth();
                //int popupHeight = contentView.getMeasuredHeight();
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupWidth="+popupWidth);
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupHeight="+popupHeight);

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
        });

        int receiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            receiverFlags = RECEIVER_NOT_EXPORTED;
        getApplicationContext().registerReceiver(finishBroadcastReceiver, new IntentFilter(PPApplication.ACTION_FINISH_ACTIVITY), receiverFlags);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (activityStarted) {
            // this is for API 33+
            if (!Permissions.grantNotificationsPermission(this)) {
                Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
                intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, StringConstants.EXTRA_EDITOR);
                getApplicationContext().sendBroadcast(intent);

                LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                        new IntentFilter(PPApplication.ACTION_REFRESH_ACTIVATOR_GUI_BROADCAST_RECEIVER));
                LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                        new IntentFilter(ActivatorActivity.ACTION_SHOW_ACTIVATOR_TARGET_HELPS_BROADCAST_RECEIVER));

                refreshGUI(/*true,*/ false);

                showPrivacyPolicy();
            }
        }
        else {
            if (!isFinishing())
                finish();
        }

        //-----------------------------------------------------------------------------------------

    }

    private void showPrivacyPolicy() {
        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            if (firstStartOfPPP && (!privacyPolicyDisplayed)) {
                String url = PPApplication.PRIVACY_POLICY_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.privacy_policy_web_browser_chooser)));
                    privacyPolicyDisplayed = true;
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Permissions.NOTIFICATIONS_PERMISSION_REQUEST_CODE)
        {
            ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(getApplicationContext(), PhoneProfilesService.class);
            if (serviceInfo == null)
                startPPServiceWhenNotStarted();
            else {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ActivatorActivity.onActivityResult", "call of PPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(true, getApplicationContext());
                DrawOverAppsPermissionNotification.showNotification(getApplicationContext(), true);
                IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext(), true);
                PPAppNotification.drawNotification(true, getApplicationContext());
            }

            //!!!! THIS IS IMPORTANT BECAUSE WITHOUT THIS IS GENERATED CRASH
            //  java.lang.NullPointerException: Attempt to invoke virtual method 'void android.content.BroadcastReceiver.onReceive(android.content.Context, android.content.Intent)'
            //  on a null object reference
            //  at androidx.localbroadcastmanager.content.LocalBroadcastManager.executePendingBroadcasts(LocalBroadcastManager.java:313)
            finish();
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
        PPApplicationStatic.setApplicationFullyStarted(getApplicationContext());
//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] ActivatorActivity.showNotStartedToast", "xxx");
        return false;
/*        boolean applicationStarted = PPApplicationStatic.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted;
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
        return false;*/
    }

    @SuppressWarnings("SameReturnValue")
    private boolean startPPServiceWhenNotStarted() {
        if (PPApplicationStatic.getApplicationStopping(getApplicationContext())) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_stopping_toast);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }

        boolean serviceStarted = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
            //AutostartPermissionNotification.showNotification(getApplicationContext(), true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplicationStatic.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplicationStatic.logE("[START_PP_SERVICE] ActivatorActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplicationStatic.startPPService(this, serviceIntent, true);
            //return true;
        }/* else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }*/

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
        PPApplication.brightnessInternalChange = false;
        PPExecutors.scheduleDisableBrightnessInternalChangeExecutor();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        refreshGUIBroadcastReceiver = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);
        showTargetHelpsBroadcastReceiver = null;

        //if (targetHelpsSequenceStarted) {
            if (ActivatorTargetHelpsActivity.activity != null)
                ActivatorTargetHelpsActivity.activity.finish();
            ActivatorTargetHelpsActivity.activity = null;
            //targetHelpsSequenceStarted = false;
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            getApplicationContext().unregisterReceiver(finishBroadcastReceiver);
        } catch (Exception e) {
            //PPApplicationStatic.recordException(e);
        }
        finishBroadcastReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
            menuItem.setVisible(EventStatic.getGlobalEventsRunning(this));
            menuItem.setEnabled(PPApplicationStatic.getApplicationStarted(true, false));
        }

        return ret;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_edit_profiles) {
            // For Android 14 is better to finish Activator after start of Editor.
            // Because of bad animatation in Pixel 6, Android 14 beta.

            //finish();

            Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
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
                    ((ActivatorListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons);
                }
        //    }
        //});
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
        if (fragment != null)
            return ((ActivatorListFragment)fragment).activityDataWrapper;
        else
            return null;
    }

    void setEventsRunStopIndicator()
    {
        //boolean whiteTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true).equals("white");
        if (EventStatic.getGlobalEventsRunning(this))
        {
            //if (ApplicationPreferences.prefEventsBlocked) {
            if (EventStatic.getEventsBlocked(this.getApplicationContext())) {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_manual_activation);
            }
            else {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_running);
            }
        }
        else {
            eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_stopped);
        }
    }

    void startTargetHelpsActivity() {
        if (ApplicationPreferences.prefActivatorActivityStartTargetHelps ||
                ApplicationPreferences.prefActivatorFragmentStartTargetHelps ||
                ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.e("ActivatorActivity.startTargetHelpsActivity", "xxx");

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
                editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS, false);
                editor.apply();
                ApplicationPreferences.prefActivatorActivityStartTargetHelps = false;

                //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int titleTextColor = R.color.tabTargetHelpTitleTextColor;
                int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
                //boolean tintTarget = !appTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);
                List<TapTarget> targets = new ArrayList<>();
                if (EventStatic.getGlobalEventsRunning(this)) {
                    int id = 1;
                    try {
                        View editorActionView = toolbar.findViewById(R.id.menu_edit_profiles);
                        targets.add(
                                TapTarget.forView(editorActionView, getString(R.string.activator_activity_targetHelps_editor_title), getString(R.string.activator_activity_targetHelps_editor_description_ppp))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                    try {
                        View restartEventsActionView = toolbar.findViewById(R.id.menu_restart_events);
                        targets.add(
                                TapTarget.forView(restartEventsActionView, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
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
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }

                    sequence.targets(targets);
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;

                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS_FINISHED, true);
                        editor.apply();
                        ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished = true;

                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null)
                        {
                            //Log.e("ActivatorActivity.showTargetHelps", "start fragment showTargetHelps");
                            ((ActivatorListFragment)fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;

                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS, false);
                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_LIST_FRAGMENT_START_TARGET_HELPS, false);
                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_LIST_ADAPTER_START_TARGET_HELPS, false);

                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS_FINISHED, true);
                        editor.putBoolean(PPApplication.PREF_ACTIVATOR_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                        //editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                        editor.apply();

                        ApplicationPreferences.prefActivatorActivityStartTargetHelps = false;
                        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = false;
                        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = false;

                        ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished = true;
                        ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = true;
                        //ApplicationPreferences.prefActivatorAdapterStartTargetHelpsFinished = true;

                        final Handler handler = new Handler(getMainLooper());
                        handler.postDelayed(() -> {
//                                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorActivity.showTargetHelps (1)");

                            if (ActivatorTargetHelpsActivity.activity != null) {
                                //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                                try {
                                    ActivatorTargetHelpsActivity.activity.finish();
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                                ActivatorTargetHelpsActivity.activity = null;
                                //ActivatorTargetHelpsActivity.activatorActivity = null;
                            }
                        }, 500);
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;

                editor = ApplicationPreferences.getEditor(getApplicationContext());
                editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS_FINISHED, false);
                editor.apply();
                ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished = false;

                sequence.start();
            }
            else {
                //Log.d("ActivateProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                //final Context context = getApplicationContext();
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorActivity.showTargetHelps (2)");

//                    PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] ActivatorActivity.showTargetHelps", "xxx");
                    Intent intent = new Intent(ACTION_SHOW_ACTIVATOR_TARGET_HELPS_BROADCAST_RECEIVER);
                    intent.putExtra(ActivatorActivity.EXTRA_SHOW_TARGET_HELPS_FOR_ACTIVITY, false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    /*if (ActivatorActivity.getInstance() != null) {
                        Fragment fragment = ActivatorActivity.getInstance().getFragmentManager().findFragmentById(R.id.activate_profile_list);
                        if (fragment != null) {
                            ((ActivatorListFragment) fragment).showTargetHelps();
                        }
                    }*/
                }, 500);
            }
        }
        else {
            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorActivity.showTargetHelps (3)");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivateProfilesActivity.showTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                }
            }, 500);
        }
    }

    @Override
    public void refreshGUIFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ActivatorActivity.refreshGUIBroadcastReceiver", "xxx");
        //boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);

        if (intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_RELOAD_ACTIVITY, false))
            GlobalGUIRoutines.reloadActivity(this, true);
        else {
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            refreshGUI(refreshIcons);
        }
    }

    @Override
    public void showTargetHelpsFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ActivatorActivity.showTargetHelpsBroadcastReceiver", "xxx");
        if (isFinishing()) {
            if (ActivatorTargetHelpsActivity.activity != null)
                ActivatorTargetHelpsActivity.activity.finish();
            ActivatorTargetHelpsActivity.activity = null;
            return;
        }
        if (isDestroyed()) {
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
                showTargetHelps();
            else {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activate_profile_list);
                if (fragment != null) {
                    //Log.e("ActivatorActivity.showTargetHelpsFromListener", "start fragment showTargetHelps");
                    ((ActivatorListFragment) fragment).showTargetHelps();
                }
            }
        }
        else {
            if (ActivatorTargetHelpsActivity.activity != null)
                ActivatorTargetHelpsActivity.activity.finish();
            ActivatorTargetHelpsActivity.activity = null;
        }
    }

    @Override
    public void finishActivityFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ActivatorActivity.finishBroadcastReceiver", "xxx");
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(PPApplication.ACTION_FINISH_ACTIVITY)) {
                String what = intent.getStringExtra(PPApplication.EXTRA_WHAT_FINISH);
                if (what.equals(StringConstants.EXTRA_ACTIVATOR)) {
                    try {
                        setResult(Activity.RESULT_CANCELED);
                        finishAffinity();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }
    }

}
