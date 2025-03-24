package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class EditorEventListItemMenuDialogAdapter extends BaseAdapter
{
    final EditorEventListItemMenuDialog dialog;

    final CharSequence[] items;
    private final LayoutInflater inflater;

    EditorEventListItemMenuDialogAdapter(int itemsRes, EditorEventListItemMenuDialog dialog)
    {
        this.dialog = dialog;

        this.items = dialog.getResources().getStringArray(itemsRes);

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(dialog.getContext());
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
    
    private static class ViewHolder {
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
            if (dialog.itemValue == SingleSelectListDialog.NOT_USE_RADIO_BUTTONS)
                vi = inflater.inflate(R.layout.listitem_pp_list_preference_no_rb, parent, false);
            else
                vi = inflater.inflate(R.layout.listitem_pp_list_preference, parent, false);
            holder = new ViewHolder();
            holder.label = vi.findViewById(R.id.pp_list_pref_dlg_item_label);
            if (dialog.itemValue != SingleSelectListDialog.NOT_USE_RADIO_BUTTONS)
                holder.radioButton = vi.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.label.setText(items[position]);

        if (dialog.itemValue != SingleSelectListDialog.NOT_USE_RADIO_BUTTONS) {
            holder.radioButton.setChecked(dialog.itemValue == position);

            holder.radioButton.setTag(position);
            holder.radioButton.setOnClickListener(v -> {
                RadioButton rb = (RadioButton) v;
                int pos = (Integer) rb.getTag();
                dialog.itemValue = pos;
                rb.setChecked(true);
                dialog.itemClick.onClick(dialog.getDialog(), pos);
                dialog.dismiss();
            });
        }

        return vi;
    }

}
