package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.DialogPreference;
import androidx.appcompat.app.AlertDialog;
import android.util.AttributeSet;

public class NotificationVolume0DialogPreference extends DialogPreference {

    private final Context _context;

    private AlertDialog mDialog;

    public NotificationVolume0DialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;
    }

    @Override
    protected void showDialog(Bundle state) {
        final SharedPreferences preferences = getSharedPreferences();

        //Log.d("NotificationVolume0DialogPreference.showDialog","toneInstalled="+ToneHandler.isToneInstalled(TonesHandler.TONE_ID, _context));

        final String uriId = TonesHandler.getPhoneProfilesSilentUri(_context, RingtoneManager.TYPE_NOTIFICATION);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
        if (uriId.isEmpty()) {
            dialogBuilder.setTitle(getDialogTitle());
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
                message = _context.getString(R.string.profile_preferences_volumeNotificationVolume0_questionNowConfigured);
                if (notificationToneChange.equals(Profile.SHARED_PROFILE_VALUE_STR))
                    message = message + " " + _context.getString(R.string.default_profile_name);
                else {
                    message = message + " " + TonesHandler.getToneName(_context, RingtoneManager.TYPE_NOTIFICATION, notificationTone);
                }
                message = message + "\n\n";
            }
            message = message + _context.getString(R.string.profile_preferences_volumeNotificationVolume0_question);

            dialogBuilder.setTitle(getDialogTitle());
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

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        mDialog = dialogBuilder.create();
        /*mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!((Activity)_context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
    }

}
