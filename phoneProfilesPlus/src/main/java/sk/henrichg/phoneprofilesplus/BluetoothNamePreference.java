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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class BluetoothNamePreference extends DialogPreference {
	
	private String value;
	public List<BluetoothDeviceData> bluetoothList = null;
	
	Context context;

    private MaterialDialog mDialog;
	private LinearLayout progressLinearLayout;
	private RelativeLayout dataRelativeLayout;
	private EditText bluetoothName;
	private Button rescanButton;
	private ListView bluetoothListView;
	private BluetoothNamePreferenceAdapter listAdapter;
	
	private AsyncTask<Void, Integer, Void> rescanAsyncTask; 
	
    public BluetoothNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        bluetoothList = new ArrayList<BluetoothDeviceData>();
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
                .callback(callback)
                .autoDismiss(false)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_bluetooth_name_pref_dialog, null);
        onBindDialogView(layout);

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);

        bluetoothName = (EditText) layout.findViewById(R.id.bluetooth_name_pref_dlg_bt_name);
        bluetoothName.setText(value);

        bluetoothListView = (ListView) layout.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapter(context, this);
        bluetoothListView.setAdapter(listAdapter);

        refreshListView(false);

        bluetoothListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                BluetoothNamePreferenceAdapter.ViewHolder viewHolder =
                        (BluetoothNamePreferenceAdapter.ViewHolder)v.getTag();
                viewHolder.radioBtn.setChecked(true);
                setBluetoothName(bluetoothList.get(position).getName());
            }

        });

        mBuilder.customView(layout, false);

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
            if (shouldPersist()) {
                bluetoothName.clearFocus();
                value = bluetoothName.getText().toString();

                if (callChangeListener(value))
                {
                    persistString(value);
                }
            }
            mDialog.dismiss();
        }
        @Override
        public void onNegative(MaterialDialog dialog) {
            mDialog.dismiss();
        }
        @Override
        public void onNeutral(MaterialDialog dialog) {
            refreshListView(true);
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
    	
    	if (!rescanAsyncTask.isCancelled())
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

    public String getBluetoothName()
    {
    	return value;
    }
    
    public void setBluetoothName(String bluetoothName)
    {
    	value = bluetoothName;
    	this.bluetoothName.setText(value);
    }
    
    private void refreshListView(boolean forRescan)
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
	            	GlobalData.setForceOneBluetoothScan(context, true);
	            	BluetoothScanAlarmBroadcastReceiver.startScanner(context);

	            	try {
			        	Thread.sleep(200);
				    } catch (InterruptedException e) {
				        System.out.println(e);
				    }
		        	ScannerService.waitForBluetoothScanEnd(context, this);
		        }

                BluetoothScanAlarmBroadcastReceiver.getBoundedDevicesList(context);
				if (BluetoothScanAlarmBroadcastReceiver.boundedDevicesList != null)
				{
			        for (BluetoothDeviceData device : BluetoothScanAlarmBroadcastReceiver.boundedDevicesList)
			        {
			        	bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address));
			        }
				}

                BluetoothScanAlarmBroadcastReceiver.getScanResults(context);
		        if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
		        {
			        for (BluetoothDeviceData device : BluetoothScanAlarmBroadcastReceiver.scanResults)
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
				        		bluetoothList.add(new BluetoothDeviceData(device.getName(), device.address));
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
				
				for (int position = 0; position < bluetoothList.size()-1; position++)
				{
					if (bluetoothList.get(position).getName().equalsIgnoreCase(value))
					{
						bluetoothListView.setSelection(position);
						bluetoothListView.setItemChecked(position, true);
						bluetoothListView.smoothScrollToPosition(position);
						break;
					}
				}
			}
			
		};
		
		rescanAsyncTask.execute();
    }
    
}