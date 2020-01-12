package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
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
import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class RingtonePreferenceX extends DialogPreference {

    RingtonePreferenceFragmentX fragment;

    String ringtoneUri;
    private String defaultValue;
    private boolean savedInstanceState;

    //String oldRingtoneUri;

    private final String ringtoneType;
    private final boolean showSilent;
    private final boolean showDefault;

    final Map<String, String> toneList = new LinkedHashMap<>();
    AsyncTask asyncTask = null;

    private final Context prefContext;

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

        // set ringtoneUri to default
        ringtoneUri = "";
        if (!showSilent && showDefault) {
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
        }

        prefContext = context;

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // set ringtone uri from preference value
        String value = getPersistedString((String) defaultValue);
        String[] splits = value.split("\\|");
        ringtoneUri = splits[0];
        this.defaultValue = (String)defaultValue;
        setSummary("");
        setRingtone("", true);
    }

    void refreshListView() {
        if (PPApplication.logEnabled()) {
            PPApplication.logE("RingtonePreferenceX.refreshListView", "fragment=" + fragment);
            if (fragment != null) {
                PPApplication.logE("RingtonePreferenceX.refreshListView", "fragment.getDialog()=" + fragment.getDialog());
                if (fragment.getDialog() != null)
                    PPApplication.logE("RingtonePreferenceX.refreshListView", "fragment.getDialog().isShowing()=" + fragment.getDialog().isShowing());
            }
        }
        if ((fragment != null) && (fragment.getDialog() != null) && fragment.getDialog().isShowing()) {
            if (Permissions.checkRingtonePreference(prefContext)) {

                asyncTask = new AsyncTask<Void, Integer, Void>() {

                    //Ringtone defaultRingtone;
                    private final Map<String, String> _toneList = new LinkedHashMap<>();

                    @Override
                    protected Void doInBackground(Void... params) {
                        RingtoneManager manager = new RingtoneManager(prefContext);

                        Uri uri;// = null;
                    /*//noinspection ConstantConditions
                    switch (ringtoneType) {
                        case "ringtone":
                            uri = Settings.System.DEFAULT_RINGTONE_URI;
                            break;
                        case "notification":
                            uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                            break;
                        case "alarm":
                            uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                            break;
                    }

                    defaultRingtone = RingtoneManager.getRingtone(prefContext, uri);*/

                        Ringtone _ringtone;

                        switch (ringtoneType) {
                            case "ringtone":
                                manager.setType(RingtoneManager.TYPE_RINGTONE);
                                if (showDefault) {
                                    uri = Settings.System.DEFAULT_RINGTONE_URI;
                                    _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                                    String ringtoneName;
                                    try {
                                        ringtoneName = _ringtone.getTitle(prefContext);
                                    } catch (Exception e) {
                                        ringtoneName = prefContext.getString(R.string.ringtone_preference_default_ringtone);
                                    }
                                    _toneList.put(Settings.System.DEFAULT_RINGTONE_URI.toString(), ringtoneName);
                                }
                                break;
                            case "notification":
                                manager.setType(RingtoneManager.TYPE_NOTIFICATION);
                                if (showDefault) {
                                    uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                                    _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                                    String ringtoneName;
                                    try {
                                        ringtoneName = _ringtone.getTitle(prefContext);
                                    } catch (Exception e) {
                                        ringtoneName = prefContext.getString(R.string.ringtone_preference_default_notification);
                                    }
                                    _toneList.put(Settings.System.DEFAULT_NOTIFICATION_URI.toString(), ringtoneName);
                                }
                                break;
                            case "alarm":
                                manager.setType(RingtoneManager.TYPE_ALARM);
                                if (showDefault) {
                                    uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                                    _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                                    String ringtoneName;
                                    try {
                                        ringtoneName = _ringtone.getTitle(prefContext);
                                    } catch (Exception e) {
                                        ringtoneName = prefContext.getString(R.string.ringtone_preference_default_alarm);
                                    }
                                    _toneList.put(Settings.System.DEFAULT_ALARM_ALERT_URI.toString(), ringtoneName);
                                }
                                break;
                        }

                        if (showSilent)
                            _toneList.put("", prefContext.getString(R.string.ringtone_preference_none));

                        try {
                            Cursor cursor = manager.getCursor();

                        /*
                        profile._soundRingtone=content://settings/system/ringtone
                        profile._soundNotification=content://settings/system/notification_sound
                        profile._soundAlarm=content://settings/system/alarm_alert
                        */

                            while (cursor.moveToNext()) {
                                String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                                String _title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                                String _id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                                _toneList.put(_uri + "/" + _id, _title);
                            }
                        } catch (Exception ignored) {
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);

                        toneList.clear();
                        toneList.putAll(_toneList);

                        /*if (defaultRingtone == null) {
                            // ringtone not found
                            //View positive = getButton(DialogInterface.BUTTON_POSITIVE);
                            //positive.setEnabled(false);
                            setPositiveButtonText(null);
                        }*/

                        if (fragment != null)
                            fragment.updateListView(true);
                    }

                }.execute();
            }
        }
    }

    void setRingtone(String newRingtoneUri, boolean onlySetName)
    {
        if (!onlySetName)
            ringtoneUri = newRingtoneUri;

        new AsyncTask<Void, Integer, Void>() {

            private String ringtoneName;

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

            if (fragment != null)
                fragment.updateListView(false);
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

                        Context appContext = prefContext.getApplicationContext();

                        if (TonesHandler.isPhoneProfilesSilent(_ringtoneUri, appContext)) {
                            String filename = appContext.getResources().getResourceEntryName(TonesHandler.TONE_ID) + ".ogg";
                            File soundFile = new File(appContext.getFilesDir(), filename);
                            // /data/user/0/sk.henrichg.phoneprofilesplus/files
                            PPApplication.logE("RingtonePreferenceX.playRingtone", "soundFile=" + soundFile);
                            mediaPlayer.setDataSource(soundFile.getAbsolutePath());
                        }
                        else
                            mediaPlayer.setDataSource(appContext, _ringtoneUri);

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

                        PPApplication.logE("RingtonePreferenceX.playRingtone", "ringtoneVolume=" + ringtoneVolume);

                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) ringtoneVolume / maximumRingtoneValue * 100.0f;
                        int mediaVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("RingtonePreferenceX.playRingtone", "mediaVolume=" + mediaVolume);

                        ActivateProfileHelper.setMediaVolume(prefContext, audioManager, mediaVolume);

                        mediaPlayer.start();
                        ringtoneIsPlayed = true;

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
                                    PPApplication.logE("RingtonePreferenceX.playRingtone", "play stopped");
                                }

                                ringtoneIsPlayed = false;
                                mediaPlayer = null;

                                OneTimeWorkRequest disableInternalChangeWorker =
                                        new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                                .addTag("disableInternalChangeWork")
                                                .setInitialDelay(3, TimeUnit.SECONDS)
                                                .build();
                                try {
                                    WorkManager workManager = WorkManager.getInstance(prefContext);
                                    workManager.cancelUniqueWork("disableInternalChangeWork");
                                    workManager.cancelAllWorkByTag("disableInternalChangeWork");
                                    workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                                } catch (Exception ignored) {}

                                /*PPApplication.startHandlerThreadInternalChangeToFalse();
                                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);*/
                                //PostDelayedBroadcastReceiver.setAlarm(
                                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);

                                playTimer = null;
                            }
                        }, mediaPlayer.getDuration());

                    } catch (SecurityException e) {
                        PPApplication.logE("RingtonePreferenceX.playRingtone", "security exception");
                        stopPlayRingtone();

                        OneTimeWorkRequest disableInternalChangeWorker =
                                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                        .addTag("disableInternalChangeWork")
                                        .setInitialDelay(3, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = WorkManager.getInstance(prefContext);
                            workManager.cancelUniqueWork("disableInternalChangeWork");
                            workManager.cancelAllWorkByTag("disableInternalChangeWork");
                            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                        } catch (Exception ignored) {}

                        /*PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);*/
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);
                    } catch (Exception e) {
                        PPApplication.logE("RingtonePreferenceX.playRingtone", Log.getStackTraceString(e));
                        stopPlayRingtone();

                        OneTimeWorkRequest disableInternalChangeWorker =
                                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                        .addTag("disableInternalChangeWork")
                                        .setInitialDelay(3, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = WorkManager.getInstance(prefContext);
                            workManager.cancelUniqueWork("disableInternalChangeWork");
                            workManager.cancelAllWorkByTag("disableInternalChangeWork");
                            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                        } catch (Exception ignored) {}

                        /*PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);*/
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, prefContext);
                    }
                }
            });

        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            if (fragment != null) {
                final int position = fragment.getRingtonePosition();
                if (position != -1) {
                    // save to preferences
                    persistString(ringtoneUri);

                    // and notify
                    notifyChanged();
                }
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            String value = getPersistedString(defaultValue);
            String[] splits = value.split("\\|");
            ringtoneUri = splits[0];
            setSummary("");
            setRingtone("", true);
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        savedInstanceState = true;

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
        myState.ringtoneUri = ringtoneUri;
        myState.defaultValue = defaultValue;
        //myState.oldRingtoneUri = oldRingtoneUri;

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
            setRingtone("", true);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        ringtoneUri = myState.ringtoneUri;
        defaultValue = myState.defaultValue;
        //oldRingtoneUri = myState.oldRingtoneUri;

        PPApplication.logE("RingtonePreferenceX.onRestoreInstanceState", "ringtoneUri="+ringtoneUri);

        setRingtone("", true);
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

        String ringtoneUri;
        String defaultValue;

        //String oldRingtoneUri;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source) {
            super(source);
            ringtoneUri = source.readString();
            defaultValue = source.readString();
            //oldRingtoneUri = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(ringtoneUri);
            dest.writeString(defaultValue);
            //dest.writeString(oldRingtoneUri);
        }
    }

}
