package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class VolumeDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener,
                   AdapterView.OnItemSelectedListener
{

    private Context context;
    private VolumeDialogPreference preference;

    private AppCompatSpinner operatorSpinner = null;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    //private CheckBox sharedProfileChBox = null;
    private Button actualVolumeBtn = null;

    private static volatile Timer playTimer = null;
    static volatile MediaPlayer mediaPlayer = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (VolumeDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_volume_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        CheckBox noChangeChBox = view.findViewById(R.id.volumePrefDialogNoChange);
        operatorSpinner = view.findViewById(R.id.volumePrefDialogVolumesSensorOperator);

        if (preference.forVolumesSensor == 1) {
            HighlightedSpinnerAdapter voiceSpinnerAdapter = new HighlightedSpinnerAdapter(
                    (EventsPrefsActivity) context,
                    R.layout.spinner_highlighted,
                    getResources().getStringArray(R.array.volumesSensorOperatorArray));
            voiceSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
            operatorSpinner.setAdapter(voiceSpinnerAdapter);
            operatorSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
            operatorSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner_all));
        }

        seekBar = view.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = view.findViewById(R.id.volumePrefDialogValueText);
        //sharedProfileChBox = view.findViewById(R.id.volumePrefDialogSharedProfile);

        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue/* - preference.minimumValue*/);
        seekBar.setProgress(preference.value);

        valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

        actualVolumeBtn = view.findViewById(R.id.volumePrefDialogActualVolume);

        if (preference.forVolumesSensor == 0) {
            operatorSpinner.setVisibility(View.GONE);
            if (noChangeChBox != null) {
                noChangeChBox.setVisibility(View.VISIBLE);
                noChangeChBox.setChecked((preference.noChange == 1));
            }
        }
        else {
            if (noChangeChBox != null) {
                noChangeChBox.setVisibility(View.GONE);
            }
            operatorSpinner.setVisibility(View.VISIBLE);
            String[] entryValues = getResources().getStringArray(R.array.volumesSensorOperatorValues);
            int operatorIdx = 0;
            for (String entryValue : entryValues) {
                if (entryValue.equals(String.valueOf(preference.sensorOperator))) {
                    break;
                }
                ++operatorIdx;
            }
            operatorSpinner.setSelection(operatorIdx);
        }

        //sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        //sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        //if (preference.noChange == 1)
        //    sharedProfileChBox.setChecked(false);
        //if (preference.sharedProfile == 1)
        //    noChangeChBox.setChecked(false);

        if (preference.forVolumesSensor == 0) {
            valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            actualVolumeBtn.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
        }
        else {
            valueText.setEnabled(preference.sensorOperator != 0);
            seekBar.setEnabled(preference.sensorOperator != 0);
            actualVolumeBtn.setEnabled(preference.sensorOperator != 0);
        }

        seekBar.setOnSeekBarChangeListener(this);
        if (preference.forVolumesSensor == 0) {
            if (noChangeChBox != null)
                noChangeChBox.setOnCheckedChangeListener(this);
        }
        else
            operatorSpinner.setOnItemSelectedListener(this);
        //sharedProfileChBox.setOnCheckedChangeListener(this);

        int actualVolume = preference.actualVolume;
        actualVolumeBtn.setText(getString(R.string.volume_pref_dialog_actual_volume) +
                                    StringConstants.STR_COLON_WITH_SPACE + actualVolume);
        actualVolumeBtn.setOnClickListener(v -> {
            preference.value = actualVolume;

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value));

            seekBar.setProgress(preference.value);

            preference.callChangeListener(preference.getSValue());
        });

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        final Context appContext = context.getApplicationContext();
        final WeakReference<VolumeDialogPreference> preferenceWeakRef = new WeakReference<>(preference);
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragment.onDialogClosed");

            //Context appContext = appContextWeakRef.get();
            //AudioManager audioManager = audioManagerWeakRef.get();
            VolumeDialogPreference _preference = preferenceWeakRef.get();

            if (/*(appContext != null) &&*/ (_preference != null)) {
                if (_preference.usedValueMusic != -1)
                    ActivateProfileHelper.setMediaVolume(appContext, _preference.audioManager, _preference.usedValueMusic, true, false);
                if (_preference.oldMediaMuted) {
                    PPApplication.volumesInternalChange = true;
                    _preference.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                    PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
                }
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying())
                            mediaPlayer.stop();
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                    try {
                        mediaPlayer.release();
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                }
                if (playTimer != null) {
                    playTimer.cancel();
                    playTimer = null;
                }
            }
        };
        PPApplicationStatic.createPlayToneExecutor();
        PPApplication.playToneExecutor.submit(runnable);

        preference.fragment = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (preference.forVolumesSensor == 0) {

            if (buttonView.getId() == R.id.volumePrefDialogNoChange) {
                preference.noChange = (isChecked) ? 1 : 0;

                valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
                seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
                actualVolumeBtn.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);

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
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

            if (preference.oldMediaMuted && (preference.audioManager != null)) {
                PPApplication.volumesInternalChange = true;
                preference.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
            }
            ActivateProfileHelper.setMediaVolume(context, preference.audioManager, volume, true, false);

            final Context appContext = context.getApplicationContext();
            final WeakReference<VolumeDialogPreference> preferenceWeakRef = new WeakReference<>(preference);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragment.onStopTrackingTouch");

                //Context appContext = appContextWeakRef.get();
                //AudioManager audioManager = audioManagerWeakRef.get();

                VolumeDialogPreference _preference = preferenceWeakRef.get();
                if (/*(appContext != null) &&*/ (_preference != null)) {

                    if (mediaPlayer != null) {
                        try {
                            if (mediaPlayer.isPlaying())
                                mediaPlayer.stop();
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            mediaPlayer.release();
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                    if (playTimer != null) {
                        playTimer.cancel();
                        playTimer = null;
                    }

                    try {
                        if (_preference.volumeType.equalsIgnoreCase("RINGTONE")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (_preference.volumeType.equalsIgnoreCase("NOTIFICATION")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.NOTIFICATION_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (_preference.volumeType.equalsIgnoreCase("MEDIA")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (_preference.volumeType.equalsIgnoreCase("ALARM")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.ALARM_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (_preference.volumeType.equalsIgnoreCase("SYSTEM"))
                            mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if (_preference.volumeType.equalsIgnoreCase("VOICE")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (_preference.volumeType.equalsIgnoreCase("DTMF"))
                            mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if (_preference.volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                            mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if (_preference.volumeType.equalsIgnoreCase("BLUETOOTHSCO")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else
                            mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);

                        if (mediaPlayer != null) {
                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            mediaPlayer.setAudioAttributes(attrs);
                            //_preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            mediaPlayer.start();

                            playTimer = new Timer();
                            playTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        try {
                                            if (mediaPlayer.isPlaying())
                                                mediaPlayer.stop();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }
                                        try {
                                            mediaPlayer.release();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }
                                    }

                                    mediaPlayer = null;
                                    playTimer = null;
                                }
                            }, mediaPlayer.getDuration());

                        }
                    } catch (Exception e) {
                        //Log.e("VolumeDialogPreferenceFragment.onStopTrackingTouch", Log.getStackTraceString(e));

                        // fo not recordException, because of thsi:
                        // java.lang.SecurityException: Settings key: <ringtone2> is not readable. From S+, settings keys
                        // annotated with @hide are restricted to system_server and system apps only, unless they are annotated
                        // with @Readable.
                        //PPApplicationStatic.recordException(e);
                    }
                }
            };
            PPApplicationStatic.createPlayToneExecutor();
            PPApplication.playToneExecutor.submit(runnable);
            /*
            try {
                preference.mediaPlayer.start();
            } catch (Exception ignored) {
            }
            */
        //}
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (preference.forVolumesSensor == 1) {
            ((HighlightedSpinnerAdapter)operatorSpinner.getAdapter()).setSelection(position);

            preference.sensorOperator = Integer.parseInt(preference.operatorValues[position]);

            valueText.setEnabled(preference.sensorOperator != 0);
            seekBar.setEnabled(preference.sensorOperator != 0);
            actualVolumeBtn.setEnabled(preference.sensorOperator != 0);

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

/*    private static abstract class PlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;

        PlayRingtoneRunnable(Context appContext,
                                    AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }

    }*/

/*    private static abstract class StopPlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;

        StopPlayRingtoneRunnable(Context appContext,
                                        AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }

    }*/
}
