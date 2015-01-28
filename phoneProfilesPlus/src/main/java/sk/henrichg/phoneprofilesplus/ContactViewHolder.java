package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactViewHolder {
	
	public ImageView imageViewPhoto;
    public TextView textViewDisplayName;
    public TextView textViewPhoneNumber;
    public CheckBox checkBox;

    public ContactViewHolder() {
    }

    public ContactViewHolder(ImageView imageViewPhoto, TextView textViewDisplayName, TextView textViewPhoneNumber, CheckBox checkBox) 
    {
        this.imageViewPhoto = imageViewPhoto;
    	this.textViewDisplayName = textViewDisplayName;
        this.textViewPhoneNumber = textViewPhoneNumber;
        this.checkBox = checkBox;
    }

}
