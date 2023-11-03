package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class ContactsFilterDialogAdapter extends BaseAdapter {

    //private final LayoutInflater inflater;
    private final Context context;

    private final ContactsFilterDialog dialog;

    ContactsFilterDialogAdapter(Context context, ContactsFilterDialog dialog)
    {
        this.dialog = dialog;
        this.context = context;

        //inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dialog.contactsFilterList.size();
    }

    @Override
    public Object getItem(int i) {
        return dialog.contactsFilterList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static class ViewHolder {
        TextView filterName;
        //int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.listitem_contacts_filter_dialog, parent, false);

            holder = new ViewHolder();
            holder.filterName = vi.findViewById(R.id.contacts_filter_dialog_item_filter_name);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String filterName = dialog.contactsFilterList.get(position).displayName;

        if (filterName != null)
        {
            holder.filterName.setText(filterName);
        }

        return vi;
    }
}
