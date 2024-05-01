package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ErrorNotificationActivity extends AppCompatActivity {

    AlertDialog mDialog;

    private int error_type;
    private long profile_id;
    private long event_id;

    static final String EXTRA_ERROR_TYPE = "error_type";

    static final int ERROR_TYPE_PROFILE = 1;
    static final int ERROR_TYPE_EVENT = 2;
    static final int ERROR_TYPE_DND_ACCESS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] GrantDrawOverAppsActivity.onCreate", "xxx");
        //Log.e("GrantDrawOverAppsActivity.onCreate", "xxx");

        Intent intent = getIntent();
        error_type = intent.getIntExtra(EXTRA_ERROR_TYPE, 0);
        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart()
    {
        super.onStart();

        GlobalGUIRoutines.lockScreenOrientation(this, true);

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        boolean isError = false;
        Intent intent = null;
        String title = "";
        if (error_type != 0) {
            switch (error_type) {
                case ERROR_TYPE_PROFILE:
                    title = getString(R.string.profile_preferences_red_texts_title);
                    Profile profile = null;
                    if (profile_id != 0)
                        profile = DatabaseHandler.getInstance(getApplicationContext()).getProfile(profile_id, false);
                    if (profile != null) {
                        isError = ProfileStatic.isRedTextNotificationRequired(profile, true, getApplicationContext());
                        intent = new Intent(getBaseContext(), ProfilesPrefsActivity.class);
                        intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                        intent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_EDIT);
                        intent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                    } else {
                        isError = true;
                        intent = new Intent(getBaseContext(), EditorActivity.class);
                        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
                    }
                    break;
                case ERROR_TYPE_EVENT:
                    title = getString(R.string.event_preferences_red_texts_title);
                    Event event = null;
                    if (event_id != 0)
                        event = DatabaseHandler.getInstance(getApplicationContext()).getEvent(event_id);
                    if (event != null) {
                        isError = EventStatic.isRedTextNotificationRequired(event, true, getApplicationContext());
                        intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                        intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
                        intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_EDIT);
                        intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, 0);
                    } else {
                        isError = true;
                        intent = new Intent(getBaseContext(), EditorActivity.class);
                        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER);
                    }
                    break;
                case ERROR_TYPE_DND_ACCESS:
                    title = getString(R.string.do_not_disturb_access_permission_notification);
                    NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null)
                        isError = !mNotificationManager.isNotificationPolicyAccessGranted();
                    else
                        isError = true;
                    intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    break;
            }
        }

        if (isError) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(title);
            dialogBuilder.setMessage(R.string.no_error_dialog_message);
            dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> finish());
            dialogBuilder.setOnDismissListener(dialog -> finish());

            mDialog = dialogBuilder.create();
            //mDialog.setCancelable(false);
            //mDialog.setCanceledOnTouchOutside(false);

            if (!isFinishing())
                mDialog.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

}
