package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

class InfoDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    InfoDialogPreferenceX preference;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preference = (InfoDialogPreferenceX) getPreference();

        return new AlertDialog.Builder(preference.getContext())
                .setIcon(preference.getIcon())
                .setTitle(preference.getTitle())
                .setMessage(preference.infoText)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
