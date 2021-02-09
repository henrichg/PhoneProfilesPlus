package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.TextView;

class ContactGroupViewHolder {

    final TextView textViewDisplayName;
    final CheckBox checkBox;
    final TextView textViewAccountType;

    //ContactGroupViewHolder() {
    //}

    ContactGroupViewHolder(TextView textViewDisplayName, CheckBox checkBox, TextView textViewAccountType)
    {
        this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
        this.textViewAccountType = textViewAccountType;
    }

}
