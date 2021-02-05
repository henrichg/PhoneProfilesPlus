package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

//import me.drakeet.support.toast.ToastCompat;

public class LauncherActivity extends AppCompatActivity {

    private boolean activityStarted = false;
    private int startupSource;

    private static final int REQUEST_CODE_IMPORTANT_INFO = 1620;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] LauncherActivity.onCreate", "xxx");

        boolean doServiceStart = startPPServiceWhenNotStarted();
//        PPApplication.logE("LauncherActivity.onCreate", "doServiceStart="+doServiceStart);
//        if (showNotStartedToast()) {
//            PPApplication.logE("LauncherActivity.onCreate", "showNotStartedToast()=true");
//            finish();
//            return;
//        }
//        else
        if (doServiceStart) {
            finish();
            return;
        }

        activityStarted = true;
//        PPApplication.logE("LauncherActivity.onCreate", "activityStarted="+activityStarted);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
        //PPApplication.logE("LauncherActivity.onCreate", "startupSource="+startupSource);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
//        PPApplication.logE("LauncherActivity.onStart", "doServiceStart="+doServiceStart);
//        if (showNotStartedToast()) {
//            PPApplication.logE("LauncherActivity.onStart", "showNotStartedToast()=true");
//            if (!isFinishing())
//                finish();
//            return;
//        }
//        else
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }

//        PPApplication.logE("LauncherActivity.onStart", "activityStarted="+activityStarted);

        if (activityStarted) {
            if (startupSource == 0) {
                // activity was not started from notification, widget

                //PPApplication.showProfileNotification(/*getApplicationContext()*/true, false);
                //PPApplication.logE("ActivateProfileHelper.updateGUI", "from LauncherActivity.onStart");
                //PPApplication.logE("###### PPApplication.updateGUI", "from=LauncherActivity.onStart");
                PPApplication.updateGUI(0/*getApplicationContext(), true, true*/);
                startupSource = PPApplication.STARTUP_SOURCE_LAUNCHER;
            }

            endOnStart();
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    private void endOnStart()
    {
        //  application is already started - is in PhoneProfilesService
        //PPApplication.setApplicationStarted(getBaseContext(), true);

        Intent intentLaunch;

//        PPApplication.logE("LauncherActivity.endOnStart", "startupSource="+startupSource);
        switch (startupSource) {
            case PPApplication.STARTUP_SOURCE_NOTIFICATION:
                //PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_NOTIFICATION");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationNotificationLauncher="+ApplicationPreferences.applicationNotificationLauncher);
                if (ApplicationPreferences.applicationNotificationLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            case PPApplication.STARTUP_SOURCE_WIDGET:
                //PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_WIDGET");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationWidgetLauncher="+ApplicationPreferences.applicationWidgetLauncher);
                if (ApplicationPreferences.applicationWidgetLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            default:
//                PPApplication.logE("LauncherActivity.endOnStart", "default");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationHomeLauncher="+ApplicationPreferences.applicationHomeLauncher);
                if (ApplicationPreferences.applicationHomeLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
        }

        //PPApplication.logE("LauncherActivity.endOnStart", "applicationFirstStart="+ApplicationPreferences.applicationFirstStart(getApplicationContext()));
        /*if (ApplicationPreferences.applicationFirstStart(getApplicationContext())) {
            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                editor.apply();
            }
            intentLaunch = new Intent(getApplicationContext(), ImportantInfoActivity.class);
            intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, true);
            startActivityForResult(intentLaunch, REQUEST_CODE_IMPORTANT_INFO);
        }
        else {*/
            finish();
            //PPApplication.sleep(100);

            //if (startupSource == PPApplication.STARTUP_SOURCE_NOTIFICATION)
            //    intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK /*| Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
            //else
                intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentLaunch.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            getApplicationContext().startActivity(intentLaunch);

            // reset startupSource
            startupSource = 0;
        //}
    }

//    private boolean showNotStartedToast() {
//        //PPApplication.logE("[APP_START] LauncherActivity.showNotStartedToast", "xxx");
//        boolean applicationStarted = PPApplication.getApplicationStarted(true);
//        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced)*/;
//        if (!applicationStarted) {
//            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
//            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            return true;
//        }
//        if (!fullyStarted) {
//            //Log.e("LauncherActivity.showNotStartedToast", "PPApplication.startTimeOfApplicationStart="+PPApplication.startTimeOfApplicationStart);
//            //Log.e("LauncherActivity.showNotStartedToast", "PPApplication.APPLICATION_START_DELAY="+PPApplication.APPLICATION_START_DELAY);
//            //Log.e("LauncherActivity.showNotStartedToast", "delta="+(Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart));
//            if ((PPApplication.startTimeOfApplicationStart > 0) &&
//                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
//                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
//                // clear all opened activities
//                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(activityIntent);
//            }
//            else {
//                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
//                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            }
//            return true;
//        }
//        return false;
//    }

    private boolean startPPServiceWhenNotStarted() {
        // this is for list widget header

        boolean serviceStarted = PhoneProfilesService.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
//            if (PPApplication.logEnabled()) {
//                PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "application is not started");
//                PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "service instance=" + PhoneProfilesService.getInstance());
//                if (PhoneProfilesService.getInstance() != null)
//                    PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
//            }
            // start PhoneProfilesService
//            PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "start PPService");
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplication.logE("[START_PP_SERVICE] LauncherActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplication.startPPService(this, serviceIntent);
            return true;
        } else {
            //noinspection RedundantIfStatement
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
//                if (PPApplication.logEnabled()) {
//                    PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "application is started");
//                    PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "service instance=" + PhoneProfilesService.getInstance());
//                    if (PhoneProfilesService.getInstance() != null)
//                        PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
//                }

                // start PhoneProfilesService
//                PPApplication.logE("LauncherActivity.startPPServiceWhenNotStarted", "start PPService");
                //PPApplication.firstStartServiceStarted = false;

                /*
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.logE("[START_PP_SERVICE] LauncherActivity.startPPServiceWhenNotStarted", "(2)");
                PPApplication.startPPService(this, serviceIntent);
                */

                return true;
            }
            //else {
            //    PPApplication.logE("EditorProfilesActivity.onStart", "application and service is started");
            //}
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORTANT_INFO)
        {
            endOnStart();
        }
    }

}
