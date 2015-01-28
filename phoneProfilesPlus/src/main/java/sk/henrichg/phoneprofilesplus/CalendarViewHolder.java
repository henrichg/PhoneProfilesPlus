package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarViewHolder {
	
	public LinearLayout calendarColor;
    public TextView textViewDisplayName;
    public CheckBox checkBox;

    public CalendarViewHolder() {
    }

    public CalendarViewHolder(LinearLayout calendarColor, TextView textViewDisplayName, CheckBox checkBox) 
    {
        this.calendarColor = calendarColor;
    	this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
