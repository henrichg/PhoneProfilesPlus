package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class WorkManagerNotWorkingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("ExitApplicationActivity.onCreate", "xxx");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        //PPApplication.logE("ExitApplicationActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.background_jobs_not_working_alert_title);
        dialogBuilder.setMessage(R.string.background_jobs_not_working_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(false);

        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> WorkManagerNotWorkingActivity.this.finish());

        dialogBuilder.setNegativeButton(R.string.application_cannot_be_started_button_restart, (dialog, which) -> {
            boolean serviceStarted = PhoneProfilesService.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
            if (serviceStarted) {
                stopService(new Intent(getApplicationContext(), PhoneProfilesService.class));
                PPApplication.sleep(2000);

                //PPApplication.startTimeOfApplicationStart = Calendar.getInstance().getTimeInMillis();

                PPApplication.setApplicationStarted(getApplicationContext(), true);
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                PPApplication.logE("[START_PP_SERVICE] WorkManagerNotWorkingActivity.onStart", "xxx");
                PPApplication.startPPService(getApplicationContext(), serviceIntent);
                WorkManagerNotWorkingActivity.this.finish();
            }
        });

        /*dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                WorkManagerNotWorkingActivity.this.finish();
            }
        });*/
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
