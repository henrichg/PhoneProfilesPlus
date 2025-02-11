package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.math.BigDecimal;

public class BetterNumberPickerDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private LocationGeofenceEditorActivityOSM activity;
    private PPNumberPicker numberPicker;

    public BetterNumberPickerDialog() {
    }

    public BetterNumberPickerDialog(LocationGeofenceEditorActivityOSM activity)
    {
        this.activity = activity;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (LocationGeofenceEditorActivityOSM) getActivity();
        if (this.activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    getString(R.string.event_preferences_location_radius_label), null);
            //dialogBuilder.setTitle(R.string.event_preferences_location_radius_label);
            dialogBuilder.setCancelable(true);

            dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                boolean persist = true;
                BigDecimal number = numberPicker.getEnteredNumber();
                if (activity.isSmaller(number) || activity.isBigger(number)) {
                    /*String errorText = context.getString(R.string.number_picker_min_max_error, String.valueOf(preference.mMin), String.valueOf(preference.mMax));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();*/
                    persist = false;
                } else if (activity.isSmaller(number)) {
                    /*String errorText = context.getString(R.string.number_picker_min_error, String.valueOf(preference.mMin));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();*/
                    persist = false;
                } else if (activity.isBigger(number)) {
                    /*String errorText = context.getString(R.string.number_picker_max_error, String.valueOf(preference.mMax));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();*/
                    persist = false;
                }

                if (persist) {
                    activity.geofence._radius = numberPicker.getNumber().floatValue();
                    activity.radiusValue.setText(String.valueOf(Math.round(activity.geofence._radius)));
                    activity.updateEditedMarker(true);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_better_number_preference, null);
            dialogBuilder.setView(layout);

            numberPicker = layout.findViewById(R.id.better_number_picker);
            // Initialize state
            //noinspection DataFlowIssue
            numberPicker.setMin(BigDecimal.valueOf(LocationGeofenceEditorActivityOSM.MIN_RADIUS));
            numberPicker.setMax(BigDecimal.valueOf(LocationGeofenceEditorActivityOSM.MAX_RADIUS));
            numberPicker.setPlusMinusVisibility(View.INVISIBLE);
            numberPicker.setDecimalVisibility(View.INVISIBLE);
            //mNumberPicker.setLabelText(getContext().getString(R.string.minutes_label_description));
            numberPicker.setNumber(Math.round(activity.geofence._radius), null, null);

            if (/*ApplicationPreferences.applicationTheme(this, true).equals("dark")*/activity.nightModeOn)
                numberPicker.setTheme(R.style.BetterPickersDialogFragment);
            else
                numberPicker.setTheme(R.style.BetterPickersDialogFragment_Light);

            mDialog = dialogBuilder.create();
        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            //mDialog.show();
            show(activity.getSupportFragmentManager(), "COLOR_CHOOSER_CUSTOM_COLOR_DIALOG");
    }

}
