package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ImportPPDataBroadcastReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        switch (intent.getAction()) {
            case PPApplication.ACTION_EXPORT_PP_DATA_STOP:
                EditorProfilesActivity.importFromPPStopped = true;
                break;
        }

    }

}
