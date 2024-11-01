package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NotificationVolume0DialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private NotificationVolume0DialogPreference preference;

    //private RadioButton phoneProfilesSilentRB;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preference = (NotificationVolume0DialogPreference) getPreference();
        preference.fragment = this;

        final SharedPreferences preferences = preference.getSharedPreferences();

        //Log.d("NotificationVolume0DialogPreferenceFragment.showDialog","toneInstalled="+ToneHandler.isToneInstalled(TonesHandler.TONE_ID, _context));

        //final String uriId = TonesHandler.getPhoneProfilesSilentUri(preference.prefContext, RingtoneManager.TYPE_NOTIFICATION);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(preference.prefContext);

        LayoutInflater inflater = ((Activity)preference.prefContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_notification_volume_0_preference, null);
        dialogBuilder.setView(layout);

        dialogBuilder.setTitle(preference.getDialogTitle());

        String message = "";

        //if (uriId.isEmpty())
        //    message = getString(R.string.profile_preferences_volumeNotificationVolume0_toneNotInstalled) + "\n\n";

        String notificationToneChange = "0";
        if (preferences != null)
            notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
        String notificationTone = "";
        if (preferences != null)
            notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");

        if (!notificationToneChange.equals("0")) {
            message = message + getString(R.string.profile_preferences_volumeNotificationVolume0_NowConfigured);
            //if (notificationToneChange.equals(Profile.SHARED_PROFILE_VALUE_STR))
            //    message = message + " " + getString(R.string.default_profile_name);
            //else {
                message = message + " " + TonesHandler.getToneName(preference.prefContext, RingtoneManager.TYPE_NOTIFICATION, notificationTone);
            //}
            message = message + StringConstants.STR_DOUBLE_NEWLINE;
        }

        message = message + getString(R.string.profile_preferences_volumeNotificationVolume0_radioButtonsLabel);

        dialogBuilder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
            if (preferences != null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "1");
                //if ((!uriId.isEmpty()) && (phoneProfilesSilentRB.isChecked()))
                //    editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, uriId);
                //else
                editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
                editor.apply();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

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

        TextView text = layout.findViewById(R.id.notification_0_pref_dialog_text);
        //noinspection DataFlowIssue
        text.setText(message);

        //phoneProfilesSilentRB = layout.findViewById(R.id.notification_0_pref_dialog_PhoneProfilesSilent_rb);
        final RadioButton noneRB = layout.findViewById(R.id.notification_0_pref_dialog_None_rb);

        //if (uriId.isEmpty()) {
        //    phoneProfilesSilentRB.setVisibility(View.GONE);
            //noinspection DataFlowIssue
            noneRB.setChecked(true);
        //}
        //else {
        //    phoneProfilesSilentRB.setText(TonesHandler.TONE_NAME);
        //    phoneProfilesSilentRB.setChecked(true);
        //    phoneProfilesSilentRB.setOnClickListener(new View.OnClickListener() {
        //        @Override
        //        public void onClick(View v) {
        //            noneRB.setChecked(false);
        //        }
        //    });
        //}

        noneRB.setText(R.string.ringtone_preference_none);
        //noneRB.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        if (!uriId.isEmpty())
        //            phoneProfilesSilentRB.setChecked(false);
        //    }
        //});

        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }
}
