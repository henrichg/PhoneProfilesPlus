package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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

@SuppressWarnings("WeakerAccess")
public class BluetoothNamePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private BluetoothNamePreferenceX preference;

    private AlertDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView bluetoothListView;
    private EditText bluetoothName;
    private AppCompatImageButton addIcon;
    private BluetoothNamePreferenceAdapterX listAdapter;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (BluetoothNamePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_bluetooth_name_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        PPApplication.forceRegisterReceiversForBluetoothScanner(prefContext);
        BluetoothNamePreferenceX.forceRegister = true;

        progressLinearLayout = view.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        dataRelativeLayout = view.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btName = bluetoothName.getText().toString();
                preference.addBluetoothName(btName);
                boolean found = false;
                for (BluetoothDeviceData customBtNameData : preference.customBluetoothList) {
                    if (customBtNameData.getName().equalsIgnoreCase(btName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //if (android.os.Build.VERSION.SDK_INT >= 18)
                    preference.customBluetoothList.add(new BluetoothDeviceData(btName, "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                    //else
                    //    customBluetoothList.add(new BluetoothDeviceData(btName, "", 0, true, 0));
                }
                refreshListView(false, btName);
            }
        });

        bluetoothName = view.findViewById(R.id.bluetooth_name_pref_dlg_bt_name);
        bluetoothName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GlobalGUIRoutines.setImageButtonEnabled(!bluetoothName.getText().toString().isEmpty(),
                        addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!bluetoothName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());

        bluetoothListView = view.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapterX(prefContext, preference);
        bluetoothListView.setAdapter(listAdapter);

        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String btName = preference.bluetoothList.get(position).getName();
                if (!(btName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE) ||
                        btName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE))) {
                    bluetoothName.setText(btName);
                }
            }

        });

        final ImageView helpIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString = getString(R.string.event_preference_bluetooth_btName_type)+"\n\n"+
                        //getString(R.string.event_preference_bluetooth_bt_types)+"\n\n"+
                        getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                        getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                        getString(R.string.bluetooth_name_pref_dlg_info_about_wildcards) + " " +
                        getString(R.string.pref_dlg_info_about_wildcards_3);

                DialogHelpPopupWindowX.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, getDialog(), helpString);
            }
        });


        ImageView changeSelectionIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity)prefContext).isFinishing()) {
                    mSelectorDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.pref_dlg_change_selection_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.bluetoothNameDChangeSelectionArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            preference.value = "";
                                            break;
                                        case 1:
                                            for (BluetoothDeviceData bluetooth : preference.bluetoothList) {
                                                if (bluetooth.name.equals(bluetoothName.getText().toString()))
                                                    preference.addBluetoothName(bluetooth.name);
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
            }
        });

        rescanButton = view.findViewById(R.id.bluetooth_name_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.grantBluetoothScanDialogPermissions(prefContext))
                    refreshListView(true, "");
            }
        });

        locationEnabledStatusTextView = view.findViewById(R.id.bluetooth_name_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.bluetooth_name_pref_dlg_locationSystemSettingsButton);

        setLocationEnableStatus();

        refreshListView(false, "");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.customBluetoothList.clear();
            preference.resetSummary();
        }

        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();

        BluetoothScanWorker.setScanRequest(prefContext, false);
        BluetoothScanWorker.setWaitForResults(prefContext, false);
        BluetoothScanWorker.setLEScanRequest(prefContext, false);
        BluetoothScanWorker.setWaitForLEResults(prefContext, false);
        BluetoothScanWorker.setScanKilled(prefContext, true);
        WifiBluetoothScanner.setForceOneBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
        WifiBluetoothScanner.setForceOneLEBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED))) {
            PPApplication.logE("BluetoothNamePreferenceFragmentX.onDialogClosed","cancel asyncTask");
            rescanAsyncTask.cancel(true);
        }

        BluetoothNamePreferenceX.forceRegister = false;
        PPApplication.reregisterReceiversForBluetoothScanner(prefContext);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        if (Build.VERSION.SDK_INT >= 23) {
            String statusText;
            if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
                statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                        "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

                locationEnabledStatusTextView.setText(statusText);

                locationSystemSettingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ok = false;
                        if (getActivity() != null) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                                    ok = true;
                                } catch (Exception ignored) {}
                                if (!ok) {
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
                                    if (!((Activity) prefContext).isFinishing())
                                        dialog.show();
                                }
                            }
                        }
                    }
                });

                locationEnabledStatusTextView.setVisibility(View.VISIBLE);
                locationSystemSettingsButton.setVisibility(View.VISIBLE);
                rescanButton.setVisibility(View.GONE);
            } else {
                locationEnabledStatusTextView.setVisibility(View.GONE);
                locationSystemSettingsButton.setVisibility(View.GONE);
                rescanButton.setVisibility(View.VISIBLE);
            }
        }
        else {
            locationEnabledStatusTextView.setVisibility(View.GONE);
            locationSystemSettingsButton.setVisibility(View.GONE);
            rescanButton.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(boolean forRescan, final String scrollToBTName)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            List<BluetoothDeviceData> _bluetoothList = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                _bluetoothList = new ArrayList<>();

                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                if (_forRescan) {
                    PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView","start rescan");
                    WifiBluetoothScanner.setForceOneBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    WifiBluetoothScanner.setForceOneLEBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    BluetoothScanWorker.startScanner(prefContext, true);

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    //PPApplication.sleep(500);
                    //WifiBluetoothScanner.waitForForceOneBluetoothScanEnd(prefContext, this);
                    PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView","end rescan");
                }

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(prefContext);
                if (boundedDevicesList != null) {
                    for (BluetoothDeviceData device : boundedDevicesList) {
                        _bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, true, false));
                    }
                }

                List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(prefContext);
                PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "scanResults="+scanResults);
                if (scanResults != null) {
                    for (BluetoothDeviceData device : scanResults) {
                        if (!device.getName().isEmpty()) {
                            boolean exists = false;
                            for (BluetoothDeviceData _device : _bluetoothList) {
                                if (_device.getName().equalsIgnoreCase(device.getName())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                _bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                            }
                        }
                    }
                }

                // add all from value
                boolean found;
                String[] splits = preference.value.split("\\|");
                for (String _bluetoothName : splits) {
                    if (!_bluetoothName.isEmpty() &&
                            !_bluetoothName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE) &&
                            !_bluetoothName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE)) {
                        found = false;
                        for (BluetoothDeviceData bluetoothName : _bluetoothList) {
                            if (_bluetoothName.equals(bluetoothName.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            //if (android.os.Build.VERSION.SDK_INT >= 18) {
                            _bluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                        /*}
                        else {
                            _bluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", 0, true, 0));
                        }*/
                        }

                        found = false;
                        for (BluetoothDeviceData bluetoothName : preference.customBluetoothList) {
                            if (_bluetoothName.equals(bluetoothName.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            //if (android.os.Build.VERSION.SDK_INT >= 18) {
                            preference.customBluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                        /*}
                        else {
                            customBluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", 0, true, 0));
                        }*/
                        }
                    }
                }

                // add custom Bluetooth names
                for (BluetoothDeviceData customBTName : preference.customBluetoothList) {
                    if (customBTName.getName() != null) {
                        boolean exists = false;
                        for (BluetoothDeviceData btNameData : _bluetoothList) {
                            if (customBTName.getName().equalsIgnoreCase(btNameData.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            //if (android.os.Build.VERSION.SDK_INT >= 18)
                            _bluetoothList.add(new BluetoothDeviceData(customBTName.getName(), "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                            //else
                            //    _bluetoothList.add(new BluetoothDeviceData(customBTName.getName(), "", 0, true, 0));
                        }
                    }
                }

                Collections.sort(_bluetoothList, new BluetoothNamePreferenceFragmentX.SortList());

                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE, "", BluetoothDevice.DEVICE_TYPE_DUAL, false, 0, false, false));
                _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE, "", BluetoothDevice.DEVICE_TYPE_DUAL, false, 0, false, false));
                /*}
                else {
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE, "", 0, false, 0));
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE, "", 0, false, 0));
                }*/

                // move checked on top
                int i = 0;
                int ich = 0;
                while (i < _bluetoothList.size()) {
                    BluetoothDeviceData bluetoothData = _bluetoothList.get(i);
                    if (preference.isBluetoothNameSelected(bluetoothData.getName())) {
                        _bluetoothList.remove(i);
                        _bluetoothList.add(ich, bluetoothData);
                        ich++;
                    }
                    i++;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                preference.bluetoothList = new ArrayList<>(_bluetoothList);
                listAdapter.notifyDataSetChanged();

                if (_forRescan) {
                    BluetoothScanWorker.setScanRequest(prefContext, false);
                    BluetoothScanWorker.setWaitForResults(prefContext, false);
                    BluetoothScanWorker.setLEScanRequest(prefContext, false);
                    BluetoothScanWorker.setWaitForLEResults(prefContext, false);
                    WifiBluetoothScanner.setForceOneBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                    WifiBluetoothScanner.setForceOneLEBluetoothScan(prefContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanWorker.setScanKilled(prefContext, false);
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                if (!scrollToBTName.isEmpty())
                    for (int position = 0; position < preference.bluetoothList.size() - 1; position++) {
                        if (preference.bluetoothList.get(position).getName().equalsIgnoreCase(scrollToBTName)) {
                            bluetoothListView.setSelection(position);
                            break;
                        }
                    }
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<BluetoothDeviceData> {

        public int compare(BluetoothDeviceData lhs, BluetoothDeviceData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.getName(), rhs.getName());
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
        new MenuInflater(context).inflate(R.menu.bluetooth_name_pref_dlg_item_edit, popup.getMenu());

        int btNamePos = (int)view.getTag();
        final String btName = preference.bluetoothList.get(btNamePos).getName();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @SuppressWarnings("StringConcatenationInLoop")
            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bluetooth_name_pref_dlg_item_menu_change:
                        if (!bluetoothName.getText().toString().isEmpty()) {
                            String[] splits = preference.value.split("\\|");
                            preference.value = "";
                            boolean found = false;
                            for (String _bluetoothName : splits) {
                                if (!_bluetoothName.isEmpty()) {
                                    if (!_bluetoothName.equals(btName)) {
                                        if (!preference.value.isEmpty())
                                            preference.value = preference.value + "|";
                                        preference.value = preference.value + _bluetoothName;
                                    } else
                                        found = true;
                                }
                            }
                            PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "preference.value="+preference.value);
                            if (found) {
                                if (!preference.value.isEmpty())
                                    preference.value = preference.value + "|";
                                preference.value = preference.value + bluetoothName.getText().toString();
                            }
                            PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "preference.value="+preference.value);
                            for (BluetoothDeviceData customBluetoothName : preference.customBluetoothList) {
                                if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                                    customBluetoothName.name = bluetoothName.getText().toString();
                                    break;
                                }
                            }
                            refreshListView(false, "");
                        }
                        return true;
                    case R.id.bluetooth_name_pref_dlg_item_menu_delete:
                        preference.removeBluetoothName(btName);
                        for (BluetoothDeviceData customBluetoothName : preference.customBluetoothList)
                        {
                            if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                                preference.customBluetoothList.remove(customBluetoothName);
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
