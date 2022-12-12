package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.appcompat.app.AlertDialog;

class DebugVersion {
    static final boolean enabled = true;

    static boolean debugMenuItems(int menuItem, Activity activity) {

        if (menuItem == R.id.menu_test_crash) {
            throw new RuntimeException("Test Crash");
            //return true;
        }
        else
        if (menuItem == R.id.menu_test_nonFatal) {
            try {
                throw new RuntimeException("Test non-fatal exception");
            } catch (Exception e) {
                // You must relaunch PPP to get this exception in Firebase console:
                //
                // Crashlytics processes exceptions on a dedicated background thread, so the performance
                // impact to your app is minimal. To reduce your users’ network traffic, Crashlytics batches
                // logged exceptions together and sends them the next time the app launches.
                //
                // Crashlytics only stores the most recent 8 exceptions in a given app session. If your app
                // throws more than 8 exceptions in a session, older exceptions are lost.
                PPApplication.recordException(e);
            }
            return true;
        }
        else
        if (menuItem == R.id.menu_show_sound_mode) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle("Sound mode in system");

            String soundModeString = "Ringer mode=";

            final AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                switch (audioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        soundModeString = soundModeString + "RINGER_MODE_NORMAL";
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        soundModeString = soundModeString + "RINGER_MODE_VIBRATE";
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        soundModeString = soundModeString + "RINGER_MODE_SILENT";
                        break;
                }
            }

            soundModeString = soundModeString + "\nZen mode=";
            switch (ActivateProfileHelper.getSystemZenMode(activity.getApplicationContext())) {
                case ActivateProfileHelper.ZENMODE_ALL:
                    soundModeString = soundModeString + "ZENMODE_ALL";
                    break;
                case ActivateProfileHelper.ZENMODE_PRIORITY:
                    soundModeString = soundModeString + "ZENMODE_PRIORITY";
                    break;
                case ActivateProfileHelper.ZENMODE_ALARMS:
                    soundModeString = soundModeString + "ZENMODE_ALARMS";
                    break;
                case ActivateProfileHelper.ZENMODE_NONE:
                    soundModeString = soundModeString + "ZENMODE_NONE";
                    break;
                case ActivateProfileHelper.ZENMODE_SILENT:
                    soundModeString = soundModeString + "ZENMODE_SILENT";
                    break;
            }

            dialogBuilder.setMessage(soundModeString);

            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

            if (!activity.isFinishing())
                dialog.show();

            return true;
        }
        else
        if (menuItem == R.id.menu_show_log_file) {
            Intent intentLaunch = new Intent(activity.getApplicationContext(), LogCrashActivity.class);
            activity.startActivity(intentLaunch);

            return true;
        }
        else
            return false;
    }

}
