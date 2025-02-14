package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class GenerateNotificationAfterClickDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;
    private RadioButton startActivatorRb;
    private RadioButton startEditorRb;
    private RadioButton restartEventsRb;
    private RadioButton runStopEventsRunRb;

    public GenerateNotificationAfterClickDialog() {
    }

    public GenerateNotificationAfterClickDialog(AppCompatActivity activity)
    {
        this.activity = activity;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (this.activity != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.generate_notification_after_click_dialog_title), null);
            //dialogBuilder.setTitle(R.string.generate_notification_after_click_dialog_title);
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
                    //noinspection DataFlowIssue
                    if (startActivatorRb.isChecked()) {
                        Intent launcherIntent = new Intent(activity.getApplicationContext(), ActivatorActivity.class);
                        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                        launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                        activity.startActivity(launcherIntent);
                        activity.finish();
                    } else {
                        //noinspection DataFlowIssue
                        if (startEditorRb.isChecked()) {
                            Intent launcherIntent = new Intent(activity.getApplicationContext(), EditorActivity.class);
                            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            activity.startActivity(launcherIntent);
                            activity.finish();
                        } else {
                            //noinspection DataFlowIssue
                            if (restartEventsRb.isChecked()) {
                                DataWrapper dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                                dataWrapper.restartEventsWithAlert(activity);
                            } else {
                                //noinspection DataFlowIssue
                                if (runStopEventsRunRb.isChecked()) {
                                    DataWrapper dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                                    dataWrapper.runStopEventsFronGeneratedNotification(activity);
                                }
                            }
                        }
                    }
                    dismiss();
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> activity.finish());
            dialogBuilder.setOnCancelListener(dialog -> activity.finish());

            mDialog = dialogBuilder.create();
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }
        return mDialog;
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            //mDialog.show();
            show(activity.getSupportFragmentManager(), "GENERATE_NTIFICATION_AFTER_CLICK_DIALOG");
    }

}
