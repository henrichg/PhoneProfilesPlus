package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class WifiSSIDPreferenceAdapter extends BaseAdapter 
{
	WifiSSIDPreference preference;
	
    private LayoutInflater inflater;
    //private Context context;

    public WifiSSIDPreferenceAdapter(Context context, WifiSSIDPreference preference) 
    {
    	this.preference = preference;
    	
    	// Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

	public int getCount() {
		return preference.SSIDList.size();
	}

	public Object getItem(int position) {
		return preference.SSIDList.get(position); 
	}

	public long getItemId(int position) {
		return position;
	}
    
	static class ViewHolder {
		  TextView SSIDName;
		  RadioButton radioBtn;
		  int position;
		}
	
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // SSID to display
        WifiSSIDData wifiSSID = preference.SSIDList.get(position);
        //System.out.println(String.valueOf(position));

		ViewHolder holder;
        
		View vi = convertView;
		if (convertView == null)
		{
      		vi = inflater.inflate(R.layout.wifi_ssid_preference_list_item, parent, false);
      		holder = new ViewHolder();
  			holder.SSIDName = (TextView)vi.findViewById(R.id.wifi_ssid_pref_dlg_item_label);
	        holder.radioBtn = (RadioButton)vi.findViewById(R.id.wifi_ssid_pref_dlg_item_radiobtn);
  			vi.setTag(holder);        
		}
		else
		{
			holder = (ViewHolder)vi.getTag();
		}
		
		holder.SSIDName.setText(wifiSSID.ssid);

		holder.radioBtn.setTag(position);
    	holder.radioBtn.setChecked(wifiSSID.ssid.equals(preference.getSSID()));
    	holder.radioBtn.setOnClickListener(new View.OnClickListener()
    	{
            public void onClick(View v) {
            	RadioButton rb = (RadioButton) v;
            	
            	int index = (Integer)rb.getTag();
            	String ssid = preference.SSIDList.get(index).ssid;
            	preference.setSSID(ssid);
            }
        });
    	
		
		return vi;
    }

}
