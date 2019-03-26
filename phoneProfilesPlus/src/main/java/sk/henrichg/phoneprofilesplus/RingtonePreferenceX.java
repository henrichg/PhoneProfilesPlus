package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

public class RingtonePreferenceX extends DialogPreference {
    String ringtoneUri;
    String oldRingtoneUri;
    private String ringtoneName;

    final String ringtoneType;
    final boolean showSilent;
    final boolean showDefault;

    private final Context prefContext;
    RingtonePreferenceAdapterX listAdapter;

    private static MediaPlayer mediaPlayer = null;
    private static int oldMediaVolume = -1;
    private static Timer playTimer = null;
    private static boolean ringtoneIsPlayed = false;

    public RingtonePreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference);

        ringtoneType = typedArray.getString(R.styleable.RingtonePreference_ringtoneType);
        showSilent = typedArray.getBoolean(R.styleable.RingtonePreference_showSilent, false);
        showDefault = typedArray.getBoolean(R.styleable.RingtonePreference_showDefault, false);

        ringtoneUri = "";
        if (ringtoneType != null) {
            switch (ringtoneType) {
                case "ringtone":
                    ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI.toString();
                    break;
                case "notification":
                    ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
                    break;
                case "alarm":
                    ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
                    break;
            }
        }

        prefContext = context;

        typedArray.recycle();

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopPlayRingtone();
            }
        });

        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopPlayRingtone();
            }
        });

        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        //if (myState.isDialogShowing) {
        //    showDialog(myState.dialogBundle);
        //}
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        String value;
        try {
            value = getPersistedString((String) defaultValue);
        } catch  (Exception e) {
            value = (String) defaultValue;
        }
        ringtoneUri = value;
        setRingtone("", true);
    }

    void setRingtone(String newRingtoneUri, boolean onlySetName)
    {
        if (!onlySetName)
            ringtoneUri = newRingtoneUri;

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = prefContext.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(prefContext, uri);
                    try {
                        ringtoneName = ringtone.getTitle(prefContext);
                    } catch (Exception e) {
                        ringtoneName = prefContext.getString(R.string.ringtone_preference_not_set);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                setSummary(ringtoneName);
            }

        }.execute();

        if (!onlySetName) {
            //View positive =
            //        getButton(DialogInterface.BUTTON_POSITIVE);
            //positive.setEnabled(true);
            setPositiveButtonText(android.R.string.ok);

            listAdapter.notifyDataSetChanged();
        }
    }

    void stopPlayRingtone() {
        final AudioManager audioManager = (AudioManager)prefContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (playTimer != null) {
                playTimer.cancel();
                playTimer = null;
            }
            if ((mediaPlayer != null) && ringtoneIsPlayed) {
                try {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.release();
                } catch (Exception ignored) {
                }
                ringtoneIsPlayed = false;
                mediaPlayer = null;

                if (oldMediaVolume > -1)
                    ActivateProfileHelper.setMediaVolume(prefContext, audioManager, oldMediaVolume);
            }
        }
    }
    void playRingtone() {
        final AudioManager audioManager = (AudioManager)prefContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {

            final Uri _ringtoneUri = Uri.parse(ringtoneUri);

            PPApplication.startHandlerThreadPlayTone();
            final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        stopPlayRingtone();

                        RingerModeChangeReceiver.internalChange = true;

                        if (mediaPlayer == null)
                            mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(prefContext, _ringtoneUri);
                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);

                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        int ringtoneVolume = 0;
                        int maximumRingtoneValue = 0;

                        switch (ringtoneType) {
                            case "ringtone":
                                ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                                break;
                            case "notification":
                                ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                                break;
                            case "alarm":
                                ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                                break;
                        }

                        PPApplication.logE("RingtonePreference.playRingtone", "ringtoneVolume=" + ringtoneVolume);

                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) ringtoneVolume / maximumRingtoneValue * 100.0f;
                        int mediaVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("RingtonePreference.playRingtone", "mediaVolume=" + mediaVolume);

                        ActivateProfileHelper.setMediaVolume(prefContext, audioManager, mediaVolume);

                        mediaPlayer.start();
                        ringtoneIsPlayed = true;

                        //final Context context = this;
                        playTimer = new Timer();
                        playTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null) {
                                    try {
                                        if (mediaPlayer.isPlaying())
                                            mediaPlayer.stop();
                                        mediaPlayer.release();
                                    } catch (Exception ignored) {
                                    }

                                    if (oldMediaVolume > -1)
                                        ActivateProfileHelper.setMediaVolume(prefContext, audioManager, oldMediaVolume);
                                    PPApplication.logE("RingtonePreference.playRingtone", "play stopped");
                                }

                                ringtoneIsPlayed = false;
                                mediaPlayer = null;

                                PPApplication.startHandlerThreadInternalChangeToFalse();
                                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);
                                //PostDelayedBroadcastReceiver.setAlarm(
                                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);

                                playTimer = null;
                            }
                        }, mediaPlayer.getDuration());

                    } catch (SecurityException e) {
                        PPApplication.logE("RingtonePreference.playRingtone", "security exception");
                        stopPlayRingtone();
                        PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);
                    } catch (Exception e) {
                        PPApplication.logE("RingtonePreference.playRingtone", "exception");
                        stopPlayRingtone();
                        PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);
                    }
                }
            });

        }
    }

    void initRingtoneUri() {
        String value;
        try {
            value = getPersistedString(ringtoneUri);
        } catch  (Exception e) {
            value = ringtoneUri;
        }
        ringtoneUri = value;
        PPApplication.logE("RingtonePreferenceX.initRingtoneUri", "ringtoneUri="+ringtoneUri);
    }

    void persistValue() {
        if (shouldPersist())
        {
            // save to preferences
            persistString(ringtoneUri);

            // and notify
            notifyChanged();
        }
    }

    // From DialogPreference
    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR =
                new Creator<RingtonePreferenceX.SavedState>() {
                    public RingtonePreferenceX.SavedState createFromParcel(Parcel in) {
                        return new RingtonePreferenceX.SavedState(in);
                    }

                    public RingtonePreferenceX.SavedState[] newArray(int size) {
                        return new RingtonePreferenceX.SavedState[size];
                    }
                };
        boolean isDialogShowing;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
        }
    }

}
