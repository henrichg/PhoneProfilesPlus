package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class ContactViewHolder {

    ImageView imageViewPhoto;
    TextView textViewDisplayName;
    TextView textViewPhoneNumber;
    CheckBox checkBox;

    //ContactViewHolder() {
    //}

    ContactViewHolder(ImageView imageViewPhoto, TextView textViewDisplayName, TextView textViewPhoneNumber, CheckBox checkBox)
    {
        this.imageViewPhoto = imageViewPhoto;
        this.textViewDisplayName = textViewDisplayName;
        this.textViewPhoneNumber = textViewPhoneNumber;
        this.checkBox = checkBox;
    }

}
