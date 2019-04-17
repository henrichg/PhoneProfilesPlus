package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

public class WifiSSIDPreference extends DialogPreference {

    private String value;
    List<WifiSSIDData> SSIDList;
    private final List<WifiSSIDData> customSSIDList;

    private final Context context;

    private AlertDialog mDialog;
    private AlertDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView SSIDListView;
    private EditText SSIDName;
    private AppCompatImageButton addIcon;
    private WifiSSIDPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    static boolean forceRegister = false;

    //private static final String PREF_SHOW_HELP = "wifi_ssid_pref_show_help";

    public WifiSSIDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        SSIDList = new ArrayList<>();
        customSSIDList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {
        value = getPersistedString(value);

        PPApplication.forceRegisterReceiversForWifiScanner(context);
        forceRegister = true;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                            /*
                            SSIDName.clearFocus();

                            String editText = SSIDName.getText().toString();
                            if (editText.equals(context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb)))
                                value = EventPreferencesWifi.CONFIGURED_SSIDS_VALUE;
                            else
                                value = editText;
                            */

                    if (callChangeListener(value))
                    {
                        persistString(value);
                    }
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_wifi_ssid_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                refreshListView(false, "");
            }
        });

        progressLinearLayout = layout.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = layout.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        addIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = SSIDName.getText().toString();
                addSSID(ssid);
                boolean found = false;
                for (WifiSSIDData customSSIDData : customSSIDList) {
                    if (customSSIDData.ssid.equals(ssid)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    customSSIDList.add(new WifiSSIDData(ssid, "", true, false, false));
                refreshListView(false, ssid);
            }
        });

        SSIDName = layout.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
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
                        addIcon, R.drawable.ic_button_add, context.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!SSIDName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, context.getApplicationContext());

        SSIDListView = layout.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(context, this);
        SSIDListView.setAdapter(listAdapter);

        //refreshListView(false, "");

        SSIDListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //WifiSSIDPreferenceAdapter.ViewHolder viewHolder =
                //        (WifiSSIDPreferenceAdapter.ViewHolder) v.getTag();
                String ssid = SSIDList.get(position).ssid;
                if (!(ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE) ||
                        ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE))) {
                    SSIDName.setText(ssid);
                }
                /*
                viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                if (viewHolder.checkBox.isChecked()) {
                    addSSID(SSIDList.get(position).ssid);
                }
                else {
                    removeSSID(SSIDList.get(position).ssid);
                }
                */
            }

        });

        final ImageView helpIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString = context.getString(R.string.event_preference_wifi_ssidName_type)+"\n\n"+
                        context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                        context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                        context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                        context.getString(R.string.pref_dlg_info_about_wildcards_3);

                DialogHelpPopupWindow.showPopup(helpIcon, (Activity)context, mDialog, helpString);
            }
        });

        ImageView changeSelectionIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_dlg_change_selection_title)
                        .setCancelable(true)
                        .setNegativeButton(getNegativeButtonText(), null)
                        //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                        .setItems(R.array.wifiSSIDChangeSelectionArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        value = "";
                                        break;
                                    case 1:
                                        for (WifiSSIDData ssid : SSIDList) {
                                            if (ssid.ssid.equals(SSIDName.getText().toString()))
                                                addSSID(ssid.ssid);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView(false, "");
                                //dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        final Button rescanButton = layout.findViewById(R.id.wifi_ssid_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.grantWifiScanDialogPermissions(context))
                    refreshListView(true, "");
            }
        });

        final TextView locationEnabledStatusTextView = layout.findViewById(R.id.wifi_ssid_pref_dlg_locationEnableStatus);
        String statusText;
        if (!PhoneProfilesService.isLocationEnabled(context)) {
            statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";
        } else {
            statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
        }
        locationEnabledStatusTextView.setText(statusText);

        AppCompatImageButton locationSystemSettingsButton = layout.findViewById(R.id.wifi_ssid_pref_dlg_locationSystemSettingsButton);
        locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    context.startActivity(intent);
                }
                else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
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
                    if (!((Activity)context).isFinishing())
                        dialog.show();
                }
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
        forceRegister = false;
        PPApplication.reregisterReceiversForWifiScanner(context);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }
    }

    /*
    public String getSSIDs()
    {
        return value;
    }
    */

    void addSSID(String ssid) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String _ssid : splits) {
            if (_ssid.equals(ssid))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + ssid;
        }
        //Log.d("WifiSSIDPreference.addSSID","value="+value);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeSSID(String ssid) {
        String[] splits = value.split("\\|");
        value = "";
        for (String _ssid : splits) {
            if (!_ssid.isEmpty()) {
                if (!_ssid.equals(ssid)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _ssid;
                }
            }
        }
        //Log.d("WifiSSIDPreference.removeSSID","value="+value);
    }

    boolean isSSIDSelected(String ssid) {
        String[] splits = value.split("\\|");
        for (String _ssid : splits) {
            if (_ssid.equals(ssid))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(boolean forRescan, final String scrollToSSID)
    {
        if ((mDialog != null) && mDialog.isShowing()) {
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
                        WifiBluetoothScanner.setForceOneWifiScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                        WifiScanJob.startScanner(context, true);
                        PPApplication.logE("WifiSSIDPreference.refreshListView","start waiting for scan end");

                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        PPApplication.sleep(500);
                        WifiBluetoothScanner.waitForWifiScanEnd(context, this);
                        PPApplication.logE("WifiSSIDPreference.refreshListView","end waiting for scan end");
                    }

                    List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(context);
                    if (wifiConfigurationList != null) {
                        for (WifiSSIDData wifiConfiguration : wifiConfigurationList) {
                            //if ((wifiConfiguration.bssid != null) && (wifiConfiguration.ssid != null))
                            // bssid is null from configuration list
                            if (wifiConfiguration.ssid != null)
                                _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid, false, true, false));
                        }
                    }

                    List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(context);
                    if (scanResults != null) {
                        for (WifiSSIDData scanResult : scanResults) {
                            //Log.d("WifiSSIDPreference.refreshListView","scanResult.ssid="+scanResult.ssid);
                            if (!WifiScanJob.getSSID(scanResult, wifiConfigurationList).isEmpty()) {
                                //Log.d("WifiSSIDPreference.refreshListView","not empty");
                                boolean exists = false;
                                for (WifiSSIDData ssidData : _SSIDList) {
                                    if (!ssidData.ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                        if (WifiScanJob.compareSSID(scanResult, ssidData.ssid, wifiConfigurationList)) {
                                            //Log.d("WifiSSIDPreference.refreshListView", "exists");
                                            exists = true;
                                            break;
                                        }
                                    }
                                }
                                if (!exists) {
                                    //Log.d("WifiSSIDPreference.refreshListView","not exists");
                                    _SSIDList.add(new WifiSSIDData(WifiScanJob.getSSID(scanResult, wifiConfigurationList), scanResult.bssid, false, false, true));
                                }
                            }
                        }
                    }

                    // add all from value
                    boolean found;
                    String[] splits = value.split("\\|");
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
                            for (WifiSSIDData ssid : customSSIDList) {
                                if (_ssid.equals(ssid.ssid)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                customSSIDList.add(new WifiSSIDData(_ssid, "", true, false, false));
                            }
                        }
                    }

                    // add custom SSIDs
                    for (WifiSSIDData customSSID : customSSIDList) {
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

                    Collections.sort(_SSIDList, new SortList());

                    _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE, "", false, false, false));
                    _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.ALL_SSIDS_VALUE, "", false, false, false));

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    SSIDList = new ArrayList<>(_SSIDList);
                    listAdapter.notifyDataSetChanged();

                    if (_forRescan) {
                        progressLinearLayout.setVisibility(View.GONE);
                        dataRelativeLayout.setVisibility(View.VISIBLE);
                    }

                    if (!scrollToSSID.isEmpty()) {
                        for (int position = 0; position < SSIDList.size() - 1; position++) {
                            if (SSIDList.get(position).ssid.equals(scrollToSSID)) {
                                SSIDListView.setSelection(position);
                                break;
                            }
                        }
                    }

                }

            };

            rescanAsyncTask.execute();
        }
    }

    private class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }

    public void showEditMenu(View view)
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
        final String ssid = SSIDList.get(ssidPos).ssid;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                     case R.id.wifi_ssid_pref_dlg_item_menu_change:
                         if (!SSIDName.getText().toString().isEmpty()) {
                             String[] splits = value.split("\\|");
                             value = "";
                             boolean found = false;
                             for (String _ssid : splits) {
                                 if (!_ssid.isEmpty()) {
                                     if (!_ssid.equals(ssid)) {
                                         if (!value.isEmpty())
                                             //noinspection StringConcatenationInLoop
                                             value = value + "|";
                                         //noinspection StringConcatenationInLoop
                                         value = value + _ssid;
                                     } else
                                         found = true;
                                 }
                             }
                             if (found) {
                                 if (!value.isEmpty())
                                     value = value + "|";
                                 value = value + SSIDName.getText().toString();
                             }
                             for (WifiSSIDData customSSID : customSSIDList) {
                                 if (customSSID.ssid.equals(ssid)) {
                                     customSSID.ssid = SSIDName.getText().toString();
                                     break;
                                 }
                             }
                             refreshListView(false, "");
                         }
                         return true;
                     case R.id.wifi_ssid_pref_dlg_item_menu_delete:
                         removeSSID(ssid);
                         for (WifiSSIDData customSSID : customSSIDList)
                         {
                             if (customSSID.ssid.equals(ssid)) {
                                 customSSIDList.remove(customSSID);
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