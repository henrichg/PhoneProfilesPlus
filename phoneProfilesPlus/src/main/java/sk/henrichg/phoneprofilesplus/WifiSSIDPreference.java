package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiSSIDPreference extends DialogPreference {

    private String value;
    List<WifiSSIDData> SSIDList = null;
    private List<WifiSSIDData> customSSIDList = null;

    Context context;

    private MaterialDialog mDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private EditText SSIDName;
    private ImageView addIcon;
    private WifiSSIDPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public WifiSSIDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        SSIDList = new ArrayList<>();
        customSSIDList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {
        value = getPersistedString(value);

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.wifi_ssid_pref_dlg_rescan_button)
                .autoDismiss(false)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (Permissions.grantWifiScanDialogPermissions(context, WifiSSIDPreference.this))
                            refreshListView(true);
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_wifi_ssid_pref_dialog, null);
        onBindDialogView(layout);

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        addIcon = (ImageView)layout.findViewById(R.id.wifi_ssid_pref_dlg_addIcon);
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
                    customSSIDList.add(new WifiSSIDData(ssid, "", true));
                refreshListView(false);
            }
        });

        SSIDName = (EditText) layout.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
        SSIDName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                addIcon.setEnabled(!SSIDName.getText().toString().isEmpty());
            }
        });

        addIcon.setEnabled(!SSIDName.getText().toString().isEmpty());

        ListView SSIDListView = (ListView) layout.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(context, this);
        SSIDListView.setAdapter(listAdapter);

        refreshListView(false);

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

        mBuilder.customView(layout, false);

        final TextView helpText = (TextView)layout.findViewById(R.id.wifi_ssid_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if ((rescanAsyncTask != null) && (!rescanAsyncTask.isCancelled()))
            rescanAsyncTask.cancel(true);
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

    public String getSSIDs()
    {
        return value;
    }

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

    public void refreshListView(boolean forRescan)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                SSIDList.clear();

                if (_forRescan)
                {
                    GlobalData.setForceOneWifiScan(context, GlobalData.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    WifiScanAlarmBroadcastReceiver.startScanner(context);

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    GlobalData.sleep(200);
                    ScannerService.waitForWifiScanEnd(context, this);
                }

                SSIDList.add(new WifiSSIDData(EventPreferencesWifi.ALL_SSIDS_VALUE, "", false));
                SSIDList.add(new WifiSSIDData(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE, "", false));

                List<WifiSSIDData> wifiConfigurationList = WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(context);
                if (wifiConfigurationList != null)
                {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                    {
                        if (wifiConfiguration.ssid != null)
                            SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid, false));
                    }
                }

                List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(context);
                if (scanResults != null)
                {
                    for (WifiSSIDData scanResult : scanResults)
                    {
                        //Log.d("WifiSSIDPreference.refreshListView","scanResult.ssid="+scanResult.ssid);
                        if (!WifiScanAlarmBroadcastReceiver.getSSID(scanResult, wifiConfigurationList).isEmpty())
                        {
                            //Log.d("WifiSSIDPreference.refreshListView","not empty");
                            boolean exists = false;
                            for (WifiSSIDData ssidData : SSIDList)
                            {
                                if (!ssidData.ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                    if (WifiScanAlarmBroadcastReceiver.compareSSID(scanResult, ssidData.ssid, wifiConfigurationList)) {
                                        //Log.d("WifiSSIDPreference.refreshListView", "exists");
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                            if (!exists) {
                                //Log.d("WifiSSIDPreference.refreshListView","not exists");
                                SSIDList.add(new WifiSSIDData(WifiScanAlarmBroadcastReceiver.getSSID(scanResult, wifiConfigurationList), scanResult.bssid, false));
                            }
                        }
                    }
                }

                // add custom SSIDs
                for (WifiSSIDData customSSID : customSSIDList)
                {
                    if (customSSID.ssid != null) {
                        boolean exists = false;
                        for (WifiSSIDData ssidData : SSIDList)
                        {
                            if (customSSID.ssid.equals(ssidData.ssid)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists)
                            SSIDList.add(new WifiSSIDData(customSSID.ssid, customSSID.bssid, true));
                    }
                }

                // add all from value
                boolean found;
                String[] splits = value.split("\\|");
                for (String _ssid : splits) {
                    if (!_ssid.isEmpty()) {
                        found = false;
                        for (WifiSSIDData ssid : SSIDList) {
                            if (_ssid.equals(ssid.ssid)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            SSIDList.add(new WifiSSIDData(_ssid, "", true));
                            customSSIDList.add(new WifiSSIDData(_ssid, "", true));
                        }
                    }
                }

                Collections.sort(SSIDList, new SortList());

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listAdapter.notifyDataSetChanged();

                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                /*
                for (int position = 0; position < SSIDList.size() - 1; position++) {
                    if (SSIDList.get(position).ssid.equals(value)) {
                        SSIDListView.setSelection(position);
                        SSIDListView.setItemChecked(position, true);
                        SSIDListView.smoothScrollToPosition(position);
                        break;
                    }
                }
                */
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            return GUIData.collator.compare(lhs.ssid, rhs.ssid);
        }

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.wifi_ssid_pref_dlg_item_edit, popup.getMenu());

        int ssidPos = (int)view.getTag();
        final String ssid = SSIDList.get(ssidPos).ssid;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                     case R.id.wifi_ssif_pref_dlg_item_menu_change:
                         if (!SSIDName.getText().toString().isEmpty()) {
                            String[] splits = value.split("\\|");
                            value = "";
                            boolean found = false;
                            for (String _ssid : splits) {
                                if (!_ssid.isEmpty()) {
                                    if (!_ssid.equals(ssid)) {
                                        if (!value.isEmpty())
                                            value = value + "|";
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
                            refreshListView(false);
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
                        refreshListView(false);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

}