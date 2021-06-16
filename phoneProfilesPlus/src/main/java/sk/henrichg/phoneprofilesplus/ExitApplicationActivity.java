package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ExitApplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] ExitApplicationActivity.onCreate", "xxx");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        //PPApplication.logE("ExitApplicationActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.exit_application_alert_title);
        dialogBuilder.setMessage(R.string.exit_application_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
            //PPApplication.logE("ExitApplicationActivity.onStart", "exit");

            Context appContext = getApplicationContext();

            //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
            SharedPreferences settings = ApplicationPreferences.getSharedPreferences(appContext);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
            editor.apply();
            ApplicationPreferences.applicationEventNeverAskForEnableRun(appContext);
            ApplicationPreferences.applicationNeverAskForGrantRoot(appContext);
            ApplicationPreferences.applicationNeverAskForGrantG1Permission(appContext);

            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0f);
            //PPApplication.logE("PPApplication.exitApp", "from ExitApplicationActivity.onStart shutdown=false");
            PPApplication.exitApp(true, appContext, dataWrapper, ExitApplicationActivity.this, false/*, true, true*/);

            // close activities
            Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
            intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "activator");
            appContext.sendBroadcast(intent);
            intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
            intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "editor");
            appContext.sendBroadcast(intent);

            finish();
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, (dialogInterface, i) -> ExitApplicationActivity.this.finish());
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
