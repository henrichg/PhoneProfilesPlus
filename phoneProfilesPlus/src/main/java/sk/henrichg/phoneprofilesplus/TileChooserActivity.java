package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** @noinspection ExtractMethodRecommender*/
public class TileChooserActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    int tileId = 0;

    static final String EXTRA_TILE_ID = "tile_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, true, false, false, true, false, false, false);

        super.onCreate(savedInstanceState);
        // animation
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] TileChooserActivity.onCreate", "xxx");

        //GlobalGUIRoutines.setLanguage(this);

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

        Intent intent = getIntent();
        tileId = intent.getIntExtra(EXTRA_TILE_ID, 0);

    // set window dimensions ----------------------------------------------------------

    /*    getWindow().setFlags(LayoutParams.FLAG_DIM_BEHIND, LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float popupWidth = displaymetrics.widthPixels;
        float popupMaxHeight = displaymetrics.heightPixels;
        //Display display = getWindowManager().getDefaultDisplay();
        //float popupWidth = display.getWidth();
        //float popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        float actionBarHeight = 0;

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
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
        }
        popupMaxHeight = popupMaxHeight / 100f * 90f;

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add list items height
        int profileCount = DatabaseHandler.getInstance(getApplicationContext()).getProfilesCount();
        ++profileCount; // for restart events
        if (profileCount > 0) {
            popupHeight = popupHeight + (60f * scale * profileCount); // item
            popupHeight = popupHeight + (1f * scale * (profileCount - 1)); // divider
        }
        else
            popupHeight = popupHeight + 60f * scale; // for empty TextView

        popupHeight = popupHeight + (20f * scale); // listview padding

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));

    */
    //-----------------------------------------------------------------------------------

        setContentView(R.layout.activity_tile_chooser);

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        /*if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_tile_chooser);
            getSupportActionBar().setElevation(0);
        }*/
        setTitle(R.string.title_activity_tile_chooser);

        setTitle(R.string.title_activity_tile_chooser);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
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

        if (!(activityStarted && (tileId != 0))) {
            if (!isFinishing())
                finish();
        } else {
            Permissions.grantNotificationsPermission(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Permissions.NOTIFICATIONS_PERMISSION_REQUEST_CODE)
        {
            Context appContext = getApplicationContext();
            ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(appContext, PhoneProfilesService.class);
            if (serviceInfo == null)
                startPPServiceWhenNotStarted();
            else {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ActivatorActivity.onActivityResult", "call of PPAppNotification.drawNotification");
                ImportantInfoNotification.showInfoNotification(appContext);
                ProfileListNotification.drawNotification(true, appContext);
                //DrawOverAppsPermissionNotification.showNotification(appContext, true);
                IgnoreBatteryOptimizationNotification.showNotification(appContext, true);
                DNDPermissionNotification.showNotification(appContext, true);
                PPAppNotification.drawNotification(true, appContext);
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
//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] TileChooserActivity.showNotStartedToast", "xxx");
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
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_cannot_be_started);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
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
        Context appContext = getApplicationContext();
        if (PPApplicationStatic.getApplicationStopping(appContext)) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_stopping_toast);
            PPApplication.showToast(appContext, text, Toast.LENGTH_SHORT);
            return true;
        }

        boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
        if (!serviceStarted) {

            //AutostartPermissionNotification.showNotification(appContext, true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplicationStatic.setApplicationStarted(appContext, true);
            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, false);
//            PPApplicationStatic.logE("[START_PP_SERVICE] TileChooserActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplicationStatic.startPPService(this, serviceIntent, true);
            //return true;
        } /*else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }*/

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
    }

}
