package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class MobileCellNamesDialogAdapterX extends BaseAdapter {

    private final LayoutInflater inflater;

    private final MobileCellNamesDialogX dialog;

    MobileCellNamesDialogAdapterX(Context context, MobileCellNamesDialogX dialog)
    {
        this.dialog = dialog;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dialog.cellNamesList.size();
    }

    @Override
    public Object getItem(int i) {
        return dialog.cellNamesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolder {
        TextView cellName;
        //int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.mobile_cell_names_dialog_list_item, parent, false);

            holder = new ViewHolder();
            holder.cellName = vi.findViewById(R.id.mobile_cell_names_dialog_item_cell_name);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String cellName = dialog.cellNamesList.get(position);

        if (cellName != null)
        {
            holder.cellName.setText(cellName);
        }

        return vi;
    }
}
