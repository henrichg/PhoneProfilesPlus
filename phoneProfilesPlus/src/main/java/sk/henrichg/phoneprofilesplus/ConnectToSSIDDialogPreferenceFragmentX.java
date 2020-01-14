package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
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

@SuppressWarnings("WeakerAccess")
public class ConnectToSSIDDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConnectToSSIDDialogPreferenceX preference;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConnectToSSIDPreferenceAdapterX listAdapter;

    boolean wifiEnabled;
    private AsyncTask asyncTask1 = null;
    private AsyncTask asyncTask2 = null;

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

        wifiEnabled = false;

        if (Permissions.grantConnectToSSIDDialogPermissions(prefContext))
            refreshListView();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (preference.callChangeListener(preference.value))
                preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask1 != null) && !asyncTask1.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask1.cancel(true);
        }
        if ((asyncTask2 != null) && !asyncTask2.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask2.cancel(true);
        }

        preference.fragment = null;
    }

    void refreshListView() {
        asyncTask1 = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    WifiManager wifi = (WifiManager) prefContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifi != null) {
                        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                            //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.1", "enable wifi");
                            wifiEnabled = true;
                            wifi.setWifiEnabled(true);
                            PPApplication.sleep(3000);
                        }
                        else
                            WifiScanWorker.fillWifiConfigurationList(prefContext.getApplicationContext());
                    }
                } catch (Exception ignored) {
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.1", "call async 2");

                asyncTask2 = new AsyncTask<Void, Integer, Void>() {

                    List<WifiSSIDData> _SSIDList = null;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "onPreExecute");

                        _SSIDList = new ArrayList<>();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "doInBackground");

                        List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(prefContext);
                        //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "wifiConfigurationList.size()="+wifiConfigurationList.size());

                        if (wifiEnabled) {
                            try {
                                WifiManager wifi = (WifiManager) prefContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifi != null)
                                    wifi.setWifiEnabled(false);
                                //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "wifi disabled");
                            } catch (Exception ignored) {
                            }
                        }
                        wifiEnabled = false;

                        //if (wifiConfigurationList != null) {
                            for (WifiSSIDData wifiConfiguration : wifiConfigurationList) {
                                //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "wifiConfiguration.ssid="+wifiConfiguration.ssid);
                                //if ((wifiConfiguration.ssid != null) && (wifiConfiguration.bssid != null)) {
                                // bssid is null from configuration list
                                if (wifiConfiguration.ssid != null)
                                    _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid/*.replace("\"", "")*/, wifiConfiguration.bssid, false, true, false));
                            }
                        //}

                        Collections.sort(_SSIDList, new ConnectToSSIDDialogPreferenceFragmentX.SortList());

                        //if (preference.disableSharedProfile == 0)
                        //    _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_SHAREDPROFILE, "", false, false, false));
                        _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_JUSTANY, "", false, false, false));

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);

                        //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "onPostExecute");

                        preference.ssidList = new ArrayList<>(_SSIDList);
                        listView.setAdapter(listAdapter);

                        linlaProgress.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }

                }.execute();
            }

        }.execute();
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
