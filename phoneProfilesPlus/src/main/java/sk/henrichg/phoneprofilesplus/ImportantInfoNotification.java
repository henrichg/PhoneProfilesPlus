package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class ImportantInfoNotification {

    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START = "show_info_notification_on_start";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION = "show_info_notification_on_start_version";

    static final String EXTRA_FIRST_INSTALLATION = "first_installation";

    static void showInfoNotification(Context context) {
        try {
            // test if notifications are enabled
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (!notificationManager.areNotificationsEnabled())
                    // not enabled, do notthing in this
                    return;
            }

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            int packageVersionCode = PPApplicationStatic.getVersionCode(pInfo);
            int savedVersionCode = getShowInfoNotificationOnStartVersion(context);

            // show notification for display Quick giude, when version code is not saved
            // typically it is for new users
            if (savedVersionCode == 0) {
                setShowInfoNotificationOnStart(context, false, packageVersionCode);

                showNotification(context, /*false,*/ true,
                        context.getString(R.string.info_notification_title),
                        context.getString(R.string.info_notification_text),
                        PPApplication.IMPORTANT_INFO_NOTIFICATION_TAG);
                return;
            }

//            Log.e("ImportantInfoNotification.canShowInfoNotification", "packageVersionCode="+packageVersionCode);
//            Log.e("ImportantInfoNotification.canShowInfoNotification", "savedVersionCode="+savedVersionCode);

            boolean showInfo = false;
            boolean showExtender = false;
            boolean showPPPPS = false;
            if (packageVersionCode > savedVersionCode) {
                // show notification only when is new version of package, not saved
                // result = notification will be displayed only once
                showInfo = canShowInfoNotification(packageVersionCode, savedVersionCode);
            }
            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
            if ((extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED))
                showExtender = true;

            int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(context);
            if ((ppppsVersion != 0) && (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_REQUIRED))
                showPPPPS = true;

            //Log.e("ImportantInfoNotification.showInfoNotification", "showInfo="+showInfo);
            //Log.e("ImportantInfoNotification.showInfoNotification", "showExtender="+showExtender);
            //Log.e("ImportantInfoNotification.showInfoNotification", "showPPPPS="+showPPPPS);

            // Save package version, in future notification will be displayed only when pacakage
            // version will be changed.
            // But this may be disabled by PPApplication.SHOW_IMPORTANT_INFO_NOTIFICATION_NEWS = false.
            setShowInfoNotificationOnStart(context, showInfo || showExtender || showPPPPS, packageVersionCode);

            if (/*(savedVersionCode == 0) ||*/ getShowInfoNotificationOnStart(context, packageVersionCode)) {

                if (showInfo)
                    showNotification(context, /*false,*/ false,
                            context.getString(R.string.info_notification_title),
                            context.getString(R.string.info_notification_text),
                            PPApplication.IMPORTANT_INFO_NOTIFICATION_TAG);
                if (showExtender)
                    showNotification(context, /*false,*/ false,
                            context.getString(R.string.info_notification_title),
                            context.getString(R.string.important_info_accessibility_service_new_version_notification),
                            PPApplication.IMPORTANT_INFO_NOTIFICATION_EXTENDER_TAG);
                if (showPPPPS)
                    showNotification(context, /*false,*/ false,
                            context.getString(R.string.info_notification_title),
                            context.getString(R.string.important_info_pppps_new_version_notification),
                            PPApplication.IMPORTANT_INFO_NOTIFICATION_PPPPS_TAG);

                //setShowInfoNotificationOnStart(context, false, packageVersionCode);
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    static private boolean canShowInfoNotification(int packageVersionCode, int savedVersionCode) {
        boolean news = false;

        boolean newsLatest = (packageVersionCode >= PPApplication.PPP_VERSION_CODE_FOR_IMPORTANT_INFO_NEWS);
        //boolean news4550 = ((packageVersionCode >= 4550) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //boolean news4340 = ((packageVersionCode >= 4340) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //boolean news3670 = ((packageVersionCode >= 3670) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //boolean news1804 = ((packageVersionCode >= 1804) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        //boolean news1772 = ((packageVersionCode >= 1772) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));

        boolean afterInstall = savedVersionCode == 0;

//        Log.e("ImportantInfoNotification.canShowInfoNotification", "newsLatest="+newsLatest);
        if (newsLatest) {
            // change to false for not show notification

            // 7281 (Advanced protection) is only for Android 16
            news = PPApplication.SHOW_IMPORTANT_INFO_NOTIFICATION_NEWS && (Build.VERSION.SDK_INT >= 36);
        }

        /*if (news4550) {
            if (Build.VERSION.SDK_INT >= 28)
                news = true;
        }*/

        /*if (news4340) {
            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            dataWrapper.fillEventList();
            boolean sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS);
            if (!sensorExists)
                sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL);

            if (!sensorExists)
                news = false;
            else {
                news = true;
            }
        }*/

        /*if (news3670) {
            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            dataWrapper.fillEventList();
            boolean sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION);
            if (!sensorExists)
                sensorExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION);
            if (!sensorExists)
                news = false;
            else {
                news = true;
            }
        }*/

        /*if (news1804) {
            news = true;
        }*/

        /*if (news1772) {
            news = true;
        }*/

        if (afterInstall)
            news = true;

        //Log.e("ImportantInfoNotification.canShowInfoNotification", "news="+news);
        return news;
    }

    static private void showNotification(Context context,
                                         //boolean firstInstallation,
                                         boolean showQuickGuide,
                                         String title, String text,
                                         String notificationTag) {
        PPApplicationStatic.createExclamationNotificationChannel(context.getApplicationContext(), false);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.informationColor))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_information_notification))
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_FIRST_INSTALLATION, /*firstInstallation*/false);
        intent.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, showQuickGuide);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    notificationTag,
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_ID, mBuilder.build());
        } catch (SecurityException en) {
            PPApplicationStatic.logException("ImportantInfoNotification.showNotification", Log.getStackTraceString(en), false);
        } catch (Exception e) {
            //Log.e("ImportantInfoNotification.showNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_TAG,
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_ID);
            notificationManager.cancel(
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_EXTENDER_TAG,
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_ID);
            notificationManager.cancel(
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_PPPPS_TAG,
                    PPApplication.IMPORTANT_INFO_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    private static boolean getShowInfoNotificationOnStart(Context context, int version)
    {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        boolean show = preferences.getBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, true);
        int _version = preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        return ((_version >= version) && show);
    }

    static void setShowInfoNotificationOnStart(Context context, boolean show, int version)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, show);
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.apply();
    }

    private static int getShowInfoNotificationOnStartVersion(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, 0);
    }

    /*
    private static void setShowInfoNotificationOnStartVersion(Context context, int version)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.apply();
    }
    */
}
