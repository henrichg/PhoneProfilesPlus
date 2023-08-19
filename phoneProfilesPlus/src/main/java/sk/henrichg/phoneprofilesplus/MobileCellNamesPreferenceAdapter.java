package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

class MobileCellNamesPreferenceAdapter extends BaseAdapter
{
    private final MobileCellNamesPreference preference;
    //private RadioButton selectedRB;
    //int selectedRBIndex = -1;

    private final LayoutInflater inflater;
    //private final Context context;

    MobileCellNamesPreferenceAdapter(Context context, MobileCellNamesPreference preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context;
    }

    public int getCount() {
        return preference.cellNamesList.size();
    }

    public Object getItem(int position) {
        return preference.cellNamesList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("SetTextI18n")
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // cell to display

        //java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification.
        // Make sure the content of your adapter is not modified from a background thread, but only from the UI thread.
        // Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(2131689809, class android.widget.ListView)
        // with Adapter(class sk.henrichg.phoneprofilesplus.MobileCellsPreferenceAdapter)]

        MobileCellNamesPreferenceViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.listitem_mobile_cell_names_preference, parent, false);
            holder = new MobileCellNamesPreferenceViewHolder();
            holder.cellName = vi.findViewById(R.id.mobile_cell_names_list_pref_dlg_item_label);
            //holder.lastConnectedTime = vi.findViewById(R.id.mobile_cells_pref_dlg_item_lastConnectedTime);
            holder.checkBox = vi.findViewById(R.id.mobile_cell_names_list_pref_dlg_item_checkbox);
            vi.setTag(holder);
        } else {
            holder = (MobileCellNamesPreferenceViewHolder) vi.getTag();
        }

        // must be set, without this not working long click
        holder.checkBox.setFocusable(false);
        holder.checkBox.setFocusableInTouchMode(false);

        if (preference.cellNamesList.size() == 0)
            return vi;


        String cellName = preference.cellNamesList.get(position);
        /*if (!cellData.name.isEmpty())
            cellName = cellData.name + StringConstants.CHAR_NEW_LINE;
        String cellFlags = "";
        if (cellData._new)
            cellFlags = cellFlags + "N";
        if (cellData.connected)
            cellFlags = cellFlags + "C";
        if (!cellFlags.isEmpty())
            cellName = cellName + "(" + cellFlags + ") ";
        cellName = cellName + cellData.cellId;*/
        holder.cellName.setText(cellName);

        /*
        if (cellData.lastConnectedTime != 0) {
            //Calendar calendar = Calendar.getInstance().setTimeInMillis(cellData.lastConnectedTime);
            holder.lastConnectedTime.setText(context.getString(R.string.mobile_cells_pref_dlg_last_connected) + " " +
                    StringFormatUtils.timeDateStringFromTimestamp(context, cellData.lastConnectedTime));
        }
        else {
            holder.lastConnectedTime.setText("");
        }
        */

        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(preference.isCellSelected(cellName));
        holder.checkBox.setOnClickListener(v -> {
            CheckBox chb = (CheckBox) v;

            int cellPosition = (Integer)chb.getTag();
            if (cellPosition < preference.cellNamesList.size()) {
                String _cellName = preference.cellNamesList.get(cellPosition);

                if (chb.isChecked())
                    preference.addCellName(_cellName);
                else
                    preference.removeCellName(_cellName);
                preference.refreshListView(/*false*/);
            }
        });

        /*
        boolean found = false;
        String[] splits = preference.persistedValue.split("\\|");
        for (String cell : splits) {
            if (cell.equals(Integer.toString(preference.cellsList.get(position).cellId))) {
                found = true;
                break;
            }
        }
        */

        return vi;
    }

}
