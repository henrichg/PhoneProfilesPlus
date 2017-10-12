package sk.henrichg.phoneprofilesplus;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class ApplicationViewHolder {

    ImageView imageViewIcon;
    TextView textViewAppName;
    CheckBox checkBox;
    TextView textViewAppType;

    //public ApplicationViewHolder() {
    //}

    ApplicationViewHolder(ImageView imageViewIcon, TextView textViewAppName,
                                 TextView textViewAppType, CheckBox checkBox)
    {
        this.imageViewIcon = imageViewIcon;
        this.textViewAppName = textViewAppName;
        this.checkBox = checkBox;
        this.textViewAppType = textViewAppType;
    }

}
