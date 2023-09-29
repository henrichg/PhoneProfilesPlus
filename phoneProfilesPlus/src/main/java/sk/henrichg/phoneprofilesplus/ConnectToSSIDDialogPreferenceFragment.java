package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConnectToSSIDDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ConnectToSSIDDialogPreference preference;

    private ListView listView;
    private LinearLayout linlaProgress;
    private LinearLayout linLaListView;

    private ConnectToSSIDPreferenceAdapter listAdapter;

    boolean wifiEnabled;
    private RefreshListView1AsyncTask asyncTask1 = null;
    private RefreshListView2AsyncTask asyncTask2 = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ConnectToSSIDDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_connect_to_ssid_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.connect_to_ssid_pref_dlg_listview);
        linlaProgress = view.findViewById(R.id.connect_to_ssid_pref_dlg_linla_progress);
        linLaListView = view.findViewById(R.id.connect_to_ssid_pref_dlg_root2);
        TextView helpTextView = view.findViewById(R.id.connect_to_ssid_pref_dlg_help);

        listAdapter = new ConnectToSSIDPreferenceAdapter(prefContext, preference);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            preference.value = preference.ssidList.get(position).ssid;
            listAdapter.notifyDataSetChanged();
        });

        helpTextView.setText(getString(R.string.connect_to_ssid_dialog_help) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
        helpTextView.setOnClickListener(v -> {
            boolean ok = false;
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            //intent.setData(Uri.parse(PPApplication.DATA_PACKAGE+PPApplication.PACKAGE_NAME));
            if (GlobalGUIRoutines.activityIntentExists(intent, prefContext)) {
                try {
                    startActivity(intent);
                    ok = true;
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            if (!ok){
                PPAlertDialog dialog = new PPAlertDialog(
                        preference.getTitle(),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        getActivity()
                );

                if ((getActivity() != null) && (!getActivity().isFinishing()))
                    dialog.show();
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

        if ((asyncTask1 != null) && asyncTask1.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask1.cancel(true);
        asyncTask1 = null;
        if ((asyncTask2 != null) && asyncTask2.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask2.cancel(true);
        asyncTask2 = null;

        preference.fragment = null;
    }

    void refreshListView() {
        asyncTask1 = new RefreshListView1AsyncTask(preference, this, prefContext);
        asyncTask1.execute();
    }

    private static class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }

    private static class RefreshListView1AsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<ConnectToSSIDDialogPreference> preferenceWeakRef;
        private final WeakReference<ConnectToSSIDDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListView1AsyncTask(ConnectToSSIDDialogPreference preference,
                                         ConnectToSSIDDialogPreferenceFragment fragment,
                                         Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ConnectToSSIDDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.linLaListView.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConnectToSSIDDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConnectToSSIDDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                try {
                    WifiManager wifi = (WifiManager) prefContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifi != null) {
                        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                            fragment.wifiEnabled = true;

                            //if (Build.VERSION.SDK_INT >= 29)
                            //    CmdWifi.setWifi(true);
                            wifi.setWifiEnabled(true);

                            GlobalUtils.sleep(3000);
                        } else
                            WifiScanWorker.fillWifiConfigurationList(prefContext.getApplicationContext());
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ConnectToSSIDDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConnectToSSIDDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.asyncTask2 = new RefreshListView2AsyncTask(preference, fragment, prefContext);
                fragment.asyncTask2.execute();
            }
        }
    }

    private static class RefreshListView2AsyncTask extends AsyncTask<Void, Integer, Void> {

        List<WifiSSIDData> _SSIDList = null;

        private final WeakReference<ConnectToSSIDDialogPreference> preferenceWeakRef;
        private final WeakReference<ConnectToSSIDDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListView2AsyncTask(ConnectToSSIDDialogPreference preference,
                                         ConnectToSSIDDialogPreferenceFragment fragment,
                                         Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _SSIDList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConnectToSSIDDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConnectToSSIDDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(prefContext);

                if (fragment.wifiEnabled) {
                    try {
                        WifiManager wifi = (WifiManager) prefContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifi != null) {
                            //if (Build.VERSION.SDK_INT >= 29)
                            //    CmdWifi.setWifi(false);
                            wifi.setWifiEnabled(false);
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                fragment.wifiEnabled = false;

                //if (wifiConfigurationList != null) {
                for (WifiSSIDData wifiConfiguration : wifiConfigurationList) {
                    //if ((wifiConfiguration.ssid != null) && (wifiConfiguration.bssid != null)) {
                    // bssid is null from configuration list
                    if (wifiConfiguration.ssid != null)
                        _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid/*.replace("\"", "")*/, /*wifiConfiguration.bssid,*/ false, true, false));
                }
                //}

                _SSIDList.sort(new SortList());

                //if (preference.disableSharedProfile == 0)
                //    _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_SHAREDPROFILE, "", false, false, false));
                _SSIDList.add(0, new WifiSSIDData(StringConstants.CONNECTTOSSID_JUSTANY, /*"",*/ false, false, false));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ConnectToSSIDDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ConnectToSSIDDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.linLaListView.setVisibility(View.VISIBLE);

                    preference.ssidList = new ArrayList<>(_SSIDList);
                    fragment.listView.setAdapter(fragment.listAdapter);
                });
            }
        }

    }

}
