package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class EndOfGooglePlaySupport {

    static void showNotification(Context context) {
        PPApplication.getShowEndOfGooglePlaySupport(context);
        if (PPApplication.prefShowEndOfGooglePlaySupport) {
            PPApplication.createExclamationNotificationChannel(context);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                    .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                    .setContentTitle(context.getString(R.string.google_play_not_more_supported_title)) // title for notification
                    //.setContentText("End of GPLay support text") // message for notification
                    .setAutoCancel(true); // clear notification after click
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.google_play_not_more_supported_title)));
            final Intent intent = new Intent(context, EndOfGooglePlaySupportActivity.class);
            intent.setData(Uri.parse("package:" + PPApplication.PACKAGE_NAME));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            //if (android.os.Build.VERSION.SDK_INT >= 21)
            //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            //}
            mBuilder.setOnlyAlertOnce(true);

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
            try {
                mNotificationManager.notify(
                        PPApplication.END_OF_GOOGLE_PLAY_NOTIFICATION_TAG,
                        PPApplication.END_OF_GOOGLE_PLAY_NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
                //Log.e("EndOfGooglePlaySupport.showNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    /*
    static void showDialog(Context context) {
        PPApplication.getShowEndOfGooglePlaySupport(context);
        //if (PPApplication.prefShowEndOfGooglePlaySupport) {
            PPApplication.logE("[BACKGROUND_ACTIVITY] EndOfGooglePlaySupport.showDialog", "xxx");
            Intent activityIntent = new Intent(context, EndOfGooglePlaySupportActivity.class);
            // clear all opened activities
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        //}
    }
    */

}
