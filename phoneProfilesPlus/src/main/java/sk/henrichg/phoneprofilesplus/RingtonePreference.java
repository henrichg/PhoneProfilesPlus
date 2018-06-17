package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RingtonePreference extends DialogPreference {

    String ringtone;

    private final String ringtoneType;
    private final boolean showSilent;
    private final boolean showDefault;

    private final Context prefContext;
    private MaterialDialog mDialog;
    private ListView listView;

    private final Map<String, String> toneList = new LinkedHashMap<>();
    private RingtonePreferenceAdapter listAdapter;

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

        ringtone = "";
        if (ringtoneType != null) {
            if (ringtoneType.equals("ringtone"))
                ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
            else if (ringtoneType.equals("notification"))
                ringtone = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
            else if (ringtoneType.equals("alarm"))
                ringtone = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
        }

        prefContext = context;

        typedArray.recycle();

    }

    protected void showDialog(Bundle state) {
        PPApplication.logE("RingtonePreference.showDialog", "xx");

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .customView(R.layout.activity_ringtone_pref_dialog, false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // set summary
                            PPApplication.logE("RingtonePreference._setSummary", "OK button");
                            _setSummary(ringtone);

                            // save to preferences
                            persistString(ringtone);

                            // and notify
                            notifyChanged();

                            mDialog.dismiss();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                    }
                });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                RingtonePreference.this.onShow(/*dialog*/);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapter.ViewHolder viewHolder = (RingtonePreferenceAdapter.ViewHolder) item.getTag();
                setRingtone((String)listAdapter.getItem(position)/*, viewHolder.radioBtn*/);
                viewHolder.radioBtn.setChecked(true);
                playRingtone();
            }
        });

        listAdapter = new RingtonePreferenceAdapter(this, prefContext, toneList);
        listView.setAdapter(listAdapter);

        String value;
        try {
            value = getPersistedString(ringtone);
        } catch  (Exception e) {
            value = ringtone;
        }
        ringtone = value;
        PPApplication.logE("RingtonePreference.showDialog", "ringtone="+ringtone);

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext, this))
            refreshListView();
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);
        PPApplication.logE("RingtonePreference.onDismiss", "ringtone="+ringtone);
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
        PPApplication.logE("RingtonePreference.onActivityDestroy", "ringtone="+ringtone);

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
                value = getPersistedString(ringtone);
            } catch  (Exception e) {
                value = ringtone;
            }
            ringtone = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            ringtone = value;
            persistString(value);
        }
        PPApplication.logE("RingtonePreference._setSummary", "onSetInitialValue");
        _setSummary(ringtone);
    }

    private void _setSummary(String ringtone)
    {
        String ringtoneName;

        /*if (ringtone.equals(Settings.System.DEFAULT_RINGTONE_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_ringtone);
        else
        if (ringtone.equals(Settings.System.DEFAULT_NOTIFICATION_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_notification);
        else
        if (ringtone.equals(Settings.System.DEFAULT_ALARM_ALERT_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_alarm);
        else*/
        if (ringtone.isEmpty())
            ringtoneName = prefContext.getString(R.string.ringtone_preference_none);
        else {
            /*try {
                Uri ringtoneUri = Uri.parse(ringtone);

                ContentResolver cr = getContext().getContentResolver();
                String[] projection = {MediaStore.MediaColumns.TITLE};
                //String title;
                Cursor cur = cr.query(ringtoneUri, projection, null, null, null);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        ringtoneName = cur.getString(0);
                    }
                    cur.close();
                }
            } catch (Exception ignored) {
            }*/
            Uri uri = Uri.parse(ringtone);
            Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
            try {
                ringtoneName = _ringtone.getTitle(prefContext);
            } catch (Exception e) {
                ringtoneName = prefContext.getString(R.string.ringtone_preference_not_set);
            }
        }

        PPApplication.logE("RingtonePreference._setSummary", "ringtoneName="+ringtoneName);

        setSummary(ringtoneName);
    }

    void setRingtone(String newRingtone/*, RadioButton newCheckedRadioButton*/)
    {
        ringtone = newRingtone;

        View positive = mDialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(true);

        listAdapter.notifyDataSetChanged();

        // set summary
        //_setSummary(ringtone);
    }

    void refreshListView() {
        RingtoneManager manager = new RingtoneManager(prefContext);

        Uri uri = null;
        if (ringtoneType.equals("ringtone"))
            uri = Settings.System.DEFAULT_RINGTONE_URI;
        else
        if (ringtoneType.equals("notification"))
            uri = Settings.System.DEFAULT_NOTIFICATION_URI;
        else
        if (ringtoneType.equals("alarm"))
            uri = Settings.System.DEFAULT_ALARM_ALERT_URI;

        Ringtone _ringtone = RingtoneManager.getRingtone(prefContext, uri);
        if (_ringtone == null) {
            // ringtone not found
            View positive = mDialog.getActionButton(DialogAction.POSITIVE);
            positive.setEnabled(false);
        }

        if (ringtoneType.equals("ringtone")) {
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
                toneList.put(Settings.System.DEFAULT_RINGTONE_URI.toString(), ringtoneName);
            }
        }
        else
        if (ringtoneType.equals("notification")) {
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
                toneList.put(Settings.System.DEFAULT_NOTIFICATION_URI.toString(), ringtoneName);
            }
        }
        else
        if (ringtoneType.equals("alarm")) {
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
                toneList.put(Settings.System.DEFAULT_ALARM_ALERT_URI.toString(), ringtoneName);
            }
        }

        if (showSilent)
            toneList.put("", prefContext.getString(R.string.ringtone_preference_none));

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
            toneList.put(_uri + "/" + _id, _title);
        }

        listAdapter.notifyDataSetChanged();

        List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
        final int position = uris.indexOf(ringtone);
        listView.setSelection(position);

        PPApplication.logE("RingtonePreference._setSummary", "refreshListView");
        _setSummary(ringtone);
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

            final Uri ringtoneUri = Uri.parse(ringtone);

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
                        mediaPlayer.setDataSource(prefContext, ringtoneUri);
                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);

                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        int ringtoneVolume = 0;
                        int maximumRingtoneValue = 0;

                        if (ringtoneType.equals("ringtone")) {
                            ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                            maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        } else if (ringtoneType.equals("notification")) {
                            ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                            maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        } else if (ringtoneType.equals("alarm")) {
                            ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                            maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
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

                                PPApplication.startHandlerThreadPlayTone();
                                final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);

                                playTimer = null;
                            }
                        }, mediaPlayer.getDuration());

                    } catch (SecurityException e) {
                        PPApplication.logE("RingtonePreference.playRingtone", "security exception");
                        stopPlayRingtone();
                        PPApplication.startHandlerThreadPlayTone();
                        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
                    } catch (Exception e) {
                        PPApplication.logE("RingtonePreference.playRingtone", "exception");
                        stopPlayRingtone();
                        PPApplication.startHandlerThreadPlayTone();
                        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
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
