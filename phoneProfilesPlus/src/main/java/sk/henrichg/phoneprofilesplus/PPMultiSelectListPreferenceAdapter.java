package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class PPMultiSelectListPreferenceAdapter extends BaseAdapter
{
    private final PPMultiSelectListPreference preference;

    private final LayoutInflater inflater;

    PPMultiSelectListPreferenceAdapter(Context context, PPMultiSelectListPreference preference)
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
        CheckBox checkBox;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.listitem_pp_multiselect_list_preference, parent, false);
            holder = new ViewHolder();
            holder.entry = vi.findViewById(R.id.pp_multiselect_list_pref_dlg_item_label);
            holder.checkBox = vi.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.entry.setText(preference.entries[position]);

        holder.checkBox.setTag(position);

        boolean found = false;
        String valueFromPos = preference.entryValues[position].toString();
        for (String value : preference.value) {
            if (value.equals(valueFromPos)) {
                found = true;
                break;
            }
        }
        holder.checkBox.setChecked(found);

        holder.checkBox.setOnClickListener(v -> {
            CheckBox chb = (CheckBox) v;
            int pos = (Integer)chb.getTag();

            String _valueFromPos = preference.entryValues[pos].toString();

            // search for value from position in preference.value
            boolean _found = false;
            for (String value : preference.value) {
                if (value.equals(_valueFromPos)) {
                    _found = true;
                    break;
                }
            }
            if (_found) {
                // value form position exists in value
                preference.value.remove(_valueFromPos);
                _found = false;
            } else {
                // value form position not exists in value
                preference.value.add(_valueFromPos);
                _found = true;
            }
            chb.setChecked(_found);
            //preference.persistValue();
        });

        return vi;
    }

}
