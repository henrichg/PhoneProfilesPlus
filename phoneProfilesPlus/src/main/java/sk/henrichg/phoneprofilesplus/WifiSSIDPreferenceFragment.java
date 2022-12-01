package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WifiSSIDPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private WifiSSIDPreference preference;

    private SingleSelectListDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView SSIDListView;
    private EditText SSIDName;
    private AppCompatImageButton addIcon;
    private WifiSSIDPreferenceAdapter listAdapter;
    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private RefreshListViewAsyncTask rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (WifiSSIDPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_wifi_ssid_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        WifiSSIDPreference.forceRegister = true;
        PPApplication.forceRegisterReceiversForWifiScanner(prefContext);

        progressLinearLayout = view.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = view.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_addIcon);
        TooltipCompat.setTooltipText(addIcon, getString(R.string.wifi_ssid_pref_dlg_add_button_tooltip));
        addIcon.setOnClickListener(v -> {
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
                preference.customSSIDList.add(new WifiSSIDData(ssid, /*"",*/ true, false, false));
            refreshListView(false, ssid);
        });

        SSIDName = view.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
        SSIDName.setBackgroundTintList(ContextCompat.getColorStateList(prefContext, R.color.highlighted_spinner_all));
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
                        addIcon, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!SSIDName.getText().toString().isEmpty(),
                addIcon, prefContext.getApplicationContext());

        SSIDListView = view.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(prefContext, preference);
        SSIDListView.setAdapter(listAdapter);

        SSIDListView.setOnItemLongClickListener((parent, view12, position, id) -> {
            String ssid = preference.SSIDList.get(position).ssid;
            if (!(ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE) ||
                    ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE))) {
                SSIDName.setText(ssid);
            }
            return true;
        });

        final ImageView helpIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> {
            String helpString = getString(R.string.event_preference_wifi_ssidName_type)+"\n\n"+
                    getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                    getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                    getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                    getString(R.string.pref_dlg_info_about_wildcards_3);

            DialogHelpPopupWindow.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, /*getDialog(),*/ helpString, false);
        });

        ImageView changeSelectionIcon = view.findViewById(R.id.wifi_ssid_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.wifi_ssid_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSelectorDialog = new SingleSelectListDialog(
                            R.string.pref_dlg_change_selection_title,
                            R.array.wifiSSIDChangeSelectionArray,
                            SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                            (dialog, which) -> {
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
                            },
                            false,
                            getActivity());

                    mSelectorDialog.show();
                }
        });

        rescanButton = view.findViewById(R.id.wifi_ssid_pref_dlg_rescanButton);
        rescanButton.setOnClickListener(v -> {
            if (Permissions.grantWifiScanDialogPermissions(prefContext))
                refreshListView(true, "");
        });

        locationSystemSettingsRelLa = view.findViewById(R.id.wifi_ssid_pref_dlg_locationSystemSettingsRelLa);
        locationEnabledStatusTextView = view.findViewById(R.id.wifi_ssid_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.wifi_ssid_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

        setLocationEnableStatus();

        refreshListView(false, "");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        } else {
            preference.customSSIDList.clear();
            preference.resetSummary();
        }

        if ((mSelectorDialog != null) && mSelectorDialog.mDialog.isShowing())
            mSelectorDialog.mDialog.dismiss();

        WifiScanWorker.setScanRequest(prefContext, false);
        WifiScanWorker.setWaitForResults(prefContext, false);
        WifiScanner.setForceOneWifiScan(prefContext, WifiScanner.FORCE_ONE_SCAN_DISABLED);

        if ((rescanAsyncTask != null) && rescanAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            rescanAsyncTask.cancel(true);

        WifiSSIDPreference.forceRegister = false;
        PPApplication.reregisterReceiversForWifiScanner(prefContext);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        //if (Build.VERSION.SDK_INT >= 23) {
            String statusText;
            if (!GlobalUtils.isLocationEnabled(prefContext)) {
                statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                        "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

                locationEnabledStatusTextView.setText(statusText);

                locationSystemSettingsButton.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //noinspection deprecation
                                getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_WIFI_LOCATION_SYSTEM_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    getString(R.string.phone_profiles_pref_eventLocationSystemSettings),
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

                            if (getActivity() != null)
                                if (!getActivity().isFinishing())
                                    dialog.show();
                        }
                    }
                });

                locationSystemSettingsRelLa.setVisibility(View.VISIBLE);
                //locationEnabledStatusTextView.setVisibility(View.VISIBLE);
                //locationSystemSettingsButton.setVisibility(View.VISIBLE);
                rescanButton.setVisibility(View.GONE);
            } else {
                locationSystemSettingsRelLa.setVisibility(View.GONE);
                //locationEnabledStatusTextView.setVisibility(View.GONE);
                //locationSystemSettingsButton.setVisibility(View.GONE);
                rescanButton.setVisibility(View.VISIBLE);
            }
        /*}
        else {
            locationSystemSettingsRelLa.setVisibility(View.GONE);
            //locationEnabledStatusTextView.setVisibility(View.GONE);
            //locationSystemSettingsButton.setVisibility(View.GONE);
            rescanButton.setVisibility(View.VISIBLE);
        }*/
    }

    void refreshListView(boolean forRescan, final String scrollToSSID)
    {
        rescanAsyncTask = new RefreshListViewAsyncTask(forRescan, scrollToSSID, preference, this, prefContext);
        rescanAsyncTask.execute();
    }

    private static class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(_context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(_context).inflate(R.menu.wifi_ssid_pref_dlg_item_edit, popup.getMenu());

        final int ssidPos = (int)view.getTag();
        final String ssid = preference.SSIDList.get(ssidPos).ssid;

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.wifi_ssid_pref_dlg_item_menu_change) {
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
            }
            else
            if (itemId == R.id.wifi_ssid_pref_dlg_item_menu_delete) {
                if (getActivity() != null) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(getString(R.string.profile_context_item_delete));
                    dialogBuilder.setMessage(getString(R.string.delete_wifi_ssid_alert_message));
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                        preference.removeSSID(ssid);
                        for (WifiSSIDData customSSID : preference.customSSIDList) {
                            if (customSSID.ssid.equals(ssid)) {
                                preference.customSSIDList.remove(customSSID);
                                break;
                            }
                        }
                        refreshListView(false, "");
                    });
                    dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                    AlertDialog dialog = dialogBuilder.create();

                    //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    //            @Override
                    //            public void onShow(DialogInterface dialog) {
                    //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    //                if (positive != null) positive.setAllCaps(false);
                    //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    //                if (negative != null) negative.setAllCaps(false);
                    //            }
                    //        });

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            else
            if (itemId == R.id.wifi_ssid_pref_dlg_item_menu_copy_name) {
                if (!(ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE) ||
                        ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE))) {
                    SSIDName.setText(ssid);
                }
                return true;
            }
            else {
                return false;
            }
        });


        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean forRescan;
        final String scrollToSSID;
        private final WeakReference<WifiSSIDPreference> preferenceWeakRef;
        private final WeakReference<WifiSSIDPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(boolean forRescan, final String scrollToSSID,
                                        WifiSSIDPreference preference,
                                        WifiSSIDPreferenceFragment fragment,
                                        Context prefContext) {
            this.forRescan = forRescan;
            this.scrollToSSID = scrollToSSID;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        List<WifiSSIDData> _SSIDList = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _SSIDList = new ArrayList<>();

            WifiSSIDPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (forRescan) {
                    fragment.dataRelativeLayout.setVisibility(View.GONE);
                    fragment.progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            WifiSSIDPreferenceFragment fragment = fragmentWeakRef.get();
            WifiSSIDPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (forRescan) {
                    WifiScanner.setForceOneWifiScan(prefContext, WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    WifiScanWorker.startScanner(prefContext, true);

                    //PPApplication.sleep(500);
                    //WifiBluetoothScanner.waitForWifiScanEnd(prefContext, this);
                }

                List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(prefContext);
                //if (wifiConfigurationList != null) {
                for (WifiSSIDData wifiConfiguration : wifiConfigurationList) {
                    //if ((wifiConfiguration.bssid != null) && (wifiConfiguration.ssid != null))
                    // bssid is null from configuration list
                    if (wifiConfiguration.ssid != null)
                        _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), /*wifiConfiguration.bssid,*/ false, true, false));
                }
                //}

                List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(prefContext);
                if (scanResults != null) {
                    for (WifiSSIDData scanResult : scanResults) {
                        if (!WifiScanWorker.getSSID(scanResult, wifiConfigurationList).isEmpty()) {
                            boolean exists = false;
                            for (WifiSSIDData ssidData : _SSIDList) {
                                if (!ssidData.ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                    if (WifiScanWorker.compareSSID(scanResult, ssidData.ssid, wifiConfigurationList)) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                            if (!exists) {
                                _SSIDList.add(new WifiSSIDData(WifiScanWorker.getSSID(scanResult, wifiConfigurationList), /*scanResult.bssid,*/ false, false, true));
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
                            _SSIDList.add(new WifiSSIDData(_ssid, /*"",*/ true, false, false));
                        }

                        found = false;
                        for (WifiSSIDData ssid : preference.customSSIDList) {
                            if (_ssid.equals(ssid.ssid)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            preference.customSSIDList.add(new WifiSSIDData(_ssid, /*"",*/ true, false, false));
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
                            _SSIDList.add(new WifiSSIDData(customSSID.ssid, /*customSSID.bssid,*/ true, false, false));
                        }
                    }
                }

                _SSIDList.sort(new SortList());

                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE, /*"",*/ false, false, false));
                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.ALL_SSIDS_VALUE, /*"",*/ false, false, false));

                // move checked on top
                int i = 0;
                int ich = 0;
                while (i < _SSIDList.size()) {
                    WifiSSIDData ssidData = _SSIDList.get(i);
                    if (preference.isSSIDSelected(ssidData.ssid)) {
                        _SSIDList.remove(i);
                        _SSIDList.add(ich, ssidData);
                        ich++;
                    }
                    i++;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            WifiSSIDPreferenceFragment fragment = fragmentWeakRef.get();
            WifiSSIDPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.SSIDList = new ArrayList<>(_SSIDList);
                fragment.listAdapter.notifyDataSetChanged();

                if (forRescan) {
                    WifiScanWorker.setScanRequest(prefContext, false);
                    WifiScanWorker.setWaitForResults(prefContext, false);
                    WifiScanner.setForceOneWifiScan(prefContext, WifiScanner.FORCE_ONE_SCAN_DISABLED);
                    fragment.progressLinearLayout.setVisibility(View.GONE);
                    fragment.dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                if (!scrollToSSID.isEmpty()) {
                    for (int position = 0; position < preference.SSIDList.size() - 1; position++) {
                        if (preference.SSIDList.get(position).ssid.equals(scrollToSSID)) {
                            fragment.SSIDListView.setSelection(position);
                            break;
                        }
                    }
                }
            }

        }

    }

}
