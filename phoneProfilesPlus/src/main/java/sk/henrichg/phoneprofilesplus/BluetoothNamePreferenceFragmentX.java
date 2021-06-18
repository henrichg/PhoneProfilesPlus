package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private RefreshListViewAsyncTask rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (BluetoothNamePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_bluetooth_name_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        PPApplication.forceRegisterReceiversForBluetoothScanner(prefContext);
        BluetoothNamePreferenceX.forceRegister = true;

        progressLinearLayout = view.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        dataRelativeLayout = view.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_addIcon);
        TooltipCompat.setTooltipText(addIcon, getString(R.string.bluetooth_name_pref_dlg_add_button_tooltip));
        addIcon.setOnClickListener(v -> {
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
                        addIcon, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!bluetoothName.getText().toString().isEmpty(),
                addIcon, prefContext.getApplicationContext());

        bluetoothListView = view.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapterX(prefContext, preference);
        bluetoothListView.setAdapter(listAdapter);

        bluetoothListView.setOnItemClickListener((parent, v, position, id) -> {
            String btName = preference.bluetoothList.get(position).getName();
            if (!(btName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE) ||
                    btName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE))) {
                bluetoothName.setText(btName);
            }
        });

        final ImageView helpIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> {
            String helpString = getString(R.string.event_preference_bluetooth_btName_type)+"\n\n"+
                    //getString(R.string.event_preference_bluetooth_bt_types)+"\n\n"+
                    getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                    getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                    getString(R.string.bluetooth_name_pref_dlg_info_about_wildcards) + " " +
                    getString(R.string.pref_dlg_info_about_wildcards_3);

            DialogHelpPopupWindowX.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, getDialog(), helpString);
        });


        ImageView changeSelectionIcon = view.findViewById(R.id.bluetooth_name_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.bluetooth_name_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSelectorDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.pref_dlg_change_selection_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.bluetoothNameDChangeSelectionArray, (dialog, which) -> {
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
                            })
                            .create();

//                    mSelectorDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    mSelectorDialog.show();
                }
        });

        rescanButton = view.findViewById(R.id.bluetooth_name_pref_dlg_rescanButton);
        rescanButton.setOnClickListener(v -> {
            if (Permissions.grantBluetoothScanDialogPermissions(prefContext))
                refreshListView(true, "");
        });

        locationSystemSettingsRelLa = view.findViewById(R.id.bluetooth_name_pref_dlg_locationSystemSettingsRelLa);
        locationEnabledStatusTextView = view.findViewById(R.id.bluetooth_name_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.bluetooth_name_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

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
        BluetoothScanner.setForceOneBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
        BluetoothScanner.setForceOneLEBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED))) {
            //PPApplication.logE("BluetoothNamePreferenceFragmentX.onDialogClosed","cancel asyncTask");
            rescanAsyncTask.cancel(true);
        }

        BluetoothNamePreferenceX.forceRegister = false;
        PPApplication.reregisterReceiversForBluetoothScanner(prefContext);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        //if (Build.VERSION.SDK_INT >= 23) {
            String statusText;
            if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
                statusText = getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                        "* " + getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

                locationEnabledStatusTextView.setText(statusText);

                locationSystemSettingsButton.setOnClickListener(v -> {
                    boolean ok = false;
                    if (getActivity() != null) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //noinspection deprecation
                                getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            if (!ok) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();

//                                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                        @Override
//                                        public void onShow(DialogInterface dialog) {
//                                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                            if (positive != null) positive.setAllCaps(false);
//                                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                            if (negative != null) negative.setAllCaps(false);
//                                        }
//                                    });

                                if (getActivity() != null)
                                    if (!getActivity().isFinishing())
                                        dialog.show();
                            }
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

    @SuppressLint("StaticFieldLeak")
    void refreshListView(boolean forRescan, final String scrollToBTName)
    {
        rescanAsyncTask = new RefreshListViewAsyncTask(forRescan, scrollToBTName,
                preference, this, prefContext) ;
        rescanAsyncTask.execute();
    }

    private static class SortList implements Comparator<BluetoothDeviceData> {

        public int compare(BluetoothDeviceData lhs, BluetoothDeviceData rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.getName(), rhs.getName());
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
        new MenuInflater(_context).inflate(R.menu.bluetooth_name_pref_dlg_item_edit, popup.getMenu());

        int btNamePos = (int)view.getTag();
        final String btName = preference.bluetoothList.get(btNamePos).getName();

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bluetooth_name_pref_dlg_item_menu_change) {
                if (!bluetoothName.getText().toString().isEmpty()) {
                    String[] splits = preference.value.split("\\|");
                    preference.value = "";
                    boolean found = false;
                    for (String _bluetoothName : splits) {
                        if (!_bluetoothName.isEmpty()) {
                            if (!_bluetoothName.equals(btName)) {
                                if (!preference.value.isEmpty())
                                    //noinspection StringConcatenationInLoop
                                    preference.value = preference.value + "|";
                                //noinspection StringConcatenationInLoop
                                preference.value = preference.value + _bluetoothName;
                            } else
                                found = true;
                        }
                    }
                    //PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "preference.value="+preference.value);
                    if (found) {
                        if (!preference.value.isEmpty())
                            preference.value = preference.value + "|";
                        preference.value = preference.value + bluetoothName.getText().toString();
                    }
                    //PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "preference.value="+preference.value);
                    for (BluetoothDeviceData customBluetoothName : preference.customBluetoothList) {
                        if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                            customBluetoothName.name = bluetoothName.getText().toString();
                            break;
                        }
                    }
                    refreshListView(false, "");
                }
                return true;
            }
            else
            if (itemId == R.id.bluetooth_name_pref_dlg_item_menu_delete) {
                if (getActivity() != null) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(getResources().getString(R.string.profile_context_item_delete));
                    dialogBuilder.setMessage(getResources().getString(R.string.delete_bluetooth_name_alert_message));
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                        preference.removeBluetoothName(btName);
                        for (BluetoothDeviceData customBluetoothName : preference.customBluetoothList) {
                            if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                                preference.customBluetoothList.remove(customBluetoothName);
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
            else {
                return false;
            }
        });

        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        List<BluetoothDeviceData> _bluetoothList = null;
        final boolean forRescan;
        final String scrollToBTName;

        private final WeakReference<BluetoothNamePreferenceX> preferenceWeakRef;
        private final WeakReference<BluetoothNamePreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(boolean forRescan, final String scrollToBTName,
                                        BluetoothNamePreferenceX preference,
                                        BluetoothNamePreferenceFragmentX fragment,
                                        Context prefContext) {
            this.forRescan = forRescan;
            this.scrollToBTName = scrollToBTName;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _bluetoothList = new ArrayList<>();

            BluetoothNamePreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (forRescan) {
                    fragment.dataRelativeLayout.setVisibility(View.GONE);
                    fragment.progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            BluetoothNamePreferenceFragmentX fragment = fragmentWeakRef.get();
            BluetoothNamePreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                if (forRescan) {
                    //PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView","start rescan");
                    BluetoothScanner.setForceOneBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    BluetoothScanner.setForceOneLEBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    BluetoothScanWorker.startScanner(prefContext, true);

                    //PPApplication.sleep(500);
                    //WifiBluetoothScanner.waitForForceOneBluetoothScanEnd(prefContext, this);
                    //PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView","end rescan");
                }

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(prefContext);
                //if (boundedDevicesList != null) {
                for (BluetoothDeviceData device : boundedDevicesList) {
                    _bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, true, false));
                }
                //}

                List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(prefContext);
                //PPApplication.logE("BluetoothNamePreferenceFragmentX.refreshListView", "scanResults="+scanResults);
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

                //noinspection Java8ListSort
                Collections.sort(_bluetoothList, new SortList());

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
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            BluetoothNamePreferenceFragmentX fragment = fragmentWeakRef.get();
            BluetoothNamePreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                preference.bluetoothList = new ArrayList<>(_bluetoothList);
                fragment.listAdapter.notifyDataSetChanged();

                if (forRescan) {
                    BluetoothScanWorker.setScanRequest(prefContext, false);
                    BluetoothScanWorker.setWaitForResults(prefContext, false);
                    BluetoothScanWorker.setLEScanRequest(prefContext, false);
                    BluetoothScanWorker.setWaitForLEResults(prefContext, false);
                    BluetoothScanner.setForceOneBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanner.setForceOneLEBluetoothScan(prefContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanWorker.setScanKilled(prefContext, false);
                    fragment.progressLinearLayout.setVisibility(View.GONE);
                    fragment.dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                if (!scrollToBTName.isEmpty()) {
                    for (int position = 0; position < preference.bluetoothList.size() - 1; position++) {
                        if (preference.bluetoothList.get(position).getName().equalsIgnoreCase(scrollToBTName)) {
                            fragment.bluetoothListView.setSelection(position);
                            break;
                        }
                    }
                }
            }
        }

    }

}
