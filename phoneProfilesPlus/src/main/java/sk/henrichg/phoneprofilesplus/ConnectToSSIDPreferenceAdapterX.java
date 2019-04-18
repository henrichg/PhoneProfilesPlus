package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class ConnectToSSIDPreferenceAdapterX extends BaseAdapter
{
    private final ConnectToSSIDDialogPreferenceX preference;

    private final LayoutInflater inflater;

    ConnectToSSIDPreferenceAdapterX(Context context, ConnectToSSIDDialogPreferenceX preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return preference.ssidList.size();
    }

    public Object getItem(int position) {
        return preference.ssidList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView SSIDName;
        RadioButton radioButton;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // SSID to display
        WifiSSIDData wifiSSID = preference.ssidList.get(position);
        //System.out.println(String.valueOf(position));

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.connect_to_ssid_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.SSIDName = vi.findViewById(R.id.connect_to_ssid_pref_dlg_item_label);
            holder.radioButton = vi.findViewById(R.id.connect_to_ssid_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        switch (wifiSSID.ssid) {
            case Profile.CONNECTTOSSID_JUSTANY:
                holder.SSIDName.setText(R.string.connect_to_ssid_pref_dlg_summary_text_just_any);
                break;
            case Profile.CONNECTTOSSID_SHAREDPROFILE:
                holder.SSIDName.setText(R.string.array_pref_default_profile);
                break;
            default:
                holder.SSIDName.setText(wifiSSID.ssid);
                break;
        }

        holder.radioButton.setTag(position);
        holder.radioButton.setChecked(preference.value.equals(wifiSSID.ssid));
        holder.radioButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                preference.value = preference.ssidList.get((Integer)rb.getTag()).ssid;
                notifyDataSetChanged();
            }
        });

        return vi;
    }

}
