package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

public class ImportantInfoNotification {

    // this version code must by <= version code in manifest
    public static final int VERSION_CODE_FOR_NEWS = 2000;

    static public void showInfoNotification(Context context) {
        PackageInfo pinfo = null;
        int packageVersionCode = 0;
        int savedVersionCode = 0;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            packageVersionCode = pinfo.versionCode;
            savedVersionCode = GlobalData.getShowInfoNotificationOnStartVersion(context);
            if (packageVersionCode > savedVersionCode) {
                //boolean show = (versionCode >= VERSION_CODE_FOR_NEWS);
                boolean show = canShowNotification(packageVersionCode, savedVersionCode);
                GlobalData.setShowInfoNotificationOnStart(context, show, packageVersionCode);
            }
            else
                GlobalData.setShowInfoNotificationOnStartVersion(context, packageVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }

        if ((savedVersionCode == 0) || GlobalData.getShowInfoNotificationOnStart(context, packageVersionCode)) {

            showNotification(context,
                    context.getString(R.string.info_notification_title),
                    context.getString(R.string.info_notification_text));

            GlobalData.setShowInfoNotificationOnStart(context, false, packageVersionCode);
        }
    }

    static private boolean canShowNotification(int packageVersionCode, int savedVersionCode) {
        boolean news = false;

        boolean newsLatest = (packageVersionCode >= ImportantInfoNotification.VERSION_CODE_FOR_NEWS);
        boolean news1804 = ((packageVersionCode >= 1804) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean news1772 = ((packageVersionCode >= 1772) && (packageVersionCode < ImportantInfoNotification.VERSION_CODE_FOR_NEWS));
        boolean afterInstall = savedVersionCode == 0;

        if (newsLatest) {
            news = false;
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

        if (afterInstall)
            news = true;

        return news;
    }

    static private void showNotification(Context context, String title, String text) {
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(context.getString(R.string.app_name) + ": " + text) // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        if (android.os.Build.VERSION.SDK_INT >= 16)
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GlobalData.IMPORTANT_INFO_NOTIFICATION_ID, mBuilder.build());
    }

    public static void removeNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GlobalData.IMPORTANT_INFO_NOTIFICATION_ID);
    }

}
