package sk.henrichg.phoneprofilesplus;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothNamePreference extends DialogPreference {
	
	private String value;
	public List<BluetoothDeviceData> bluetoothList = null;
	
	Context context;
	
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

    @SuppressLint("InflateParams")
	@Override
    protected View onCreateDialogView() {

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_bluetooth_name_pref_dialog, null);

        progressLinearLayout = (LinearLayout) view.findViewById(R.id.bluetooth_name_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) view.findViewById(R.id.bluetooth_name_pref_dlg_rella_data);
        
        bluetoothName = (EditText) view.findViewById(R.id.bluetooth_name_pref_dlg_bt_name);
        bluetoothName.setText(value);

    	if (android.os.Build.VERSION.SDK_INT >= 20)
    	{
	        View buttonSeparator = view.findViewById(R.id.bluetooth_name_pref_dlg_button_separator);
	        buttonSeparator.setVisibility(View.GONE);
    	}
        
        rescanButton = (Button) view.findViewById(R.id.bluetooth_name_pref_dlg_rescan);
        rescanButton.setOnClickListener(new View.OnClickListener()
    	{
            public void onClick(View v) {
                refreshListView(true);
            }
        });
        
        bluetoothListView = (ListView) view.findViewById(R.id.bluetooth_name_pref_dlg_listview);
        listAdapter = new BluetoothNamePreferenceAdapter(context, this);
        bluetoothListView.setAdapter(listAdapter);
        
        refreshListView(false);
        
		bluetoothListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				BluetoothNamePreferenceAdapter.ViewHolder viewHolder = 
						(BluetoothNamePreferenceAdapter.ViewHolder)v.getTag();
				viewHolder.radioBtn.setChecked(true);
            	setBluetoothName(bluetoothList.get(position).name);
			}

		});
		
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    	
    	if (!rescanAsyncTask.isCancelled())
    		rescanAsyncTask.cancel(true);
    	
        if (positiveResult) {

        	bluetoothName.clearFocus();
        	value = bluetoothName.getText().toString();
        	
    		if (callChangeListener(value))
    		{
	            persistString(value);
    		}
        }
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

				if (BluetoothScanAlarmBroadcastReceiver.boundedDevicesList != null)
				{
			        for (BluetoothDeviceData device : BluetoothScanAlarmBroadcastReceiver.boundedDevicesList)
			        {
			        	bluetoothList.add(new BluetoothDeviceData(device.name, device.address));
			        }
				}
		        
		        if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
		        {
			        for (BluetoothDeviceData device : BluetoothScanAlarmBroadcastReceiver.scanResults)
			        {
			        	if (!device.name.isEmpty())
			        	{
				        	boolean exists = false;
				        	for (BluetoothDeviceData _device : bluetoothList)
				        	{
				        		if (_device.name.equalsIgnoreCase(device.name))
				        		{
				        			exists = true;
				        			break;
				        		}
				        	}
				        	if (!exists)
				        		bluetoothList.add(new BluetoothDeviceData(device.name, device.address));
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
					if (bluetoothList.get(position).name.equalsIgnoreCase(value))
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