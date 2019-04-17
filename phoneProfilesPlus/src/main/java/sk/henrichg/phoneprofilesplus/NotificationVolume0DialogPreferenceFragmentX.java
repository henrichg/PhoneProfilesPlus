package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NotificationVolume0DialogPreferenceFragmentX  extends PreferenceDialogFragmentCompat {

    NotificationVolume0DialogPreferenceX preference;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preference = (NotificationVolume0DialogPreferenceX) getPreference();

        final SharedPreferences preferences = preference.getSharedPreferences();

        //Log.d("NotificationVolume0DialogPreference.showDialog","toneInstalled="+ToneHandler.isToneInstalled(TonesHandler.TONE_ID, _context));

        final String uriId = TonesHandler.getPhoneProfilesSilentUri(preference._context, RingtoneManager.TYPE_NOTIFICATION);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(preference._context);
        if (uriId.isEmpty()) {
            dialogBuilder.setTitle(preference.getDialogTitle());
            dialogBuilder.setMessage(R.string.profile_preferences_volumeNotificationVolume0_toneNotInstalled);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
        }
        else {

            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            //Log.d("NotificationVolume0DialogPreference.showDialog","notificationToneChange="+notificationToneChange);
            //Log.d("NotificationVolume0DialogPreference.showDialog","notificationTone="+notificationTone);


            String message = "";
            if (!notificationToneChange.equals("0")) {
                message = getString(R.string.profile_preferences_volumeNotificationVolume0_questionNowConfigured);
                if (notificationToneChange.equals(Profile.SHARED_PROFILE_VALUE_STR))
                    message = message + " " + getString(R.string.default_profile_name);
                else {
                    message = message + " " + TonesHandler.getToneName(preference._context, RingtoneManager.TYPE_NOTIFICATION, notificationTone);
                }
                message = message + "\n\n";
            }
            message = message + getString(R.string.profile_preferences_volumeNotificationVolume0_question);

            dialogBuilder.setTitle(preference.getDialogTitle());
            dialogBuilder.setMessage(message);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "1");
                    editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, uriId);
                    editor.apply();
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
            /*dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
                    editor.putString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION, "");
                    editor.apply();
                }
            });*/
        }

        return dialogBuilder.create();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
