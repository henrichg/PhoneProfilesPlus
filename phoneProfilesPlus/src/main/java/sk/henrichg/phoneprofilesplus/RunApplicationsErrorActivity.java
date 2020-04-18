package sk.henrichg.phoneprofilesplus;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RunApplicationsErrorActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    static final String EXTRA_ACTIVITY_TYPE = "activity_type";
    static final String EXTRA_EXCEPTION = "exception";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("BackgroundActivateProfileActivity.onCreate", "xxx");

        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (activityStarted) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getBaseContext());
            dialogBuilder.setTitle(R.string.profile_preferences_deviceRunApplicationsShortcutsChange);

            String sException = "";
            if (getIntent().hasExtra(EXTRA_EXCEPTION))
                sException = getIntent().getStringExtra(EXTRA_EXCEPTION);

            int type = getIntent().getIntExtra(EXTRA_ACTIVITY_TYPE, 0);
            switch (type) {
                case 1:
                    String message = getString(R.string.run_applications_error_dialog_text_application);
                    if (!sException.isEmpty())
                        message = message + ": " + getIntent().getStringExtra(EXTRA_EXCEPTION);
                    dialogBuilder.setMessage(message);
                    break;
                case 2:
                    message = getString(R.string.run_applications_error_dialog_text_shortcut);
                    if (!sException.isEmpty())
                        message = message + ": " + getIntent().getStringExtra(EXTRA_EXCEPTION);
                    dialogBuilder.setMessage(message);
                    break;
                case 3:
                    message = getString(R.string.run_applications_error_dialog_text_intent);
                    if (!sException.isEmpty())
                        message = message + ": " + getIntent().getStringExtra(EXTRA_EXCEPTION);
                    dialogBuilder.setMessage(message);
                    break;
            }
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);
            dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!RunApplicationsErrorActivity.this.isFinishing())
                        RunApplicationsErrorActivity.this.finish();
                }
            });
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!isFinishing())
                dialog.show();
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    private boolean showNotStartedToast() {
        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = true;
        if (applicationStarted) {
            PhoneProfilesService instance = PhoneProfilesService.getInstance();
            fullyStarted = instance.getApplicationFullyStarted();
            applicationStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);
        }
        if (!applicationStarted) {
            String text = getString(R.string.app_name) + " " + getString(R.string.application_is_not_started);
            if (!fullyStarted)
                text = getString(R.string.app_name) + " " + getString(R.string.application_is_starting_toast);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false, mergedProfile);
                    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
