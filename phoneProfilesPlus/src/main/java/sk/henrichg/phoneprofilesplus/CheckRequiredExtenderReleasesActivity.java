package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
//import me.drakeet.support.toast.ToastCompat;

public class CheckRequiredExtenderReleasesActivity extends AppCompatActivity
{
    private boolean activityStarted = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] CheckRequiredExtenderReleasesActivity.onCreate", "xxx");

//        if (showNotStartedToast()) {
//            finish();
//            return;
//        }

        activityStarted = true;

        // close notification drawer - broadcast pending intent not close it :-/
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
//        PPApplication.logE("[BACKGROUND_ACTIVITY] CheckRequiredExtenderReleasesActivity.onStart", "xxx");

//        if (showNotStartedToast()) {
//            if (!isFinishing())
//                finish();
//            return;
//        }

        if (activityStarted) {
            // set theme and language for dialog alert ;-)
            GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false, false);
            //GlobalGUIRoutines.setLanguage(this);

//            PPApplication.logE("[BACKGROUND_ACTIVITY] CheckRequiredExtenderReleasesActivity.onStart", "do installExtender");
            ImportantInfoHelpFragment.installExtender(this, true);
        }
        else {
            if (isFinishing())
                finish();
        }
    }

//    private boolean showNotStartedToast() {
////        PPApplication.logE("[APP_START] CheckRequiredExtenderReleasesActivity.showNotStartedToast", "xxx");
//        boolean applicationStarted = PPApplication.getApplicationStarted(true);
//        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced)*/;
//        if (!applicationStarted) {
//            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
//            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            return true;
//        }
//        if (!fullyStarted) {
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

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
