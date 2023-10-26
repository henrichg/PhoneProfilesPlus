package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProfileListNotificationArrowsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfileListNotificationArrowsBroadcastReceiver.onReceive", "xxx");
        String action = intent.getAction();
        if (action != null) {
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfileListNotificationArrowsBroadcastReceiver.onReceive", "action="+action);

            if (action.equalsIgnoreCase(ProfileListNotification.ACTION_RIGHT_ARROW_CLICK)) {
                int displayedPage = ProfileListNotification.displayedPage;
                int profileCount = ProfileListNotification.profileCount;
                if ((displayedPage < profileCount / ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage) &&
                    (profileCount > ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage)) {
                    ++displayedPage;
                    ProfileListNotification.displayedPage = displayedPage;
                    ProfileListNotification._showNotification(context/*, false*/);
                }
            }
            else
            if (action.equalsIgnoreCase(ProfileListNotification.ACTION_LEFT_ARROW_CLICK)) {
                int displayedPage = ProfileListNotification.displayedPage;
                if (displayedPage > 0) {
                    --displayedPage;
                    ProfileListNotification.displayedPage = displayedPage;
                    ProfileListNotification._showNotification(context/*, false*/);
                }
            }
        }
    }
}

