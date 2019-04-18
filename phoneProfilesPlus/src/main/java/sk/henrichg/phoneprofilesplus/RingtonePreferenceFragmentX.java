package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class RingtonePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    RingtonePreferenceX preference;

    private Context prefContext;

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

        preference.listView = view.findViewById(R.id.ringtone_pref_dlg_listview);

        preference.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                RingtonePreferenceAdapterX.ViewHolder viewHolder = (RingtonePreferenceAdapterX.ViewHolder) item.getTag();
                preference.setRingtone((String)preference.listAdapter.getItem(position), false);
                viewHolder.radioBtn.setChecked(true);
                preference.playRingtone();
            }
        });

        preference.listAdapter = new RingtonePreferenceAdapterX(this, prefContext, preference.toneList);
        preference.listView.setAdapter(preference.listAdapter);

        preference.initRingtoneUri();

        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            Handler handler = new Handler(prefContext.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    preference.oldRingtoneUri = preference.ringtoneUri;
                    preference.refreshListView();
                }
            }, 200);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((preference.asyncTask != null) && !preference.asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            preference.asyncTask.cancel(true);
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

}
