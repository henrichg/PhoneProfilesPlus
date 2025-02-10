package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.view.ChromaColorView;

public class ColorChooserCustomColorDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;
    Dialog colorChooserDialog;
    ColorChooserPreference preferenceColorChooser;
    ProfileIconPreference preferenceProfileIcon;
    RestartEventsIconColorChooserPreference preferenceRestartEventsIcon;

    public ColorChooserCustomColorDialog() {
    }

    public ColorChooserCustomColorDialog(AppCompatActivity activity,
                    Dialog _colorChooserDialog,
                    ColorChooserPreference _preferenceColorChooser,
                    ProfileIconPreference _preferenceProfileIcon,
                    RestartEventsIconColorChooserPreference _preferenceRestartEventsIcon)
    {
        this.activity = activity;
        this.colorChooserDialog = _colorChooserDialog;
        this.preferenceColorChooser = _preferenceColorChooser;
        this.preferenceProfileIcon = _preferenceProfileIcon;
        this.preferenceRestartEventsIcon = _preferenceRestartEventsIcon;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (this.activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.colorChooser_pref_dialog_title), null);
            //dialogBuilder.setTitle(R.string.colorChooser_pref_dialog_title);
            dialogBuilder.setCancelable(true);

            //LayoutInflater inflater = getLayoutInflater();
            // WARNING - use this for get transparent beckround of EditText celector handler
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_custom_color_preference, null);
            dialogBuilder.setView(layout);

            final ChromaColorView chromaColorView = layout.findViewById(R.id.custom_color_chroma_color_view);
            if (preferenceColorChooser != null)
                //noinspection DataFlowIssue
                chromaColorView.setCurrentColor(ColorChooserPreference.parseValue(preferenceColorChooser.value));
            else
            if (preferenceProfileIcon != null)
                //noinspection DataFlowIssue
                chromaColorView.setCurrentColor(preferenceProfileIcon.customColor);
            else
                //noinspection DataFlowIssue
                chromaColorView.setCurrentColor(ColorChooserPreference.parseValue(preferenceRestartEventsIcon.value));
            chromaColorView.setColorMode(ColorMode.values()[0]);
            chromaColorView.setIndicatorMode(IndicatorMode.values()[1]);

            dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                int color = chromaColorView.getCurrentColor();
                if (preferenceColorChooser != null) {
                    preferenceColorChooser.value = String.valueOf(color);
                    preferenceColorChooser.persistValue();
                }
                else
                if (preferenceProfileIcon != null) {
                    Log.e("ColorChooserCustomColorDialog.onButtonClick", "color="+Integer.toHexString(color));
                    preferenceProfileIcon.setCustomColor(true, color);
                } else {
                    preferenceRestartEventsIcon.value = String.valueOf(color);
                    preferenceRestartEventsIcon.persistValue();
                }
                colorChooserDialog.dismiss();
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);
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
