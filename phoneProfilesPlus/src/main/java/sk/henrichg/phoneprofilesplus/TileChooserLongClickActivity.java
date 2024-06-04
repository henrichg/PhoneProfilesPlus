package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** @noinspection ExtractMethodRecommender*/
public class TileChooserLongClickActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    private int tileId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] BackgroundActivateProfileActivity.onCreate", "xxx");

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

            ComponentName qsTile = intent.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME);
            if (qsTile != null) {
                if (qsTile.getClassName().contains("PPTileService1"))
                    tileId = 1;
                else if (qsTile.getClassName().contains("PPTileService2"))
                    tileId = 2;
                else if (qsTile.getClassName().contains("PPTileService3"))
                    tileId = 3;
                else if (qsTile.getClassName().contains("PPTileService4"))
                    tileId = 4;
                else if (qsTile.getClassName().contains("PPTileService5"))
                    tileId = 5;
            }
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

        if (activityStarted && (tileId != 0)) {
            Intent intent = new Intent(getApplicationContext(), TileChooserActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
            intent.putExtra(TileChooserActivity.EXTRA_TILE_ID, tileId);
            startActivity(intent);
            finish();
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
        PPApplicationStatic.setApplicationFullyStarted(getApplicationContext());
//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] TileChooserLongClickActivity.showNotStartedToast", "xxx");
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

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false, mergedProfile);
                    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
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
//            PPApplicationStatic.logE("[START_PP_SERVICE] TileChooserLongClickActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplicationStatic.startPPService(this, serviceIntent, true);
            //return true;
        } /*else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }*/

        return false;
    }

}
