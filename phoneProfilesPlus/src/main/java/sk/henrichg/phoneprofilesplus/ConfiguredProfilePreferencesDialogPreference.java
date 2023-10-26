package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ConfiguredProfilePreferencesDialogPreference extends DialogPreference {

    ConfiguredProfilePreferencesDialogPreferenceFragment fragment;
    private final Context prefContext;

    long profile_id;
    List<ConfiguredProfilePreferencesData> preferencesList;

    public ConfiguredProfilePreferencesDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        prefContext = context;

        preferencesList = new ArrayList<>();

        setNegativeButtonText(null);

        setWidgetLayoutResource(R.layout.preference_widget_info_preference_clickable);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        ImageView imageView = (ImageView) holder.findViewById(R.id.info_preference_clickable_imageView1);

        imageView.setImageResource(R.drawable.ic_info_preference_icon_clickable);
        if (!isEnabled()) {
            int disabledColor = ContextCompat.getColor(prefContext, R.color.activityDisabledTextColor);
            imageView.setColorFilter(disabledColor, android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
            imageView.setColorFilter(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    /*
    void refreshListView() {
        if (fragment != null)
            fragment.refreshListView();
    }
    */

}
