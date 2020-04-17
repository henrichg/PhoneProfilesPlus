package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class NotificationVolume0DialogPreferenceFragmentX  extends PreferenceDialogFragmentCompat {

    private NotificationVolume0DialogPreferenceX preference;

    private RadioButton phoneProfilesSilentRB;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preference = (NotificationVolume0DialogPreferenceX) getPreference();
        preference.fragment = this;

        final SharedPreferences preferences = preference.getSharedPreferences();

        //Log.d("NotificationVolume0DialogPreferenceFragmentX.showDialog","toneInstalled="+ToneHandler.isToneInstalled(TonesHandler.TONE_ID, _context));

        final String uriId = TonesHandler.getPhoneProfilesSilentUri(preference._context, RingtoneManager.TYPE_NOTIFICATION);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(preference._context);

        //Log.e("NotificationVolume0DialogPreferenceFragmentX.onCreateDialog", "before layout inflater");

        LayoutInflater inflater = ((Activity)preference._context).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_notification_volume_0_pref_dialog, null);
        dialogBuilder.setView(layout);

        //Log.e("NotificationVolume0DialogPreferenceFragmentX.onCreateDialog", "after layout inflater");

        dialogBuilder.setTitle(preference.getDialogTitle());

        String message = "";

        //Log.e("NotificationVolume0DialogPreferenceFragmentX.onCreateDialog", "before configure builder");

        if (uriId.isEmpty())
            message = getString(R.string.profile_preferences_volumeNotificationVolume0_toneNotInstalled) + "\n\n";

        String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
        String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");

        if (!notificationToneChange.equals("0")) {
            message = message + getString(R.string.profile_preferences_volumeNotificationVolume0_NowConfigured);
            //if (notificationToneChange.equals(Profile.SHARED_PROFILE_VALUE_STR))
            //    message = message + " " + getString(R.string.default_profile_name);
            //else {
                message = message + " " + TonesHandler.getToneName(preference._context, RingtoneManager.TYPE_NOTIFICATION, notificationTone);
            //}
            message = message + "\n\n";
        }

        message = message + getString(R.string.profile_preferences_volumeNotificationVolume0_radioButtonsLabel);

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "1");
                if ((!uriId.isEmpty()) && (phoneProfilesSilentRB.isChecked()))
                    editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, uriId);
                else
                    editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
                editor.apply();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        //Log.e("NotificationVolume0DialogPreferenceFragmentX.onCreateDialog", "after configure builder");

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

        //Log.e("NotificationVolume0DialogPreferenceFragmentX.onCreateDialog", "after create dialog");

        TextView text = layout.findViewById(R.id.notification_0_pref_dialog_text);
        text.setText(message);

        phoneProfilesSilentRB = layout.findViewById(R.id.notification_0_pref_dialog_PhoneProfilesSilent_rb);
        final RadioButton noneRB = layout.findViewById(R.id.notification_0_pref_dialog_None_rb);

        if (uriId.isEmpty()) {
            phoneProfilesSilentRB.setVisibility(View.GONE);
            noneRB.setChecked(true);
        }
        else {
            phoneProfilesSilentRB.setText(TonesHandler.TONE_NAME);
            phoneProfilesSilentRB.setChecked(true);
            phoneProfilesSilentRB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noneRB.setChecked(false);
                }
            });
        }

        noneRB.setText(R.string.ringtone_preference_none);
        noneRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uriId.isEmpty())
                    phoneProfilesSilentRB.setChecked(false);
            }
        });

        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }
}
