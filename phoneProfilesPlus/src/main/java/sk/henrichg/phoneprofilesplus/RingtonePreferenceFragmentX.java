package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.preference.PreferenceDialogFragmentCompat;

public class RingtonePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    RingtonePreferenceX preference;

    private Context prefContext;
    private ListView listView;

    private final Map<String, String> toneList = new LinkedHashMap<>();

    private AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (RingtonePreferenceX) getPreference();

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_ringtone_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        //noinspection ConstantConditions
        listView = view.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapter.ViewHolder viewHolder = (RingtonePreferenceAdapter.ViewHolder) item.getTag();
                preference.setRingtone((String)preference.listAdapter.getItem(position), false);
                viewHolder.radioBtn.setChecked(true);
                preference.playRingtone();
            }
        });

        preference.listAdapter = new RingtonePreferenceAdapterX(this, prefContext, toneList);
        listView.setAdapter(preference.listAdapter);

        preference.initRingtoneUri();

        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            preference.oldRingtoneUri = preference.ringtoneUri;
            refreshListView();
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        PPApplication.logE("RingtonePreferenceX.onDialogClosed", "ringtoneUri="+preference.ringtoneUri);
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                preference.stopPlayRingtone();
            }
        });

        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.setRingtone(preference.oldRingtoneUri, false);
        }
    }

    void refreshListView() {
        if ((getDialog() != null) && getDialog().isShowing()) {

            asyncTask = new AsyncTask<Void, Integer, Void>() {

                Ringtone defaultRingtone;
                private final Map<String, String> _toneList = new LinkedHashMap<>();

                @Override
                protected Void doInBackground(Void... params) {
                    if (preference != null) {
                        RingtoneManager manager = new RingtoneManager(prefContext);

                        Uri uri = null;
                        //noinspection ConstantConditions
                        switch (preference.ringtoneType) {
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

                        //noinspection ConstantConditions
                        switch (preference.ringtoneType) {
                            case "ringtone":
                                manager.setType(RingtoneManager.TYPE_RINGTONE);
                                if (preference.showDefault) {
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
                                if (preference.showDefault) {
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
                                if (preference.showDefault) {
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

                        if (preference.showSilent)
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
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    toneList.clear();
                    toneList.putAll(_toneList);

                    if (defaultRingtone == null) {
                        // ringtone not found
                        //View positive = getButton(DialogInterface.BUTTON_POSITIVE);
                        //positive.setEnabled(false);
                        preference.setPositiveButtonText(null);
                    }

                    preference.listAdapter.notifyDataSetChanged();

                    List<String> uris = new ArrayList<>(preference.listAdapter.toneList.keySet());
                    final int position = uris.indexOf(preference.ringtoneUri);
                    listView.setSelection(position);
                }

            }.execute();
        }
    }

}
