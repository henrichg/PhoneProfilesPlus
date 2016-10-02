package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BluetoothNamePreference extends DialogPreference {

    private String value;
    public List<BluetoothDeviceData> bluetoothList = null;

    Context context;

    private MaterialDialog mDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private EditText bluetoothName;
    private ListView bluetoothListView;
    private BluetoothNamePreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public BluetoothNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        bluetoothList = new ArrayList<BluetoothDeviceData>();

        GlobalData.loadPreferences(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.bluetooth_name_pref_dlg_rescan_button)
                .autoDismiss(false)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
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
                        if (Permissions.grantBluetoothScanDialogPermissions(context, BluetoothNamePreference.this))
                            refreshListView(true);
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_bluetooth_name_pref_dialog, null);
        onBindDialogView(layout);

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);

        bluetoothName = (EditText) layout.findViewById(R.id.bluetooth_name_pref_dlg_bt_name);

        bluetoothListView = (ListView) layout.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapter(context, this);
        bluetoothListView.setAdapter(listAdapter);

        refreshListView(false);

        bluetoothListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                BluetoothNamePreferenceAdapter.ViewHolder viewHolder =
                        (BluetoothNamePreferenceAdapter.ViewHolder) v.getTag();

                viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                if (viewHolder.checkBox.isChecked()) {
                    addBluetoothName(bluetoothList.get(position).getName());
                }
                else {
                    removeBluetoothName(bluetoothList.get(position).getName());
                }
            }

        });

        mBuilder.customView(layout, false);

        final TextView helpText = (TextView)layout.findViewById(R.id.bluetooth_name_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                context.getString(R.string.bluetooth_name_pref_dlg_info_about_wildcards) + " " +
                context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.bluetooth_name_pref_dlg_helpIcon);
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
    public void onDismiss(DialogInterface dialog) {

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

    public String getBluetoothNames()
    {
        return value;
    }

    public void addBluetoothName(String bluetoothName) {
        String[] splits = value.split("\\|");
        value = "";
        boolean found = false;
        for (String _bluetoothName : splits) {
            if (!_bluetoothName.isEmpty()) {
                if (!_bluetoothName.equals(bluetoothName)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _bluetoothName;
                } else
                    found = true;
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + bluetoothName;
        }
    }

    public void removeBluetoothName(String bluetoothName) {
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

    public boolean isBluetoothNameSelected(String bluetoothName) {
        String[] splits = value.split("\\|");
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
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

                dataRelativeLayout.setVisibility(View.GONE);
                progressLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                bluetoothList.clear();

                if (_forRescan)
                {
                    GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_FROM_PREF_DIALOG);
                    BluetoothScanAlarmBroadcastReceiver.startScanner(context);

                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    GlobalData.sleep(200);
                    ScannerService.waitForForceOneBluetoothScanEnd(context, this);
                }

                bluetoothList.add(new BluetoothDeviceData(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE, "", BluetoothDevice.DEVICE_TYPE_DUAL));

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanAlarmBroadcastReceiver.getBoundedDevicesList(context);
                if (boundedDevicesList != null)
                {
                    for (BluetoothDeviceData device : boundedDevicesList)
                    {
                        bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type));
                    }
                }

                List<BluetoothDeviceData> scanResults = BluetoothScanAlarmBroadcastReceiver.getScanResults(context);
                if (scanResults != null)
                {
                    for (BluetoothDeviceData device : scanResults)
                    {
                        if (!device.getName().isEmpty())
                        {
                            boolean exists = false;
                            for (BluetoothDeviceData _device : bluetoothList)
                            {
                                if (_device.getName().equalsIgnoreCase(device.getName()))
                                {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists)
                                bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address, device.type));
                        }
                    }
                }

                // add all from value
                boolean found;
                String[] splits = value.split("\\|");
                for (String _bluetoothName : splits) {
                    if (_bluetoothName.isEmpty()) {
                        found = false;
                        for (BluetoothDeviceData bluetoothName : bluetoothList) {
                            if (_bluetoothName.equals(bluetoothName.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            bluetoothList.add(new BluetoothDeviceData(_bluetoothName, "", BluetoothDevice.DEVICE_TYPE_DUAL));
                        }
                    }
                }

                Collections.sort(bluetoothList, new SortList());

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listAdapter.notifyDataSetChanged();
                progressLinearLayout.setVisibility(View.GONE);
                dataRelativeLayout.setVisibility(View.VISIBLE);

                /*
                for (int position = 0; position < bluetoothList.size() - 1; position++) {
                    if (bluetoothList.get(position).getName().equalsIgnoreCase(value)) {
                        bluetoothListView.setSelection(position);
                        bluetoothListView.setItemChecked(position, true);
                        bluetoothListView.smoothScrollToPosition(position);
                        break;
                    }
                }
                */
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<BluetoothDeviceData> {

        public int compare(BluetoothDeviceData lhs, BluetoothDeviceData rhs) {
            return GUIData.collator.compare(lhs.getName(), rhs.getName());
        }

    }

}