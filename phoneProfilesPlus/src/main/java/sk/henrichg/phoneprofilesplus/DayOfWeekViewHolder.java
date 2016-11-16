package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.TextView;

class DayOfWeekViewHolder {

    TextView textViewDisplayName;
    CheckBox checkBox;

    //public DayOfWeekViewHolder() {
    //}

    DayOfWeekViewHolder(TextView textViewDisplayName, CheckBox checkBox)
    {
        this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
