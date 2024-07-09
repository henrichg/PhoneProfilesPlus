package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.widget.TooltipCompat;

class MobileCellsEditorPreferenceAdapter extends BaseAdapter
{
    private final MobileCellsEditorPreference preference;
    //private RadioButton selectedRB;
    //int selectedRBIndex = -1;

    private final LayoutInflater inflater;
    private final Context context;

    MobileCellsEditorPreferenceAdapter(Context context, MobileCellsEditorPreference preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return preference.filteredCellsList.size();
    }

    public Object getItem(int position) {
        return preference.filteredCellsList.get(position);
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
        // with Adapter(class sk.henrichg.phoneprofilesplus.MobileCellsEditorPreferenceAdapter)]

        MobileCellsEditorViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.listitem_mobile_cells_preference, parent, false);
            holder = new MobileCellsEditorViewHolder();
            holder.cellId = vi.findViewById(R.id.mobile_cells_pref_dlg_item_label);
            holder.lastConnectedTime = vi.findViewById(R.id.mobile_cells_pref_dlg_item_lastConnectedTime);
            holder.checkBox = vi.findViewById(R.id.mobile_cells_pref_dlg_item_checkbox);
            holder.itemEditMenu = vi.findViewById(R.id.mobile_cells_pref_dlg_item_edit_menu);
            vi.setTag(holder);
        } else {
            holder = (MobileCellsEditorViewHolder) vi.getTag();
        }

        // must be set, without this not working long click
        holder.checkBox.setFocusable(false);
        holder.checkBox.setFocusableInTouchMode(false);
        holder.itemEditMenu.setFocusable(false);
        holder.itemEditMenu.setFocusableInTouchMode(false);

        if (preference.filteredCellsList.isEmpty())
            return vi;

        MobileCellsData cellData = preference.filteredCellsList.get(position);

        //noinspection ExtractMethodRecommender
        String cellName = "";
        if (!cellData.name.isEmpty())
            cellName = cellData.name + StringConstants.CHAR_NEW_LINE;
        String cellFlags = "";
        if (cellData._new)
            cellFlags = cellFlags + "N";
        if (cellData.connected)
            cellFlags = cellFlags + "C";
        if (!cellFlags.isEmpty())
            cellName = cellName + "(" + cellFlags + ") ";
        if (cellData.cellId != Integer.MAX_VALUE)
            cellName = cellName + cellData.cellId;
        else
            cellName = cellName + cellData.cellIdLong;
        holder.cellId.setText(cellName);

        if (cellData.lastConnectedTime != 0) {
            //Calendar calendar = Calendar.getInstance().setTimeInMillis(cellData.lastConnectedTime);
            holder.lastConnectedTime.setText(context.getString(R.string.mobile_cells_pref_dlg_last_connected) + " " +
                    StringFormatUtils.timeDateStringFromTimestamp(context, cellData.lastConnectedTime));
        }
        else {
            holder.lastConnectedTime.setText("");
        }

        holder.checkBox.setTag(position);
        //if (cellData.cellId != Integer.MAX_VALUE)
        //    holder.checkBox.setChecked(preference.isCellSelected(cellData.cellId, Long.MAX_VALUE));
        //else if (cellData.cellIdLong != Long.MAX_VALUE)
        // holder.checkBox.setChecked(preference.isCellSelected(Integer.MAX_VALUE, cellData.cellIdLong));
        holder.checkBox.setChecked(preference.isCellSelected(cellData.cellId, cellData.cellIdLong));
        holder.checkBox.setOnClickListener(v -> {
            CheckBox chb = (CheckBox) v;

            int cellPosition = (Integer)chb.getTag();
            if (cellPosition < preference.filteredCellsList.size()) {
                int cellId = preference.filteredCellsList.get(cellPosition).cellId;
                long cellIdLong = preference.filteredCellsList.get(cellPosition).cellIdLong;

                if (chb.isChecked())
                    preference.addCellId(cellId, cellIdLong);
                else
                    preference.removeCellId(cellId, cellIdLong);
                preference.refreshListView(false, false/*, Integer.MAX_VALUE*/);
            }
        });

        /*
        boolean found = false;
        String[] splits = preference.persistedValue.split(StringConstants.STR_SPLIT_REGEX);
        for (String cell : splits) {
            if (cell.equals(Integer.toString(preference.cellsList.get(position).cellId))) {
                found = true;
                break;
            }
        }
        */
        //if (preference.filteredCellsList.get(position).connected/* || found*/)
        //    holder.itemEditMenu.setVisibility(View.GONE);
        //else
        //    holder.itemEditMenu.setVisibility(View.VISIBLE);
        TooltipCompat.setTooltipText(holder.itemEditMenu, context.getString(R.string.tooltip_options_menu));
        holder.itemEditMenu.setTag(R.id.editMenuTagCell, preference.filteredCellsList.get(position).cellId);
        holder.itemEditMenu.setTag(R.id.editMenuTagCellLong, preference.filteredCellsList.get(position).cellIdLong);
        final ImageView itemEditMenu = holder.itemEditMenu;
        holder.itemEditMenu.setOnClickListener(v -> preference.showEditMenu(itemEditMenu));

        return vi;
    }

}
