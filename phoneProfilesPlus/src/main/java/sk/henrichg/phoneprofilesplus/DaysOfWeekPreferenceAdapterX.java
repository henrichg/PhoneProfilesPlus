package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

class DaysOfWeekPreferenceAdapterX extends BaseAdapter
{
    private final List<DayOfWeek> daysOfWeekList;

    private final LayoutInflater inflater;
    //private Context context;

    DaysOfWeekPreferenceAdapterX(Context context, List<DayOfWeek> daysOfWeekList)
    {
        this.daysOfWeekList = daysOfWeekList;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context;
    }

    public int getCount() {
        return daysOfWeekList.size();
    }

    public Object getItem(int position) {
        return daysOfWeekList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent)
    {
        // day of week to display
        DayOfWeek calendar = daysOfWeekList.get(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        TextView textViewDisplayName;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.days_of_week_preference_list_item, parent, false);

            // Find the child views.
            textViewDisplayName = convertView.findViewById(R.id.days_of_week_pref_dlg_item_display_name);
            checkBox = convertView.findViewById(R.id.days_of_week_pref_dlg_item_checkbox);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new DayOfWeekViewHolder(textViewDisplayName, checkBox));

            // If CheckBox is toggled, update the Contact it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    DayOfWeek dayOfWeek = (DayOfWeek) cb.getTag();
                    dayOfWeek.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            DayOfWeekViewHolder viewHolder = (DayOfWeekViewHolder) convertView.getTag();
            textViewDisplayName = viewHolder.textViewDisplayName;
            checkBox = viewHolder.checkBox;
        }

        // Tag the CheckBox with the Contact it is displaying, so that we
        // can
        // access the Contact in onClick() when the CheckBox is toggled.
        checkBox.setTag(calendar);

        // Display Contact data
        textViewDisplayName.setText(calendar.name);

        checkBox.setChecked(calendar.checked);

        return convertView;

    }

}
