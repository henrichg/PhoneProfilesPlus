package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ActivateProfileFromExternalApplicationActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private String profileName;
    private long profile_id = 0;

    static final String EXTRA_PROFILE_NAME = "profile_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("ActivateProfileFromExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();
        profileName = intent.getStringExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        if (profileName != null) {
            profileName = profileName.trim();
            //Log.e("ActivateProfileFromExternalApplicationActivity.onCreate", "profileName="+profileName);

            if (!profileName.isEmpty()) {
                profile_id = dataWrapper.getProfileIdByName(profileName, true);
                /*dataWrapper.fillProfileList(false, false);
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._name.trim().equals(profileName)) {
                        profile_id = profile._id;
                        break;
                    }
                }*/
                //Log.e("ActivateProfileFromExternalApplicationActivity.onCreate", "profile_id="+profile_id);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean serviceStarted = PhoneProfilesService.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
//            PPApplication.logE("ActivateProfileFromExternalApplicationActivity.onStart", "application not started");

            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            if (profile_id != 0) {
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION,
                        ActionForExternalApplicationActivity.ACTION_ACTIVATE_PROFILE);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE,
                        PhoneProfilesService.START_FOR_EXTERNAL_APP_PROFILE);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, profileName);
            }
//            PPApplication.logE("[START_PP_SERVICE] ActivateProfileFromExternalApplicationActivity.onStart", "xxx");
            PPApplication.startPPService(this, serviceIntent);
            finish();
            return;
        }

//        PPApplication.logE("ActivateProfileFromExternalApplicationActivity.onStart", "application started");

        PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION,
                null, profileName, null, 0, "");

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id, false, false, false);
            if (profile != null) {

//                PPApplication.logE("ActivateProfileFromExternalApplicationActivity.onCreate", "profile=" + profile._name);
                //if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                //        /*false, false, 0,*/ PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, true, false)) {
                if (!PhoneProfilesService.displayPreferencesErrorNotification(profile, null, getApplicationContext())) {
//                    PPApplication.logE("&&&&&&& ActivateProfileFromExternalApplicationActivity.onStart", "called is DataWrapper.activateProfileFromMainThread");
                    dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this, false);
                } else
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            }
            else
                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_profile_text));

            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
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

    private void showNotification(String title, String text) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = getString(R.string.ppp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createExclamationNotificationChannel(getApplicationContext());
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        try {
            mNotificationManager.notify(
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG,
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("ActivateProfileFromExternalApplicationActivity.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
