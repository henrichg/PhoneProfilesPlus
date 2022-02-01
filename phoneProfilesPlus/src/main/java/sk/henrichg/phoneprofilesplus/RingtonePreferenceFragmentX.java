package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.ArrayList;
import java.util.List;

public class RingtonePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    RingtonePreferenceX preference;

    private RingtonePreferenceAdapterX listAdapter;
    private LinearLayout linlaProgress;
    private ListView listView;

    private Context prefContext;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (RingtonePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_ringtone_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.ringtone_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.ringtone_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            RingtonePreferenceAdapterX.ViewHolder viewHolder = (RingtonePreferenceAdapterX.ViewHolder) item.getTag();
            preference.setRingtone((String)listAdapter.getItem(position), false);
            viewHolder.radioBtn.setChecked(true);
            preference.playRingtone();
        });

        listAdapter = new RingtonePreferenceAdapterX(this, prefContext, preference.toneList);
        listView.setAdapter(listAdapter);

        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            Handler handler = new Handler(prefContext.getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=RingtonePreferenceFragmentX.onBindDialogView");
                //preference.oldRingtoneUri = preference.ringtoneUri;
                preference.refreshListView();
            }, 200);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((preference.asyncTask != null) && !preference.asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            preference.asyncTask.cancel(true);
        }

        preference.stopPlayRingtone();

        preference.fragment = null;
    }

//    void showProgress() {
//        listView.setVisibility(View.GONE);
//        linlaProgress.setVisibility(View.VISIBLE);
//    }

    void updateListView(boolean alsoSelection) {
        linlaProgress.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();

            if (alsoSelection) {
                List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
                final int position = uris.indexOf(preference.ringtoneUri);
                listView.setSelection(position);
            }
        }
    }

    int getRingtonePosition() {
        List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
        return uris.indexOf(preference.ringtoneUri);
    }

}
