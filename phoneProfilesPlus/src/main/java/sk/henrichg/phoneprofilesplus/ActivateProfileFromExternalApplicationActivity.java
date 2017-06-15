package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class ActivateProfileFromExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private long profile_id = 0;

    static final String EXTRA_PROFILE_NAME = "profile_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        //Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();
        String profileName = intent.getStringExtra(EXTRA_PROFILE_NAME);

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

        if (profileName != null) {
            profileName = profileName.trim();
            //Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profileName="+profileName);

            if (!profileName.isEmpty()) {
                //PPApplication.loadPreferences(getApplicationContext());
                List<Profile> profileList = dataWrapper.getProfileList();
                for (Profile profile : profileList) {
                    if (profile._name.trim().equals(profileName)) {
                        profile_id = profile._id;
                        break;
                    }
                }
                //Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profile_id="+profile_id);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            startService(serviceIntent);
        }

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id, false);
            //Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profile="+profile);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                    true, false, 0, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this, true)) {
                dataWrapper._activateProfile(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this);
            }
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_profile_text));

            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    private void showNotification(String title, String text) {
        String ntitle = title;
        String ntext = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            ntitle = getString(R.string.app_name);
            ntext = title+": "+text;
        }
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(ntitle) // title for notification
                .setContentText(ntext) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ntext));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        if (android.os.Build.VERSION.SDK_INT >= 16)
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
    }

}
