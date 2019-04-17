package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

class VolumeDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

    private Context context;
    private VolumeDialogPreferenceX preference;

    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox sharedProfileChBox = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (VolumeDialogPreferenceX) getPreference();

        final Context _context = context;
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    if (preference.mediaPlayer != null)
                        preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                } catch (Exception ignored) {}
            }
        });

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_volume_pref_dialog, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "value="+preference.value);
        PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "noChange="+preference.noChange);
        PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "sharedProfile="+preference.sharedProfile);

        seekBar = view.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = view.findViewById(R.id.volumePrefDialogValueText);
        noChangeChBox = view.findViewById(R.id.volumePrefDialogNoChange);
        sharedProfileChBox = view.findViewById(R.id.volumePrefDialogSharedProfile);

        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue - preference.minimumValue);
        seekBar.setProgress(preference.value);

        valueText.setText(String.valueOf(preference.value + preference.minimumValue));

        noChangeChBox.setChecked((preference.noChange == 1));

        sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        if (preference.noChange == 1)
            sharedProfileChBox.setChecked(false);
        if (preference.sharedProfile == 1)
            noChangeChBox.setChecked(false);

        valueText.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
        seekBar.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));

        seekBar.setOnSeekBarChangeListener(this);
        noChangeChBox.setOnCheckedChangeListener(this);
        sharedProfileChBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }

        final Context _context = context;
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (preference.audioManager != null) {
                    ActivateProfileHelper.setMediaVolume(_context, preference.audioManager, preference.defaultValueMusic);
                    if (preference.mediaPlayer != null) {
                        try {
                            if (preference.mediaPlayer.isPlaying())
                                preference.mediaPlayer.stop();
                            preference.mediaPlayer.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.volumePrefDialogNoChange)
        {
            preference.noChange = (isChecked)? 1 : 0;

            valueText.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            seekBar.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            if (isChecked)
                sharedProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.volumePrefDialogSharedProfile)
        {
            preference.sharedProfile = (isChecked)? 1 : 0;

            valueText.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            seekBar.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "progress="+progress);
        PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "fromUser="+fromUser);

        if (fromUser) {
            // Round the value to the closest integer value.
            //noinspection ConstantConditions
            if (preference.stepSize >= 1) {
                preference.value = Math.round((float) progress / preference.stepSize) * preference.stepSize;
            } else {
                preference.value = progress;
            }

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value + preference.minimumValue));

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (preference.mediaPlayer != null) {
            int volume;
            //noinspection ConstantConditions
            if (preference.volumeType.equalsIgnoreCase("MEDIA"))
                volume = preference.value;
            else {
                float percentage = (float) preference.value / preference.maximumValue * 100.0f;
                volume = Math.round(preference.maximumMediaValue / 100.0f * percentage);
            }

            ActivateProfileHelper.setMediaVolume(context, preference.audioManager, volume);

            try {
                preference.mediaPlayer.start();
            } catch (Exception ignored) {
            }
        }
    }
}
