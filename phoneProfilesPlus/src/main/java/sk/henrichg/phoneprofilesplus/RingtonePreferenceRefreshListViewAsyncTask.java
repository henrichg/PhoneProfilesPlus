package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/** @noinspection ExtractMethodRecommender*/
class RingtonePreferenceRefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

    //Ringtone defaultRingtone;
    private final Map<String, String> _toneList = new LinkedHashMap<>();

    private final WeakReference<RingtonePreference> preferenceWeakRef;
    private final WeakReference<Context> prefContextWeakRef;

    public RingtonePreferenceRefreshListViewAsyncTask(RingtonePreference preference,
                                    Context prefContext) {
        this.preferenceWeakRef = new WeakReference<>(preference);
        this.prefContextWeakRef = new WeakReference<>(prefContext);
    }

        /*
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            RingtonePreference preference = preferenceWeakRef.get();
            if (preference != null) {
                if (preference.toneList.size() > 0) {
                    if (preference.fragment != null)
                        preference.fragment.hideProgress();
                }
            }

            //RingtonePreference preference = preferenceWeakRef.get();
            //if ((preference != null) && (preference.fragment != null))
            //    preference.fragment.showProgress();
        }
        */

    @Override
    protected Void doInBackground(Void... params) {
        RingtonePreference preference = preferenceWeakRef.get();
        Context prefContext = prefContextWeakRef.get();
        if ((preference != null) && (prefContext != null)) {

            RingtoneManager manager = new RingtoneManager(prefContext);

            Uri uri;// = null;
                        /*switch (ringtoneType) {
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

            if (preference.showSilent) {
                _toneList.put("", prefContext.getString(R.string.ringtone_preference_none));
            }

            Ringtone _ringtone;
            boolean typeIsSet = false;

            switch (preference.ringtoneType) {
                case RingtonePreference.RINGTONE_TYPE_RINGTONE:
                    manager.setType(RingtoneManager.TYPE_RINGTONE);
                    typeIsSet = true;
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
                case RingtonePreference.RINGTONE_TYPE_NOTIFICATION:
                    manager.setType(RingtoneManager.TYPE_NOTIFICATION);
                    typeIsSet = true;
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
                case RingtonePreference.RINGTONE_TYPE_ALARM:
                    manager.setType(RingtoneManager.TYPE_ALARM);
                    typeIsSet = true;
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

            if (typeIsSet) {
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

                        // for Samsung do not allow external tones
                        boolean add = true;
                        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                            if (preference.ringtoneType.equals(RingtonePreference.RINGTONE_TYPE_RINGTONE) && (preference.simCard != 0) && (!_uri.contains(StringConstants.RINGTONE_CONTENT_INTERNAL)))
                                add = false;
                            if (preference.ringtoneType.equals(RingtonePreference.RINGTONE_TYPE_NOTIFICATION) && (preference.simCard != 0) && (!_uri.contains(StringConstants.RINGTONE_CONTENT_INTERNAL)))
                                add = false;
                        }

                        if (add)
                            _toneList.put(_uri + "/" + _id, _title);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        RingtonePreference preference = preferenceWeakRef.get();
        Context prefContext = prefContextWeakRef.get();
        if ((preference != null) && (prefContext != null)) {
            preference.toneList.clear();
            preference.toneList.putAll(_toneList);

                        /*if (defaultRingtone == null) {
                            // ringtone not found
                            //View positive = getButton(DialogInterface.BUTTON_POSITIVE);
                            //positive.setEnabled(false);
                            setPositiveButtonText(null);
                        }*/

            if (preference.fragment != null)
                preference.fragment.updateListView(true);
        }
    }

}
