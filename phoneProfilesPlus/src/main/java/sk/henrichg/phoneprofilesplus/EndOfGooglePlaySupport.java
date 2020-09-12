package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

public class EndOfGooglePlaySupport {

    static void showDialog(Context context) {
        PPApplication.getShowEndOfGooglePlaySupport(context);
        if (PPApplication.prefShowEndOfGooglePlaySupport) {
            Intent activityIntent = new Intent(context, EndOfGooglePlaySupportActivity.class);
            // clear all opened activities
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

}
