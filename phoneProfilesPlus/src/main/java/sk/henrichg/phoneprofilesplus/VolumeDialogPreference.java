package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class VolumeDialogPreference extends
        DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    // Layout widgets.

    private final Context _context;
    private AlertDialog mDialog;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox sharedProfileChBox = null;

    private final AudioManager audioManager;
    private MediaPlayer mediaPlayer = null;

    // Custom xml attributes.
    private String volumeType;
    private int noChange;
    private int sharedProfile;
    private int disableSharedProfile;

    private int maximumValue = 7;
    private final int minimumValue = 0;
    private int maximumMediaValue = 15;
    private int defaultValueRing = 0;
    private int defaultValueNotification = 0;
    private int defaultValueMusic = 0;
    private int defaultValueAlarm = 0;
    private int defaultValueSystem = 0;
    private int defaultValueVoice = 0;
    private final int stepSize = 1;

    private String sValue = "0|1";
    private int value = 0;

    public VolumeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.VolumeDialogPreference);

        volumeType = typedArray.getString(
            R.styleable.VolumeDialogPreference_volumeType);
        noChange = typedArray.getInteger(
            R.styleable.VolumeDialogPreference_vNoChange, 1);
        sharedProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vSharedProfile, 0);
        disableSharedProfile = typedArray.getInteger(
                R.styleable.VolumeDialogPreference_vDisableSharedProfile, 0);

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            // get max. values from audio manager
            if (volumeType.equalsIgnoreCase("RINGTONE"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            else if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            else if (volumeType.equalsIgnoreCase("MEDIA"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            else if (volumeType.equalsIgnoreCase("ALARM"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            else if (volumeType.equalsIgnoreCase("SYSTEM"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            else if (volumeType.equalsIgnoreCase("VOICE"))
                maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            // get actual values from audio manager
            defaultValueRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            defaultValueNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            defaultValueMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            defaultValueAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            defaultValueSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            defaultValueVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        typedArray.recycle();
    }

    @Override
    protected void showDialog(Bundle state) {

        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    if (mediaPlayer != null)
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                } catch (Exception ignored) {}
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    int _value = value + minimumValue;
                    persistString(_value
                            + "|" + noChange
                            + "|" + sharedProfile);
                    setSummaryVDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_volume_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        seekBar = layout.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = layout.findViewById(R.id.volumePrefDialogValueText);
        noChangeChBox = layout.findViewById(R.id.volumePrefDialogNoChange);
        sharedProfileChBox = layout.findViewById(R.id.volumePrefDialogSharedProfile);

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(stepSize);
        seekBar.setMax(maximumValue - minimumValue);

        getValueVDP();

        seekBar.setProgress(value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((noChange == 1));

        sharedProfileChBox.setOnCheckedChangeListener(this);
        sharedProfileChBox.setChecked((sharedProfile == 1));
        sharedProfileChBox.setEnabled(disableSharedProfile == 0);

        if (noChange == 1)
            sharedProfileChBox.setChecked(false);
        if (sharedProfile == 1)
            noChangeChBox.setChecked(false);

        valueText.setEnabled((noChange == 0) && (sharedProfile == 0));
        seekBar.setEnabled((noChange == 0) && (sharedProfile == 0));

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)_context).isFinishing())
            mDialog.show();
    }

    public void onProgressChanged(SeekBar seek, int newValue,
            boolean fromTouch) {
        // Round the value to the closest integer value.
        //noinspection ConstantConditions
        if (stepSize >= 1) {
            value = Math.round((float)newValue/stepSize)*stepSize;
        }
        else {
            value = newValue;
        }

        // Set the valueText text.
        valueText.setText(String.valueOf(value + minimumValue));

        callChangeListener(value);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == R.id.volumePrefDialogNoChange)
        {
            noChange = (isChecked)? 1 : 0;

            valueText.setEnabled((noChange == 0) && (sharedProfile == 0));
            seekBar.setEnabled((noChange == 0) && (sharedProfile == 0));
            if (isChecked)
                sharedProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.volumePrefDialogSharedProfile)
        {
            sharedProfile = (isChecked)? 1 : 0;

            valueText.setEnabled((noChange == 0) && (sharedProfile == 0));
            seekBar.setEnabled((noChange == 0) && (sharedProfile == 0));
            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        callChangeListener(noChange);
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
        if (mediaPlayer != null) {
            int volume;
            if (volumeType.equalsIgnoreCase("MEDIA"))
                volume = value;
            else {
                float percentage = (float) value / maximumValue * 100.0f;
                volume = Math.round(maximumMediaValue / 100.0f * percentage);
            }

            ActivateProfileHelper.setMediaVolume(_context, audioManager, volume);

            try {
                mediaPlayer.start();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueVDP();
        }
        else {
            // set state
            value = 0;
            noChange = 1;
            sharedProfile = 0;
            int _value = value + minimumValue;
            persistString(_value
                    + "|" + noChange
                    + "|" + sharedProfile);
        }
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString(sValue);

        String[] splits = sValue.split("\\|");
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
            {
                if (volumeType.equalsIgnoreCase("SYSTEM"))
                    value = defaultValueSystem;
                else
                if (volumeType.equalsIgnoreCase("RINGTONE"))
                    value = defaultValueRing;
                else
                if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                    value =  defaultValueNotification;
                else
                if (volumeType.equalsIgnoreCase("MEDIA"))
                    value =  defaultValueMusic;
                else
                if (volumeType.equalsIgnoreCase("ALARM"))
                    value =  defaultValueAlarm;
                else
                if (volumeType.equalsIgnoreCase("VOICE"))
                    value =  defaultValueVoice;
            }
        } catch (Exception e) {
            value = 0;
        }
        value = value - minimumValue;
        try {
            noChange = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            noChange = 1;
        }
        try {
            sharedProfile = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            sharedProfile = 0;
        }

        // You're never know...
        if (value < 0) {
            value = 0;
        }
    }

    private void setSummaryVDP()
    {
        String prefVolumeDataSummary;
        if (noChange == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_no_change);
        else
        if (sharedProfile == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);
        else
            prefVolumeDataSummary = value + " / " + maximumValue;
        setSummary(prefVolumeDataSummary);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (audioManager != null) {
                    ActivateProfileHelper.setMediaVolume(_context, audioManager, defaultValueMusic);
                    if (mediaPlayer != null) {
                        try {
                            if (mediaPlayer.isPlaying())
                                mediaPlayer.stop();
                            mediaPlayer.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        });

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final SavedState myState = new SavedState(superState);
        myState.value = value;
        myState.volumeType = volumeType;
        myState.noChange = noChange;
        myState.sharedProfile = sharedProfile;
        myState.disableSharedProfile = disableSharedProfile;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryVDP();
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        volumeType = myState.volumeType;
        noChange = myState.noChange;
        sharedProfile = myState.sharedProfile;
        disableSharedProfile = myState.disableSharedProfile;

        setSummaryVDP();
        notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        int value = 0;
        String volumeType = null;
        int noChange = 0;
        int sharedProfile = 0;
        int disableSharedProfile = 0;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readInt();
            volumeType = source.readString();
            noChange = source.readInt();
            sharedProfile = source.readInt();
            disableSharedProfile = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeInt(value);
            dest.writeString(volumeType);
            dest.writeInt(noChange);
            dest.writeInt(sharedProfile);
            dest.writeInt(disableSharedProfile);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in)
                    {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size)
                    {
                        return new SavedState[size];
                    }

                };

    }

}
