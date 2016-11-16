package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.TextView;

class ContactGroupViewHolder {

    TextView textViewDisplayName;
    CheckBox checkBox;

    //ContactGroupViewHolder() {
    //}

    ContactGroupViewHolder(TextView textViewDisplayName, CheckBox checkBox)
    {
        this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
