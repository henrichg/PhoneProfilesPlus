package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationViewHolder {

    public ImageView imageViewIcon;
    public TextView textViewAppName;
    public CheckBox checkBox;

    public ApplicationViewHolder() {
    }

    public ApplicationViewHolder(ImageView imageViewIcon, TextView textViewAppName, CheckBox checkBox)
    {
        this.imageViewIcon = imageViewIcon;
        this.textViewAppName = textViewAppName;
        this.checkBox = checkBox;
    }

}
