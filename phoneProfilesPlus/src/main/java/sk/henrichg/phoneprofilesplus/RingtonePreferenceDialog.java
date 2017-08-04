package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RingtonePreferenceDialog implements PreferenceManager.OnActivityDestroyListener
{

    private RingtonePreference ringtonePreference;
    private String ringtoneType;
    private boolean showSilent;
    private boolean showDefault;
    private RingtonePreferenceAdapter ringtonePreferenceAdapter;

    private MaterialDialog mDialog;

    RingtonePreferenceDialog(Context context, RingtonePreference preference, String ringtone)
    {
        ringtonePreference = preference;

        ringtoneType = ringtonePreference.ringtoneType;
        showSilent = ringtonePreference.showSilent;
        showDefault = ringtonePreference.showDefault;

        Map<String, String> toneList = new LinkedHashMap<>();

        String title = "";

        RingtoneManager manager = new RingtoneManager(context);
        if (ringtoneType.equals("ringtone")) {
            title = context.getString(R.string.title_activity_ringtone_preference_dialog_ringtones);
            manager.setType(RingtoneManager.TYPE_RINGTONE);
            if (showDefault)
                toneList.put(Settings.System.DEFAULT_RINGTONE_URI.toString(),
                        context.getString(R.string.ringtone_preference_dialog_default_ringtone));
        }
        else
        if (ringtoneType.equals("notification")) {
            title = context.getString(R.string.title_activity_ringtone_preference_dialog_notifications);
            manager.setType(RingtoneManager.TYPE_NOTIFICATION);
            if (showDefault)
                toneList.put(Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                        context.getString(R.string.ringtone_preference_dialog_default_notification));
        }
        else
        if (ringtoneType.equals("alarm")) {
            title = context.getString(R.string.title_activity_ringtone_preference_dialog_alarms);
            manager.setType(RingtoneManager.TYPE_ALARM);
            if (showDefault)
                toneList.put(Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                        context.getString(R.string.ringtone_preference_dialog_default_alarm));
        }

        if (showSilent)
            toneList.put("", context.getString(R.string.ringtone_preference_dialog_silent));

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
        //List<Profile> profileList = ProfilePreference.dataWrapper.getProfileList();
        //Collections.sort(profileList, new AlphabeticallyComparator());

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(title)
                //.disableDefaultFonts()
                .autoDismiss(false)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(ringtonePreference, RingtonePreferenceDialog.this);
                    }
                })
                .customView(R.layout.activity_ringtone_pref_dialog, false);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(ringtonePreference, this);

        mDialog = dialogBuilder.build();

        ListView listView = (ListView)mDialog.getCustomView().findViewById(R.id.ringtone_pref_dlg_listview);

        ringtonePreferenceAdapter = new RingtonePreferenceAdapter(this, context, ringtone, toneList);
        listView.setAdapter(ringtonePreferenceAdapter);

        int position = 0;
        if (!ringtone.isEmpty())
        {
            List<String> uris = new ArrayList(toneList.keySet());
            position = uris.indexOf(ringtone);
        }
        listView.setSelection(position);
        listView.setItemChecked(position, true);
        listView.smoothScrollToPosition(position);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    void doOnItemSelected(int position)
    {
        List<String> uris = new ArrayList(ringtonePreferenceAdapter.toneList.keySet());
        ringtonePreference.setRingtone(uris.get(position));
        mDialog.dismiss();
    }

    @Override
    public void onActivityDestroy() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    /*
    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
        }
    }
    */

    public void show() {
        mDialog.show();
    }

}
