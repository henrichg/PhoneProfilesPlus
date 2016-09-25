package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class WifiSSIDPreference extends DialogPreference {

    private String value;
    public List<WifiSSIDData> SSIDList = null;

    Context context;

    private MaterialDialog mDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private EditText SSIDName;
    private ListView SSIDListView;
    private CheckBox configuredSSIDs;
    private WifiSSIDPreferenceAdapter listAdapter;

    public RadioButton selectedRB = null;
    public int selectedPosition = -1;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public WifiSSIDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        SSIDList = new ArrayList<WifiSSIDData>();
    }

    @Override
    protected void showDialog(Bundle state) {
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
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (shouldPersist()) {
                            if (configuredSSIDs.isChecked()) {
                                value = EventPreferencesWifi.CONFIGURED_SSIDS_VALUE;
                            }
                            else {
                                SSIDName.clearFocus();
                                value = SSIDName.getText().toString();
                            }

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
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (Permissions.grantWifiScanDialogPermissions(context, WifiSSIDPreference.this))
                            refreshListView(true);
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_wifi_ssid_pref_dialog, null);
        onBindDialogView(layout);

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        SSIDName = (EditText) layout.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
        setSSID(value);

        SSIDListView = (ListView) layout.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(context, this);
        SSIDListView.setAdapter(listAdapter);

        configuredSSIDs = (CheckBox) layout.findViewById(R.id.wifi_ssid_pref_dlg_configured_ssids);
        if (value.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE)) {
            SSIDListView.setEnabled(false);
            SSIDName.setEnabled(false);
            configuredSSIDs.setChecked(true);
        }

        refreshListView(false);

        SSIDListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                WifiSSIDPreferenceAdapter.ViewHolder viewHolder =
                        (WifiSSIDPreferenceAdapter.ViewHolder) v.getTag();

                if(position != selectedPosition && selectedRB != null){
                    selectedRB.setChecked(false);
                }
                selectedPosition = position;
                selectedRB = viewHolder.radioBtn;

                viewHolder.radioBtn.setChecked(true);
                setSSID(SSIDList.get(position).ssid);
            }

        });

        configuredSSIDs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (configuredSSIDs.isChecked()) {
                    setSSID(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE);
                    SSIDListView.setEnabled(false);
                    SSIDName.setEnabled(false);
                    listAdapter.notifyDataSetChanged();
                }
                else {
                    SSIDListView.setEnabled(true);
                    SSIDName.setEnabled(true);
                    listAdapter.notifyDataSetChanged();
                    setSSID(value);
                }
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

    public String getSSID()
    {
        return value;
    }
    
    public void setSSID(String SSID)
    {
        if (SSID.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE))
            this.SSIDName.setText(context.getString(R.string.empty_string));
        else {
            value = SSID;
            this.SSIDName.setText(value);
        }
    }

    public boolean isConfiguredSSIDsChecked() {
        return configuredSSIDs.isChecked();
    }

    public void refreshListView(boolean forRescan)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                dataRelativeLayout.setVisibility(View.GONE);
                progressLinearLayout.setVisibility(View.VISIBLE);
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

                List<WifiSSIDData> wifiConfigurationList = WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(context);
                if (wifiConfigurationList != null)
                {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                    {
                        if (wifiConfiguration.ssid != null)
                            SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid));
                    }
                }

                List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(context);
                if (scanResults != null)
                {
                    for (WifiSSIDData scanResult : scanResults)
                    {
                        if (!WifiScanAlarmBroadcastReceiver.getSSID(scanResult, wifiConfigurationList).isEmpty())
                        {
                            boolean exists = false;
                            for (WifiSSIDData ssidData : SSIDList)
                            {
                                if (WifiScanAlarmBroadcastReceiver.compareSSID(scanResult, ssidData.ssid, wifiConfigurationList))
                                {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists)
                                SSIDList.add(new WifiSSIDData(WifiScanAlarmBroadcastReceiver.getSSID(scanResult, wifiConfigurationList), scanResult.bssid));
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listAdapter.notifyDataSetChanged();
                progressLinearLayout.setVisibility(View.GONE);
                dataRelativeLayout.setVisibility(View.VISIBLE);

                if (configuredSSIDs.isChecked()) {
                    SSIDListView.setEnabled(false);
                    SSIDName.setEnabled(false);
                }
                else {
                    SSIDListView.setEnabled(true);
                    SSIDName.setEnabled(true);
                    for (int position = 0; position < SSIDList.size() - 1; position++) {
                        if (SSIDList.get(position).ssid.equals(value)) {
                            SSIDListView.setSelection(position);
                            SSIDListView.setItemChecked(position, true);
                            SSIDListView.smoothScrollToPosition(position);
                            break;
                        }
                    }
                }
            }

        };

        rescanAsyncTask.execute();
    }
    
}