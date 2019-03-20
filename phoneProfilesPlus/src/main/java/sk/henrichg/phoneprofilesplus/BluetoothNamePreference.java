package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
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

public class BluetoothNamePreference extends DialogPreference {

    private String value;
    List<BluetoothDeviceData> bluetoothList;
    private final List<BluetoothDeviceData> customBluetoothList;

    private final Context context;

    private AlertDialog mDialog;
    private AlertDialog mSelectorDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private ListView bluetoothListView;
    private EditText bluetoothName;
    private AppCompatImageButton addIcon;
    private BluetoothNamePreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    static boolean forceRegister = false;

    //private static final String PREF_SHOW_HELP = "bluetooth_name_pref_show_help";

    public BluetoothNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        bluetoothList = new ArrayList<>();
        customBluetoothList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {
        value = getPersistedString(value);

        PPApplication.forceRegisterReceiversForBluetoothScanner(context);
        forceRegister = true;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                            /*
                            bluetoothName.clearFocus();

                            String editText = bluetoothName.getText().toString();
                            if (editText.equals(context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb)))
                                value = EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE;
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
        View layout = inflater.inflate(R.layout.activity_bluetooth_name_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                refreshListView(false, "");
            }
        });

        //noinspection ConstantConditions
        progressLinearLayout = layout.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        dataRelativeLayout = layout.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);

        addIcon = layout.findViewById(R.id.bluetooth_name_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btName = bluetoothName.getText().toString();
                addBluetoothName(btName);
                boolean found = false;
                for (BluetoothDeviceData customBtNameData : customBluetoothList) {
                    if (customBtNameData.getName().equalsIgnoreCase(btName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //if (android.os.Build.VERSION.SDK_INT >= 18)
                        customBluetoothList.add(new BluetoothDeviceData(btName, "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                    //else
                    //    customBluetoothList.add(new BluetoothDeviceData(btName, "", 0, true, 0));
                }
                refreshListView(false, btName);
            }
        });

        bluetoothName = layout.findViewById(R.id.bluetooth_name_pref_dlg_bt_name);
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
                        addIcon, R.drawable.ic_button_add, context.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!bluetoothName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, context.getApplicationContext());

        bluetoothListView = layout.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapter(context, this);
        bluetoothListView.setAdapter(listAdapter);

        //refreshListView(false, "");

        bluetoothListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //BluetoothNamePreferenceAdapter.ViewHolder viewHolder =
                //        (BluetoothNamePreferenceAdapter.ViewHolder) v.getTag();
                String btName = bluetoothList.get(position).getName();
                if (!(btName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE) ||
                      btName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE))) {
                    bluetoothName.setText(btName);
                }
                /*
                viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                if (viewHolder.checkBox.isChecked()) {
                    addBluetoothName(bluetoothList.get(position).getName());
                }
                else {
                    removeBluetoothName(bluetoothList.get(position).getName());
                }
                */
            }

        });

        final ImageView helpIcon = layout.findViewById(R.id.bluetooth_name_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString = context.getString(R.string.event_preference_bluetooth_btName_type)+"\n\n"+
                        context.getString(R.string.event_preference_bluetooth_bt_types)+"\n\n"+
                        context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                        context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                        context.getString(R.string.bluetooth_name_pref_dlg_info_about_wildcards) + " " +
                        context.getString(R.string.pref_dlg_info_about_wildcards_3);

                DialogHelpPopupWindow.showPopup(helpIcon, (Activity)context, mDialog, helpString);
            }
        });


        ImageView changeSelectionIcon = layout.findViewById(R.id.bluetooth_name_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_dlg_change_selection_title)
                        .setCancelable(true)
                        .setNegativeButton(getNegativeButtonText(), null)
                        //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                        .setItems(R.array.bluetoothNameDChangeSelectionArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        value = "";
                                        break;
                                    case 1:
                                        for (BluetoothDeviceData bluetooth : bluetoothList) {
                                            if (bluetooth.name.equals(bluetoothName.getText().toString()))
                                                addBluetoothName(bluetooth.name);
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

        final Button rescanButton = layout.findViewById(R.id.bluetooth_name_pref_dlg_rescanButton);
        //rescanButton.setAllCaps(false);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.grantBluetoothScanDialogPermissions(context))
                    refreshListView(true, "");
            }
        });

        final TextView locationEnabledStatusTextView = layout.findViewById(R.id.bluetooth_name_pref_dlg_locationEnableStatus);
        String statusText;
        if (!PhoneProfilesService.isLocationEnabled(context)) {
            statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";
        } else {
            statusText = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                    context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
        }
        locationEnabledStatusTextView.setText(statusText);

        AppCompatImageButton locationSystemSettingsButton = layout.findViewById(R.id.bluetooth_name_pref_dlg_locationSystemSettingsButton);
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
        forceRegister = false;
        PPApplication.reregisterReceiversForBluetoothScanner(context);
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

    /*public String getBluetoothNames()
    {
        return value;
    }*/

    void addBluetoothName(String bluetoothName) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + bluetoothName;
        }
        //Log.d("BluetoothNamePreference.addBluetoothName","value="+value);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeBluetoothName(String bluetoothName) {
        String[] splits = value.split("\\|");
        value = "";
        for (String _bluetoothName : splits) {
            if (!_bluetoothName.isEmpty()) {
                if (!_bluetoothName.equals(bluetoothName)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _bluetoothName;
                }
            }
        }
    }

    boolean isBluetoothNameSelected(String bluetoothName) {
        String[] splits = value.split("\\|");
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(boolean forRescan, final String scrollToBTName)
    {
        if ((mDialog != null) && mDialog.isShowing()) {
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
                        WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                        WifiBluetoothScanner.setForceOneLEBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                        BluetoothScanJob.startScanner(context, true);

                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        PPApplication.sleep(500);
                        WifiBluetoothScanner.waitForForceOneBluetoothScanEnd(context, this);
                    }

                    List<BluetoothDeviceData> boundedDevicesList = BluetoothScanJob.getBoundedDevicesList(context);
                    if (boundedDevicesList != null) {
                        for (BluetoothDeviceData device : boundedDevicesList) {
                            _bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, true, false));
                        }
                    }

                    List<BluetoothDeviceData> scanResults = BluetoothScanJob.getScanResults(context);
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
                    String[] splits = value.split("\\|");
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
                            for (BluetoothDeviceData bluetoothName : customBluetoothList) {
                                if (_bluetoothName.equals(bluetoothName.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                                customBluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", BluetoothDevice.DEVICE_TYPE_DUAL, true, 0, false, false));
                            /*}
                            else {
                                customBluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", 0, true, 0));
                            }*/
                            }
                        }
                    }

                    // add custom Bluetooth names
                    for (BluetoothDeviceData customBTName : customBluetoothList) {
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

                    Collections.sort(_bluetoothList, new SortList());

                    //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE, "", BluetoothDevice.DEVICE_TYPE_DUAL, false, 0, false, false));
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE, "", BluetoothDevice.DEVICE_TYPE_DUAL, false, 0, false, false));
                /*}
                else {
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE, "", 0, false, 0));
                    _bluetoothList.add(0, new BluetoothDeviceData(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE, "", 0, false, 0));
                }*/

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    bluetoothList = new ArrayList<>(_bluetoothList);
                    listAdapter.notifyDataSetChanged();

                    if (_forRescan) {
                        progressLinearLayout.setVisibility(View.GONE);
                        dataRelativeLayout.setVisibility(View.VISIBLE);
                    }

                    if (!scrollToBTName.isEmpty())
                        for (int position = 0; position < bluetoothList.size() - 1; position++) {
                            if (bluetoothList.get(position).getName().equalsIgnoreCase(scrollToBTName)) {
                                bluetoothListView.setSelection(position);
                                break;
                            }
                        }
                }

            };

            rescanAsyncTask.execute();
        }
    }

    private class SortList implements Comparator<BluetoothDeviceData> {

        public int compare(BluetoothDeviceData lhs, BluetoothDeviceData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.getName(), rhs.getName());
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
        new MenuInflater(context).inflate(R.menu.bluetooth_name_pref_dlg_item_edit, popup.getMenu());

        int btNamePos = (int)view.getTag();
        final String btName = bluetoothList.get(btNamePos).getName();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @SuppressWarnings("StringConcatenationInLoop")
            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bluetooth_name_pref_dlg_item_menu_change:
                        if (!bluetoothName.getText().toString().isEmpty()) {
                            String[] splits = value.split("\\|");
                            value = "";
                            boolean found = false;
                            for (String _bluetoothName : splits) {
                                if (!_bluetoothName.isEmpty()) {
                                    if (!_bluetoothName.equals(btName)) {
                                        if (!value.isEmpty())
                                            value = value + "|";
                                        value = value + _bluetoothName;
                                    } else
                                        found = true;
                                }
                            }
                            if (found) {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + bluetoothName.getText().toString();
                            }
                            for (BluetoothDeviceData customBluetoothName : customBluetoothList) {
                                if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                                    customBluetoothName.name = bluetoothName.getText().toString();
                                    break;
                                }
                            }
                            refreshListView(false, "");
                        }
                        return true;
                    case R.id.bluetooth_name_pref_dlg_item_menu_delete:
                        removeBluetoothName(btName);
                        for (BluetoothDeviceData customBluetoothName : customBluetoothList)
                        {
                            if (customBluetoothName.getName().equalsIgnoreCase(btName)) {
                                customBluetoothList.remove(customBluetoothName);
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