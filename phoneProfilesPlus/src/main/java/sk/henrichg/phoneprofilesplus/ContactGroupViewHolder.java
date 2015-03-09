package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.TextView;

public class ContactGroupViewHolder {

    public TextView textViewDisplayName;
    public CheckBox checkBox;

    public ContactGroupViewHolder() {
    }

    public ContactGroupViewHolder(TextView textViewDisplayName, CheckBox checkBox)
    {
    	this.textViewDisplayName = textViewDisplayName;
        this.checkBox = checkBox;
    }

}
