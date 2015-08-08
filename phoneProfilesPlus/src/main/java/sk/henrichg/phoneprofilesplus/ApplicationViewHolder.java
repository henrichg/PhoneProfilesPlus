package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationViewHolder {

    public ImageView imageViewPhoto;
    public TextView textViewAppName;
    public CheckBox checkBox;

    public ApplicationViewHolder() {
    }

    public ApplicationViewHolder(ImageView imageViewPhoto, TextView textViewAppName, CheckBox checkBox)
    {
        this.imageViewPhoto = imageViewPhoto;
        this.textViewAppName = textViewAppName;
        this.checkBox = checkBox;
    }

}
