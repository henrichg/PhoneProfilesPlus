package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class SingleSelectListDialogAdapter extends BaseAdapter
{
    final SingleSelectListDialog dialog;

    final CharSequence[] items;
    private final LayoutInflater inflater;

    SingleSelectListDialogAdapter(int itemsRes, SingleSelectListDialog dialog)
    {
        this.dialog = dialog;

        this.items = dialog.activity.getResources().getStringArray(itemsRes);

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(dialog.activity);
    }

    public int getCount() {
        return items.length;
    }

    public Object getItem(int position) {
        return items[position];
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView label;
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
            holder.label = vi.findViewById(R.id.pp_list_pref_dlg_item_label);
            holder.radioButton = vi.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.label.setText(items[position]);

        holder.radioButton.setChecked(dialog.itemValue == position);

        holder.radioButton.setTag(position);
        holder.radioButton.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            int pos = (Integer)rb.getTag();
            dialog.itemValue = pos;
            rb.setChecked(true);
            dialog.itemClick.onClick(dialog.mDialog, pos);
            dialog.mDialog.dismiss();
        });

        return vi;
    }

}
