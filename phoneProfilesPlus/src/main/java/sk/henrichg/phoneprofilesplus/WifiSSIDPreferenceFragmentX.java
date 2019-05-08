package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceDialogFragmentCompat;

public class WifiSSIDPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private WifiSSIDPreferenceX preference;

    private AlertDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView SSIDListView;
    private EditText SSIDName;
    private AppCompatImageButton addIcon;
    private WifiSSIDPreferenceAdapterX listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (WifiSSIDPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_wifi_ssid_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        PPApplication.forceRegisterReceiversForWifiScanner(prefContext);
        WifiSSIDPreferenceX.forceRegister = true;

        progressLinearLayout = view.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = view.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = SSIDName.getText().toString();
                preference.addSSID(ssid);
                boolean found = false;
                for (WifiSSIDData customSSIDData : preference.customSSIDList) {
                    if (customSSIDData.ssid.equals(ssid)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    preference.customSSIDList.add(new WifiSSIDData(ssid, "", true, false, false));
                refreshListView(false, ssid);
            }
        });

        SSIDName = view.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
        SSIDName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GlobalGUIRoutines.setImageButtonEnabled(!SSIDName.getText().toString().isEmpty(),
                        addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!SSIDName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());

        SSIDListView = view.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapterX(prefContext, preference);
        SSIDListView.setAdapter(listAdapter);

        SSIDListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String ssid = preference.SSIDList.get(position).ssid;
                if (!(ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE) ||
                        ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE))) {
                    SSIDName.setText(ssid);
                }
            }

        });

        final ImageView helpIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString = getString(R.string.event_preference_wifi_ssidName_type)+"\n\n"+
                        getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                        getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                        getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                        getString(R.string.pref_dlg_info_about_wildcards_3);

                DialogHelpPopupWindowX.showPopup(helpIcon, (Activity)prefContext, getDialog(), helpString);
            }
        });

        ImageView changeSelectionIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new AlertDialog.Builder(prefContext)
                        .setTitle(R.string.pref_dlg_change_selection_title)
                        .setCancelable(true)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setItems(R.array.wifiSSIDChangeSelectionArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        preference.value = "";
                                        break;
                                    case 1:
                                        for (WifiSSIDData ssid : preference.SSIDList) {
                                            if (ssid.ssid.equals(SSIDName.getText().toString()))
                                                preference.addSSID(ssid.ssid);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView(false, "");
                            }
                        })
                        .show();
            }
        });

        final Button rescanButton = view.findViewById(R.id.wifi_ssid_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.grantWifiScanDialogPermissions(prefContext))
                    refreshListView(true, "");
            }
        });

        final TextView locationEnabledStatusTextView = view.findViewById(R.id.wifi_ssid_pref_dlg_locationEnableStatus);
        String statusText;
        if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
            statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";
        } else {
            statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
        }
        locationEnabledStatusTextView.setText(statusText);

        AppCompatImageButton locationSystemSettingsButton = view.findViewById(R.id.wifi_ssid_pref_dlg_locationSystemSettingsButton);
        locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
                else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();
                    /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            if (positive != null) positive.setAllCaps(false);
                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            if (negative != null) negative.setAllCaps(false);
                        }
                    });*/
                    if (!((Activity)prefContext).isFinishing())
                        dialog.show();
                }
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }

        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        WifiSSIDPreferenceX.forceRegister = false;
        PPApplication.reregisterReceiversForWifiScanner(prefContext);

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(boolean forRescan, final String scrollToSSID)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            List<WifiSSIDData> _SSIDList = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                _SSIDList = new ArrayList<>();

                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                if (_forRescan) {
                    WifiBluetoothScanner.setForceOneWifiScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    WifiScanJob.startScanner(prefContext, true);
                    PPApplication.logE("WifiSSIDPreferenceFragmentX.refreshListView","start waiting for scan end");

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    PPApplication.sleep(500);
                    WifiBluetoothScanner.waitForWifiScanEnd(prefContext, this);
                    PPApplication.logE("WifiSSIDPreferenceFragmentX.refreshListView","end waiting for scan end");
                }

                List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(prefContext);
                if (wifiConfigurationList != null) {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList) {
                        //if ((wifiConfiguration.bssid != null) && (wifiConfiguration.ssid != null))
                        // bssid is null from configuration list
                        if (wifiConfiguration.ssid != null)
                            _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid, false, true, false));
                    }
                }

                List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(prefContext);
                if (scanResults != null) {
                    for (WifiSSIDData scanResult : scanResults) {
                        if (!WifiScanJob.getSSID(scanResult, wifiConfigurationList).isEmpty()) {
                            boolean exists = false;
                            for (WifiSSIDData ssidData : _SSIDList) {
                                if (!ssidData.ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                    if (WifiScanJob.compareSSID(scanResult, ssidData.ssid, wifiConfigurationList)) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                            if (!exists) {
                                _SSIDList.add(new WifiSSIDData(WifiScanJob.getSSID(scanResult, wifiConfigurationList), scanResult.bssid, false, false, true));
                            }
                        }
                    }
                }

                // add all from value
                boolean found;
                String[] splits = preference.value.split("\\|");
                for (String _ssid : splits) {
                    if (!_ssid.isEmpty() &&
                            !_ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE) &&
                            !_ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                        found = false;
                        for (WifiSSIDData ssid : _SSIDList) {
                            if (_ssid.equals(ssid.ssid)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            _SSIDList.add(new WifiSSIDData(_ssid, "", true, false, false));
                        }

                        found = false;
                        for (WifiSSIDData ssid : preference.customSSIDList) {
                            if (_ssid.equals(ssid.ssid)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            preference.customSSIDList.add(new WifiSSIDData(_ssid, "", true, false, false));
                        }
                    }
                }

                // add custom SSIDs
                for (WifiSSIDData customSSID : preference.customSSIDList) {
                    if (customSSID.ssid != null) {
                        boolean exists = false;
                        for (WifiSSIDData ssidData : _SSIDList) {
                            if (customSSID.ssid.equals(ssidData.ssid)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            _SSIDList.add(new WifiSSIDData(customSSID.ssid, customSSID.bssid, true, false, false));
                        }
                    }
                }

                Collections.sort(_SSIDList, new WifiSSIDPreferenceFragmentX.SortList());

                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE, "", false, false, false));
                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.ALL_SSIDS_VALUE, "", false, false, false));

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                preference.SSIDList = new ArrayList<>(_SSIDList);
                listAdapter.notifyDataSetChanged();

                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                if (!scrollToSSID.isEmpty()) {
                    for (int position = 0; position < preference.SSIDList.size() - 1; position++) {
                        if (preference.SSIDList.get(position).ssid.equals(scrollToSSID)) {
                            SSIDListView.setSelection(position);
                            break;
                        }
                    }
                }

            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.wifi_ssid_pref_dlg_item_edit, popup.getMenu());

        final int ssidPos = (int)view.getTag();
        final String ssid = preference.SSIDList.get(ssidPos).ssid;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.wifi_ssid_pref_dlg_item_menu_change:
                        if (!SSIDName.getText().toString().isEmpty()) {
                            String[] splits = preference.value.split("\\|");
                            preference.value = "";
                            boolean found = false;
                            for (String _ssid : splits) {
                                if (!_ssid.isEmpty()) {
                                    if (!_ssid.equals(ssid)) {
                                        if (!preference.value.isEmpty())
                                            //noinspection StringConcatenationInLoop
                                            preference.value = preference.value + "|";
                                        //noinspection StringConcatenationInLoop
                                        preference.value = preference.value + _ssid;
                                    } else
                                        found = true;
                                }
                            }
                            if (found) {
                                if (!preference.value.isEmpty())
                                    preference.value = preference.value + "|";
                                preference.value = preference.value + SSIDName.getText().toString();
                            }
                            for (WifiSSIDData customSSID : preference.customSSIDList) {
                                if (customSSID.ssid.equals(ssid)) {
                                    customSSID.ssid = SSIDName.getText().toString();
                                    break;
                                }
                            }
                            refreshListView(false, "");
                        }
                        return true;
                    case R.id.wifi_ssid_pref_dlg_item_menu_delete:
                        preference.removeSSID(ssid);
                        for (WifiSSIDData customSSID : preference.customSSIDList)
                        {
                            if (customSSID.ssid.equals(ssid)) {
                                preference.customSSIDList.remove(customSSID);
                                break;
                            }
                        }
                        refreshListView(false, "");
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

}
