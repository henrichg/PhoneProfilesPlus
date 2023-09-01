package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

class GenerateNotificationAfterClickDialog
{
    final AlertDialog mDialog;
    final Activity activity;
    final RadioButton startActivatorRb;
    final RadioButton startEditorRb;
    final RadioButton restartEventsRb;
    final RadioButton runStopEventsRunRb;

    GenerateNotificationAfterClickDialog(Activity activity)
    {
        this.activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.profile_preferences_generateNotification);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_generate_notification_after_click, null);
        dialogBuilder.setView(layout);

        startActivatorRb = layout.findViewById(R.id.generateNotificationAfterClickDialogStartActivator);
        startEditorRb = layout.findViewById(R.id.generateNotificationAfterClickDialogStartEditor);
        restartEventsRb = layout.findViewById(R.id.generateNotificationAfterClickDialogRestartEvents);
        runStopEventsRunRb = layout.findViewById(R.id.generateNotificationAfterClickDialogStartStopEventsRun);

        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (!activity.isFinishing()) {
                if (startActivatorRb.isChecked()) {
                    Intent launcherIntent = new Intent(activity.getApplicationContext(), ActivatorActivity.class);
                    launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                    launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                    activity.startActivity(launcherIntent);
                    activity.finish();
                } else if (startEditorRb.isChecked()) {
                    Intent launcherIntent = new Intent(activity.getApplicationContext(), EditorActivity.class);
                    launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                    launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                    activity.startActivity(launcherIntent);
                    activity.finish();
                } else if (restartEventsRb.isChecked()) {
                    DataWrapper dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                    dataWrapper.restartEventsWithAlert(activity);
                } else if (runStopEventsRunRb.isChecked()) {
                    DataWrapper dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                    dataWrapper.runStopEventsFronGeneratedNotification(activity);
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
        dialogBuilder.setOnCancelListener(dialog -> activity.finish());

        mDialog = dialogBuilder.create();
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
