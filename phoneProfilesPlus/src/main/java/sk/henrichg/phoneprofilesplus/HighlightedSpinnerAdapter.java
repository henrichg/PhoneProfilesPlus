package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

class HighlightedSpinnerAdapter extends ArrayAdapter<String> {

    private int mSelectedIndex = -1;
    private final Activity activity;

    HighlightedSpinnerAdapter(Activity activity, int textViewResourceId, String[] objects) {
        super(activity, textViewResourceId, objects);
        this.activity = activity;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent){
        View itemView =  super.getDropDownView(position, convertView, parent);

        TextView itemText = itemView.findViewById(android.R.id.text1);
        if (itemText != null) {
            if (position == mSelectedIndex) {
                //itemText.setTextColor(GlobalGUIRoutines.getThemeAccentColor(activity));
                itemText.setTextColor(ContextCompat.getColor(activity, R.color.accent_color));
            } else {
                //itemText.setTextColor(GlobalGUIRoutines.getThemeEditorSpinnerDropDownTextColor(activity));
                itemText.setTextColor(ContextCompat.getColor(activity, R.color.activitySecondaryTextColor));
            }
        }

        return itemView;
    }

    void setSelection(int position) {
        mSelectedIndex =  position;
        notifyDataSetChanged();
    }

}
