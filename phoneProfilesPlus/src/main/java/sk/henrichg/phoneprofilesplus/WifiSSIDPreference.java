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

public class WifiSSIDPreference extends DialogPreference {
	
	private String value;
	public List<WifiSSIDData> SSIDList = null;
	
	Context context;
	
	private LinearLayout progressLinearLayout;
	private RelativeLayout dataRelativeLayout;
	private EditText SSIDName;
	private Button rescanButton;
	private ListView SSIDListView;
	private WifiSSIDPreferenceAdapter listAdapter;
	
	private AsyncTask<Void, Integer, Void> rescanAsyncTask; 
	
    public WifiSSIDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;
        
        SSIDList = new ArrayList<WifiSSIDData>();
    }

    @SuppressLint("InflateParams")
	@Override
    protected View onCreateDialogView() {

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_wifi_ssid_pref_dialog, null);

        progressLinearLayout = (LinearLayout) view.findViewById(R.id.wifi_ssid_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) view.findViewById(R.id.wifi_ssid_pref_dlg_rella_data);
        
        SSIDName = (EditText) view.findViewById(R.id.wifi_ssid_pref_dlg_bt_name);
        SSIDName.setText(value);
        
    	if (android.os.Build.VERSION.SDK_INT >= 20)
    	{
	        View buttonSeparator = view.findViewById(R.id.wifi_ssid_pref_dlg_button_separator);
	        buttonSeparator.setVisibility(View.GONE);
    	}
        
        rescanButton = (Button) view.findViewById(R.id.wifi_ssid_pref_dlg_rescan);
        rescanButton.setOnClickListener(new View.OnClickListener()
    	{
            public void onClick(View v) {
                refreshListView(true);
            }
        });
        
        SSIDListView = (ListView) view.findViewById(R.id.wifi_ssid_pref_dlg_listview);
        listAdapter = new WifiSSIDPreferenceAdapter(context, this);
        SSIDListView.setAdapter(listAdapter);

        refreshListView(false);
        
		SSIDListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				WifiSSIDPreferenceAdapter.ViewHolder viewHolder = 
						(WifiSSIDPreferenceAdapter.ViewHolder)v.getTag();
				viewHolder.radioBtn.setChecked(true);
            	setSSID(SSIDList.get(position).ssid);
			}

		});
		
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    	if (!rescanAsyncTask.isCancelled())
    		rescanAsyncTask.cancel(true);
    	
        if (positiveResult) {

        	SSIDName.clearFocus();
        	value = SSIDName.getText().toString();
        	
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

    public String getSSID()
    {
    	return value;
    }
    
    public void setSSID(String SSID)
    {
    	value = SSID;
    	this.SSIDName.setText(value);
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
				SSIDList.clear();
				
				if (_forRescan)
				{
	            	GlobalData.setForceOneWifiScan(context, true);
	            	WifiScanAlarmBroadcastReceiver.startScanner(context);

	            	try {
			        	Thread.sleep(200);
				    } catch (InterruptedException e) {
				        System.out.println(e);
				    }
		        	ScannerService.waitForWifiScanEnd(context, this);
		        }

				if (WifiScanAlarmBroadcastReceiver.wifiConfigurationList != null)
				{
					for (WifiSSIDData wifiConfiguration : WifiScanAlarmBroadcastReceiver.wifiConfigurationList)
					{
			        	SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid.replace("\"", ""), wifiConfiguration.bssid));
					}
				}
		        
		        if (WifiScanAlarmBroadcastReceiver.scanResults != null)
		        {
			        for (WifiSSIDData scanResult : WifiScanAlarmBroadcastReceiver.scanResults)
			        {
			        	if (!DataWrapper.getSSID(scanResult).isEmpty())
			        	{
				        	boolean exists = false;
				        	for (WifiSSIDData ssidData : SSIDList)
				        	{
				        		if (DataWrapper.compareSSID(scanResult, ssidData.ssid))
				        		{
				        			exists = true;
				        			break;
				        		}
				        	}
				        	if (!exists)
				        		SSIDList.add(new WifiSSIDData(DataWrapper.getSSID(scanResult), scanResult.bssid));
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
				
				for (int position = 0; position < SSIDList.size()-1; position++)
				{
					if (SSIDList.get(position).ssid.equals(value))
					{
						SSIDListView.setSelection(position);
						SSIDListView.setItemChecked(position, true);
						SSIDListView.smoothScrollToPosition(position);
						break;
					}
				}
			}
			
		};
		
		rescanAsyncTask.execute();
    }
    
}