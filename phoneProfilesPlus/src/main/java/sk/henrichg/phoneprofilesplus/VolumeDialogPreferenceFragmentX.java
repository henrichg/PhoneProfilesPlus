package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class VolumeDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

    private Context context;
    private VolumeDialogPreferenceX preference;

    private SeekBar seekBar = null;
    private TextView valueText = null;
    //private CheckBox sharedProfileChBox = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (VolumeDialogPreferenceX) getPreference();
        preference.fragment = this;

        /*
        final Context _context = context;
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (preference.volumeType.equalsIgnoreCase("RINGTONE")) {
                        String ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("NOTIFICATION")) {
                        String ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("MEDIA"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("ALARM")) {
                        String ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("SYSTEM"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("VOICE"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("DTMF"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if ((Build.VERSION.SDK_INT >= 26) && preference.volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("BLUETOOTHSCO"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);

                    if (preference.mediaPlayer != null) {
                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        preference.mediaPlayer.setAudioAttributes(attrs);
                        //preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                } catch (Exception ignored) {}
            }
        });
        */

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_volume_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "value=" + preference.value);
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "noChange=" + preference.noChange);
            //PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "sharedProfile="+preference.sharedProfile);
        }*/

        seekBar = view.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = view.findViewById(R.id.volumePrefDialogValueText);
        CheckBox noChangeChBox = view.findViewById(R.id.volumePrefDialogNoChange);
        //sharedProfileChBox = view.findViewById(R.id.volumePrefDialogSharedProfile);

        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue/* - preference.minimumValue*/);
        seekBar.setProgress(preference.value);

        valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

        noChangeChBox.setChecked((preference.noChange == 1));

        //sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        //sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        //if (preference.noChange == 1)
        //    sharedProfileChBox.setChecked(false);
        //if (preference.sharedProfile == 1)
        //    noChangeChBox.setChecked(false);

        valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
        seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);

        seekBar.setOnSeekBarChangeListener(this);
        noChangeChBox.setOnCheckedChangeListener(this);
        //sharedProfileChBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        final Context _context = context;
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragmentX.onDialogClosed");
            if (preference.audioManager != null) {
                if (preference.defaultValueMusic != -1)
                    ActivateProfileHelper.setMediaVolume(_context, preference.audioManager, preference.defaultValueMusic);
                if (preference.oldMediaMuted)
                    preference.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                if (preference.mediaPlayer != null) {
                    try {
                        if (preference.mediaPlayer.isPlaying())
                            preference.mediaPlayer.stop();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                    try {
                        preference.mediaPlayer.release();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                }
            }
        });

        preference.fragment = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.volumePrefDialogNoChange)
        {
            preference.noChange = (isChecked)? 1 : 0;

            valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            //if (isChecked)
            //    sharedProfileChBox.setChecked(false);
        }

        /*
        if (buttonView.getId() == R.id.volumePrefDialogSharedProfile)
        {
            preference.sharedProfile = (isChecked)? 1 : 0;

            valueText.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            seekBar.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            if (isChecked)
                noChangeChBox.setChecked(false);
        }
        */

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "progress=" + progress);
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "fromUser=" + fromUser);
        }*/

        if (fromUser) {
            // Round the value to the closest integer value.
            //noinspection ConstantConditions
            if (preference.stepSize >= 1) {
                preference.value = Math.round((float) progress / preference.stepSize) * preference.stepSize;
            } else {
                preference.value = progress;
            }

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //if (preference.mediaPlayer != null) {
            int volume;
            if ((preference.volumeType != null) && preference.volumeType.equalsIgnoreCase("MEDIA"))
                volume = preference.value;
            else {
                float percentage = (float) preference.value / preference.maximumValue * 100.0f;
                volume = Math.round(preference.maximumMediaValue / 100.0f * percentage);
            }

            if (preference.oldMediaMuted && (preference.audioManager != null))
                preference.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            ActivateProfileHelper.setMediaVolume(context, preference.audioManager, volume);

            final Context _context = context;
            PPApplication.startHandlerThreadPlayTone();
            final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
            handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragmentX.onStopTrackingTouch");
                if (preference.mediaPlayer != null) {
                    try {
                        if (preference.mediaPlayer.isPlaying())
                            preference.mediaPlayer.stop();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                    try {
                        preference.mediaPlayer.release();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                }

                try {
                    if (preference.volumeType.equalsIgnoreCase("RINGTONE")) {
                        Uri _ringtoneUri =  Settings.System.DEFAULT_RINGTONE_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("NOTIFICATION")) {
                        Uri _ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("MEDIA")) {
                        Uri _ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("ALARM")) {
                        Uri _ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("SYSTEM"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("VOICE")) {
                        Uri _ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("DTMF"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if ((Build.VERSION.SDK_INT >= 26) && preference.volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("BLUETOOTHSCO")) {
                        Uri _ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI;
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);

                    if (preference.mediaPlayer != null) {
                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        preference.mediaPlayer.setAudioAttributes(attrs);
                        //preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        //PPApplication.logE("VolumeDialogPreferenceFragmentX.onStopTrackingTouch", "start playing");
                        preference.mediaPlayer.start();
                    }
                } catch (Exception e) {
                    //Log.e("VolumeDialogPreferenceFragmentX.onStopTrackingTouch", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            });
            /*
            try {
                preference.mediaPlayer.start();
            } catch (Exception ignored) {
            }
            */
        //}
    }
}
