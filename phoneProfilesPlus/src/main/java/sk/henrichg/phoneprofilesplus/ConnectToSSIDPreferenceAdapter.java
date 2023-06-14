package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class ConnectToSSIDPreferenceAdapter extends BaseAdapter
{
    private final ConnectToSSIDDialogPreference preference;
    private final Context context;

    private final LayoutInflater inflater;

    ConnectToSSIDPreferenceAdapter(Context context, ConnectToSSIDDialogPreference preference)
    {
        this.preference = preference;
        this.context = context;

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
    
    private static class ViewHolder {
        TextView SSIDName;
        RadioButton radioButton;
        //int position;
    }

    @SuppressLint("SetTextI18n")
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // SSID to display
        WifiSSIDData wifiSSID = preference.ssidList.get(position);
        //System.out.println(String.valueOf(position));

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.listitem_connect_to_ssid_preference, parent, false);
            holder = new ViewHolder();
            holder.SSIDName = vi.findViewById(R.id.connect_to_ssid_pref_dlg_item_label);
            holder.radioButton = vi.findViewById(R.id.connect_to_ssid_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        if (Profile.CONNECTTOSSID_JUSTANY.equals(wifiSSID.ssid)) {
            holder.SSIDName.setText("[\u00A0" + context.getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any) + "\u00A0]");
        } else {
            String ssid = wifiSSID.ssid.replace("\"", "");
            holder.SSIDName.setText(ssid);
        }

        holder.radioButton.setTag(position);
        holder.radioButton.setChecked(preference.value.equals(wifiSSID.ssid));
        holder.radioButton.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            preference.value = preference.ssidList.get((Integer)rb.getTag()).ssid;
            notifyDataSetChanged();
        });

        return vi;
    }

}
