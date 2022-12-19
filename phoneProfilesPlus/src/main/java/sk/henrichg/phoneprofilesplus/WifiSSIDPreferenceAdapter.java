package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
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

class WifiSSIDPreferenceAdapter extends BaseAdapter
{
    private final WifiSSIDPreference preference;
    //private RadioButton selectedRB;
    //int selectedRBIndex = -1;

    private final LayoutInflater inflater;
    private final Context context;

    WifiSSIDPreferenceAdapter(Context context, WifiSSIDPreference preference)
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

    @SuppressLint("SetTextI18n")
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
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        // must be set, without this not working long click
        holder.checkBox.setFocusable(false);
        holder.checkBox.setFocusableInTouchMode(false);
        holder.itemEditMenu.setFocusable(false);
        holder.itemEditMenu.setFocusableInTouchMode(false);

        switch (wifiSSID.ssid) {
            case EventPreferencesWifi.ALL_SSIDS_VALUE:
                holder.SSIDName.setText("[\u00A0" + context.getString(R.string.wifi_ssid_pref_dlg_all_ssids_chb) + "\u00A0]");
                break;
            case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                holder.SSIDName.setText("[\u00A0" + context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb) + "\u00A0]");
                break;
            default:
                String ssidName = "";
                if (wifiSSID.configured)
                    ssidName = "(C)";
                else if (wifiSSID.scanned)
                    ssidName = "(S)";
                if (!ssidName.isEmpty())
                    ssidName = ssidName + " ";
                ssidName = ssidName + wifiSSID.ssid;
                holder.SSIDName.setText(ssidName);
                break;
        }

        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(preference.isSSIDSelected(wifiSSID.ssid));
        holder.checkBox.setOnClickListener(v -> {
            CheckBox chb = (CheckBox) v;

            String ssid = preference.SSIDList.get((Integer)chb.getTag()).ssid;

            if (chb.isChecked())
                preference.addSSID(ssid);
            else
                preference.removeSSID(ssid);
        });

        if (!(wifiSSID.custom || wifiSSID.configured || wifiSSID.scanned))
            holder.itemEditMenu.setVisibility(View.GONE);
        else
            holder.itemEditMenu.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(holder.itemEditMenu, context.getString(R.string.tooltip_options_menu));
        holder.itemEditMenu.setTag(position);
        final ImageView itemEditMenu = holder.itemEditMenu;
        holder.itemEditMenu.setOnClickListener(v -> preference.showEditMenu(itemEditMenu, wifiSSID));

        return vi;
    }

}
