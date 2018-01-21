package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
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

    private final Context context;

    private MaterialDialog mDialog;
    private MaterialDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView SSIDListView;
    private EditText SSIDName;
    private AppCompatImageButton addIcon;
    private WifiSSIDPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    static boolean forceRegister = false;

    private static final String PREF_SHOW_HELP = "wifi_ssid_pref_show_help";

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

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.wifi_ssid_pref_dlg_rescan_button)
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_wifi_ssid_pref_dialog, false)
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
                            refreshListView(true, "");
                    }
                });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        progressLinearLayout = layout.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        dataRelativeLayout = layout.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);

        //noinspection ConstantConditions
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
                    customSSIDList.add(new WifiSSIDData(ssid, "", true));
                refreshListView(false, ssid);
            }
        });

        //noinspection ConstantConditions
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
                        addIcon, R.drawable.ic_action_location_add, context.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!SSIDName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_action_location_add, context.getApplicationContext());

        //noinspection ConstantConditions
        SSIDListView = layout.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(context, this);
        SSIDListView.setAdapter(listAdapter);

        refreshListView(false, "");

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

        final TextView helpText = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        final ImageView helpIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        ApplicationPreferences.getSharedPreferences(context);
        if (ApplicationPreferences.preferences.getBoolean(PREF_SHOW_HELP, true)) {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
            helpText.setVisibility(View.VISIBLE);
        }
        else {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help);
                    visibility = View.GONE;
                    editor.putBoolean(PREF_SHOW_HELP, false);
                }
                else {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
                    visibility = View.VISIBLE;
                    editor.putBoolean(PREF_SHOW_HELP, true);
                }
                helpText.setVisibility(visibility);
                editor.apply();
            }
        });
        ImageView changeSelectionIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new MaterialDialog.Builder(context)
                        .title(R.string.pref_dlg_change_selection_title)
                        .items(R.array.wifiSSIDChangeSelectionArray)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
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
                                return true;
                            }
                        })
                        .positiveText(R.string.pref_dlg_change_selection_button)
                        .negativeText(getNegativeButtonText())
                        .show();
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
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
        if (mSelectorDialog != null && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if (mDialog != null && mDialog.isShowing())
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
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            List<WifiSSIDData> _SSIDList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                _SSIDList = new ArrayList<>();

                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                if (_forRescan)
                {
                    WifiBluetoothScanner.setForceOneWifiScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    WifiScanJob.startScanner(context, true);

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    //PPApplication.sleep(200);
                    WifiBluetoothScanner.waitForWifiScanEnd(context, this);
                }

                List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(context);
                if (wifiConfigurationList != null)
                {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                    {
                        //if ((wifiConfiguration.bssid != null) && (wifiConfiguration.ssid != null))
                        // bssid is null from configuration list
                        if (wifiConfiguration.ssid != null)
                            _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid, false));
                    }
                }

                List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(context);
                if (scanResults != null)
                {
                    for (WifiSSIDData scanResult : scanResults)
                    {
                        //Log.d("WifiSSIDPreference.refreshListView","scanResult.ssid="+scanResult.ssid);
                        if (!WifiScanJob.getSSID(scanResult, wifiConfigurationList).isEmpty())
                        {
                            //Log.d("WifiSSIDPreference.refreshListView","not empty");
                            boolean exists = false;
                            for (WifiSSIDData ssidData : _SSIDList)
                            {
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
                                _SSIDList.add(new WifiSSIDData(WifiScanJob.getSSID(scanResult, wifiConfigurationList), scanResult.bssid, false));
                            }
                        }
                    }
                }

                // add custom SSIDs
                for (WifiSSIDData customSSID : customSSIDList)
                {
                    if (customSSID.ssid != null) {
                        boolean exists = false;
                        for (WifiSSIDData ssidData : _SSIDList)
                        {
                            if (customSSID.ssid.equals(ssidData.ssid)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists)
                            _SSIDList.add(new WifiSSIDData(customSSID.ssid, customSSID.bssid, true));
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
                            _SSIDList.add(new WifiSSIDData(_ssid, "", true));
                            customSSIDList.add(new WifiSSIDData(_ssid, "", true));
                        }
                    }
                }

                Collections.sort(_SSIDList, new SortList());

                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE, "", false));
                _SSIDList.add(0, new WifiSSIDData(EventPreferencesWifi.ALL_SSIDS_VALUE, "", false));

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
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
        if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        else
            popup = new PopupMenu(context, view);
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