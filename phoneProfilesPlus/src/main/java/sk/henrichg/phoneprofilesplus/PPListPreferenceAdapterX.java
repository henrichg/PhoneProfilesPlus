package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class PPListPreferenceAdapterX extends BaseAdapter
{
    private final PPListPreference preference;

    private final LayoutInflater inflater;

    PPListPreferenceAdapterX(Context context, PPListPreference preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return preference.entryValues.length;
    }

    public Object getItem(int position) {
        return preference.entryValues[position];
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView entry;
        RadioButton radioButton;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.pp_list_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.entry = vi.findViewById(R.id.pp_list_pref_dlg_item_label);
            holder.radioButton = vi.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.entry.setText(preference.entries[position]);

        holder.radioButton.setTag(position);
        holder.radioButton.setChecked(preference.value.equals(preference.entryValues[position].toString()));
        holder.radioButton.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            preference.value = preference.entryValues[(Integer)rb.getTag()].toString();
            //notifyDataSetChanged();
            preference.persistValue();
            preference.fragment.dismiss();
        });

        return vi;
    }

}
