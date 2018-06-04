package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

class ImportantInfoNotification {

    // this version code must by <= version code in dependencies.gradle
    static final int VERSION_CODE_FOR_NEWS = 3985;

    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START = "show_info_notification_on_start";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION = "show_info_notification_on_start_version";

    static void showInfoNotification(Context context) {
        //Log.d("ImportantInfoNotification.showInfoNotification","xxx");
        int packageVersionCode = 0;
        int savedVersionCode = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            packageVersionCode = pInfo.versionCode;
            savedVersionCode = getShowInfoNotificationOnStartVersion(context);
            if ((packageVersionCode > savedVersionCode)/* || PPApplication.newExtender*/){
                //Log.d("ImportantInfoNotification.showInfoNotification","show");
                //boolean show = (versionCode >= VERSION_CODE_FOR_NEWS);
                boolean show = canShowNotification(packageVersionCode, savedVersionCode, context);
                setShowInfoNotificationOnStart(context, show, packageVersionCode);
            }
            else
                setShowInfoNotificationOnStartVersion(context, packageVersionCode);
        } catch (Exception ignored) {
        }

        if ((savedVersionCode == 0) || getShowInfoNotificationOnStart(context, packageVersionCode)) {

            showNotification(context,
                    context.getString(R.string.info_notification_title),
                    context.getString(R.string.info_notification_text));

            setShowInfoNotificationOnStart(context, false, packageVersionCode);
        }
    }

    static private boolean canShowNotification(int packageVersionCode, int savedVersionCode, Context context) {
        boolean news = false;

        boolean newsLatest = (packageVersionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        boolean news3670 = (packageVersionCode >= 3670); // news for PhoneProfilesPlusExtender - show it when not activated
        boolean news1804 = ((packageVersionCode >= 1804) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1772 = ((packageVersionCode >= 1772) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean afterInstall = savedVersionCode == 0;

        int extenderVersion = AccessibilityServiceBroadcastReceiver.isExtenderInstalled(context);
        int applicationSensorsCount = 0;
        int orientationSensorsCount = 0;
        if (extenderVersion == 0) {
            applicationSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION, false);
            orientationSensorsCount = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
        }

        if (newsLatest) {
            // change to false for not show notification
            news = true;
        }

        if (news3670) {
            //noinspection RedundantIfStatement
            if ((extenderVersion > 0) || ((applicationSensorsCount == 0) && (orientationSensorsCount == 0)))
                news = false; // Extender is installed or not needed
            else {
                news = true;
            }
        }

        if (news1804) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                news = true;
            }
        }

        if (news1772) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                news = true;
            }
        }

        if ((extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST))
            news = true;

        if (afterInstall)
            news = true;

        return news;
    }

    static private void showNotification(Context context, String title, String text) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = context.getString(R.string.app_name);
            nText = title+": "+text;
        }
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(PPApplication.IMPORTANT_INFO_NOTIFICATION_ID, mBuilder.build());
    }

    static void removeNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.IMPORTANT_INFO_NOTIFICATION_ID);
    }

    private static boolean getShowInfoNotificationOnStart(Context context, int version)
    {
        ApplicationPreferences.getSharedPreferences(context);
        boolean show = ApplicationPreferences.preferences.getBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, true);
        int _version = ApplicationPreferences.preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        return ((_version >= version) && show);
    }

    private static void setShowInfoNotificationOnStart(Context context, boolean show, int version)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, show);
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.apply();
    }

    private static int getShowInfoNotificationOnStartVersion(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, 0);
    }

    private static void setShowInfoNotificationOnStartVersion(Context context, int version)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.apply();
    }

}
