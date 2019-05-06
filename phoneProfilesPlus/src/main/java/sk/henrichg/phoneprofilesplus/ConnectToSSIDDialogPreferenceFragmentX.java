package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.preference.PreferenceDialogFragmentCompat;

public class ConnectToSSIDDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConnectToSSIDDialogPreferenceX preference;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConnectToSSIDPreferenceAdapterX listAdapter;

    private AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ConnectToSSIDDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_connect_to_ssid_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.connect_to_ssid_pref_dlg_listview);
        linlaProgress = view.findViewById(R.id.connect_to_ssid_pref_dlg_linla_progress);

        listAdapter = new ConnectToSSIDPreferenceAdapterX(prefContext, preference);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                preference.value = preference.ssidList.get(position).ssid;
                listAdapter.notifyDataSetChanged();
            }
        });

        asyncTask = new AsyncTask<Void, Integer, Void>() {

            List<WifiSSIDData> _SSIDList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                //Log.d("ConnectToSSIDDialogPreference.onShow","onPreExecute");

                _SSIDList = new ArrayList<>();

                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                WifiScanJob.fillWifiConfigurationList(prefContext);
                List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(prefContext);
                if (wifiConfigurationList != null)
                {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                    {
                        //if ((wifiConfiguration.ssid != null) && (wifiConfiguration.bssid != null)) {
                        // bssid is null from configuration list
                        if (wifiConfiguration.ssid != null)
                            _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid/*.replace("\"", "")*/, wifiConfiguration.bssid, false, true, false));
                    }
                }

                Collections.sort(_SSIDList, new ConnectToSSIDDialogPreferenceFragmentX.SortList());

                //if (preference.disableSharedProfile == 0)
                //    _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_SHAREDPROFILE, "", false, false, false));
                _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_JUSTANY, "", false, false, false));

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                //Log.d("ConnectToSSIDDialogPreference.onShow","onPostExecute");

                preference.ssidList = new ArrayList<>(_SSIDList);
                listView.setAdapter(listAdapter);

                linlaProgress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

        }.execute();

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (preference.callChangeListener(preference.value))
        {
            preference.persistValue();
            preference.setSummaryCTSDP();
        }

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        preference.fragment = null;
    }

    private class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }


}
