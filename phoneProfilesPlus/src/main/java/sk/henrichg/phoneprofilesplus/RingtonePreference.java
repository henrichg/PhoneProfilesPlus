package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RingtonePreference extends DialogPreference {
    String ringtoneUri;
    private String oldRingtoneUri;
    private String ringtoneName;

    private final String ringtoneType;
    private final boolean showSilent;
    private final boolean showDefault;

    private final Context prefContext;
    private AlertDialog mDialog;
    private ListView listView;

    private final Map<String, String> toneList = new LinkedHashMap<>();
    private RingtonePreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    private static MediaPlayer mediaPlayer = null;
    private static int oldMediaVolume = -1;
    private static Timer playTimer = null;
    private static boolean ringtoneIsPlayed = false;

    public RingtonePreference(Context context, AttributeSet attrs)
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

    protected void showDialog(Bundle state) {
        PPApplication.logE("RingtonePreference.showDialog", "xx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // set summary
                PPApplication.logE("RingtonePreference._setSummary", "OK button");
                setRingtone(oldRingtoneUri, false);
            }
        });
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist())
                {
                    // save to preferences
                    persistString(ringtoneUri);

                    // and notify
                    notifyChanged();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_ringtone_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                RingtonePreference.this.onShow(/*dialog*/);
            }
        });

        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapter.ViewHolder viewHolder = (RingtonePreferenceAdapter.ViewHolder) item.getTag();
                setRingtone((String)listAdapter.getItem(position), false);
                viewHolder.radioBtn.setChecked(true);
                playRingtone();
            }
        });

        listAdapter = new RingtonePreferenceAdapter(this, prefContext, toneList);
        listView.setAdapter(listAdapter);

        String value;
        try {
            value = getPersistedString(ringtoneUri);
        } catch  (Exception e) {
            value = ringtoneUri;
        }
        ringtoneUri = value;
        PPApplication.logE("RingtonePreference.showDialog", "ringtoneUri="+ringtoneUri);

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            oldRingtoneUri = ringtoneUri;
            refreshListView();
        }
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        PPApplication.logE("RingtonePreference.onDismiss", "ringtoneUri="+ringtoneUri);
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopPlayRingtone();
            }
        });
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        PPApplication.logE("RingtonePreference.onActivityDestroy", "ringtoneUri="+ringtoneUri);

        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopPlayRingtone();
            }
        });

        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
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
        Dialog dialog = getDialog();
        if ((dialog == null) || !dialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = dialog.onSaveInstanceState();

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
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value;
            try {
                value = getPersistedString(ringtoneUri);
            } catch  (Exception e) {
                value = ringtoneUri;
            }
            ringtoneUri = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            ringtoneUri = value;
            persistString(value);
        }
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
            View positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setEnabled(true);

            listAdapter.notifyDataSetChanged();
        }
    }

    void refreshListView() {
        if ((mDialog != null) && mDialog.isShowing()) {

            asyncTask = new AsyncTask<Void, Integer, Void>() {

                Ringtone defaultRingtone;
                private final Map<String, String> _toneList = new LinkedHashMap<>();

                @Override
                protected Void doInBackground(Void... params) {
                    RingtoneManager manager = new RingtoneManager(prefContext);

                    Uri uri = null;
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

                    defaultRingtone = RingtoneManager.getRingtone(prefContext, uri);

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
                    } catch (Exception ignored) {}

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    toneList.clear();
                    toneList.putAll(_toneList);

                    if (defaultRingtone == null) {
                        // ringtone not found
                        View positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        positive.setEnabled(false);
                    }

                    listAdapter.notifyDataSetChanged();

                    List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
                    final int position = uris.indexOf(ringtoneUri);
                    listView.setSelection(position);
                }

            }.execute();
        }
    }

    private void stopPlayRingtone() {
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
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
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

                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0);

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
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
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

    // From DialogPreference
    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        boolean isDialogShowing;
        Bundle dialogBundle;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }
    }

}
