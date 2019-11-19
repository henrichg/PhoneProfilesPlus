package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;

class WifiSSIDPreferenceAdapterX extends BaseAdapter
{
    private final WifiSSIDPreferenceX preference;
    //private RadioButton selectedRB;
    //int selectedRBIndex = -1;

    private final LayoutInflater inflater;
    private final Context context;

    WifiSSIDPreferenceAdapterX(Context context, WifiSSIDPreferenceX preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;
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
        CheckBox checkBox;
        AppCompatImageButton itemEditMenu;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
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
            holder.SSIDName = vi.findViewById(R.id.wifi_ssid_pref_dlg_item_label);
            holder.checkBox = vi.findViewById(R.id.wifi_ssid_pref_dlg_item_checkbox);
            holder.itemEditMenu = vi.findViewById(R.id.wifi_ssid_pref_dlg_item_edit_menu);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        switch (wifiSSID.ssid) {
            case EventPreferencesWifi.ALL_SSIDS_VALUE:
                holder.SSIDName.setText(R.string.wifi_ssid_pref_dlg_all_ssids_chb);
                break;
            case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                holder.SSIDName.setText(R.string.wifi_ssid_pref_dlg_configured_ssids_chb);
                break;
            default:
                String ssidName = "";
                if (wifiSSID.configured)
                    ssidName = "(C)";
                else if (wifiSSID.scanned)
                    ssidName = "(S)";
                ssidName = ssidName + " " + wifiSSID.ssid;
                holder.SSIDName.setText(ssidName);
                break;
        }

        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(preference.isSSIDSelected(wifiSSID.ssid));
        holder.checkBox.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                CheckBox chb = (CheckBox) v;

                String ssid = preference.SSIDList.get((Integer)chb.getTag()).ssid;

                if (chb.isChecked())
                    preference.addSSID(ssid);
                else
                    preference.removeSSID(ssid);
            }
        });

        if (!wifiSSID.custom)
            holder.itemEditMenu.setVisibility(View.GONE);
        else
            holder.itemEditMenu.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(holder.itemEditMenu, context.getString(R.string.tooltip_options_menu));
        holder.itemEditMenu.setTag(position);
        final ImageView itemEditMenu = holder.itemEditMenu;
        holder.itemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preference.showEditMenu(itemEditMenu);
            }
        });

        return vi;
    }

}
