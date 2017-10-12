package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

class CalendarViewHolder {

    final LinearLayout calendarColor;
    final TextView textViewDisplayName;
    final CheckBox checkBox;

    //CalendarViewHolder() {
    //}

    CalendarViewHolder(LinearLayout calendarColor, TextView textViewDisplayName, CheckBox checkBox)
    {
        this.calendarColor = calendarColor;
        this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
