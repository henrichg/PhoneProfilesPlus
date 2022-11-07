package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class MultiSelectListDialogAdapter extends BaseAdapter
{
    final MultiSelectListDialog dialog;

    final CharSequence[] items;
    private final LayoutInflater inflater;

    MultiSelectListDialogAdapter(int itemsRes, MultiSelectListDialog dialog)
    {
        this.dialog = dialog;

        this.items = dialog.activity.getResources().getStringArray(itemsRes);

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(dialog.activity);
    }

    public int getCount() {
        return dialog.itemValues.length;
    }

    public Object getItem(int position) {
        return dialog.itemValues[position];
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView label;
        CheckBox checkBox;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.pp_multiselect_list_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.label = vi.findViewById(R.id.pp_multiselect_list_pref_dlg_item_label);
            holder.checkBox = vi.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.label.setText(items[position]);

        holder.checkBox.setChecked(dialog.itemValues[position]);

        holder.checkBox.setTag(position);
        holder.checkBox.setOnClickListener(v -> {
            CheckBox chb = (CheckBox) v;
            int pos = (Integer)chb.getTag();
            dialog.itemValues[pos] = !dialog.itemValues[pos];
            chb.setChecked(dialog.itemValues[pos]);
        });

        return vi;
    }

}
