package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarsMultiselectPreferenceAdapter extends BaseAdapter 
{
	List<CalendarEvent> calendarList = null;	
	
    private LayoutInflater inflater;
    //private Context context;

    public CalendarsMultiselectPreferenceAdapter(Context context, List<CalendarEvent> calendarList) 
    {
    	this.calendarList = calendarList;
    	
    	// Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

	public int getCount() {
		return calendarList.size();
	}

	public Object getItem(int position) {
		return calendarList.get(position); 
	}

	public long getItemId(int position) {
		return position;
	}
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Calendar to display
        CalendarEvent calendar = calendarList.get(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        LinearLayout calendarColor;
        TextView textViewDisplayName;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.calendars_multiselect_preference_list_item, parent, false);

            // Find the child views.
            calendarColor = (LinearLayout) convertView.findViewById(R.id.calendars_multiselect_pref_dlg_item_color);
            textViewDisplayName = (TextView) convertView.findViewById(R.id.calendars_multiselect_pref_dlg_item_display_name);
            checkBox = (CheckBox) convertView.findViewById(R.id.calendars_multiselect_pref_dlg_item_checkbox);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new CalendarViewHolder(calendarColor, textViewDisplayName, checkBox));

            // If CheckBox is toggled, update the Contact it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    CalendarEvent calendar = (CalendarEvent) cb.getTag();
                    calendar.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            CalendarViewHolder viewHolder = (CalendarViewHolder) convertView.getTag();
            calendarColor = viewHolder.calendarColor;
            textViewDisplayName = viewHolder.textViewDisplayName;
            checkBox = viewHolder.checkBox;
        }

        // Tag the CheckBox with the Contact it is displaying, so that we
        // can
        // access the Contact in onClick() when the CheckBox is toggled.
        checkBox.setTag(calendar);

        // Display Contact data
        //Log.e("CalendarsMultiselectPreferenceAdapter.getView","color="+calendar.color);
        calendarColor.setBackgroundColor(0xff000000 + calendar.color);
        textViewDisplayName.setText(calendar.name);
        
        checkBox.setChecked(calendar.checked);

        return convertView;
    }

}
