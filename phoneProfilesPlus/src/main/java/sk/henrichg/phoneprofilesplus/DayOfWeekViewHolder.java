package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.TextView;

public class DayOfWeekViewHolder {

    public TextView textViewDisplayName;
    public CheckBox checkBox;

    public DayOfWeekViewHolder() {
    }

    public DayOfWeekViewHolder(TextView textViewDisplayName, CheckBox checkBox)
    {
    	this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
