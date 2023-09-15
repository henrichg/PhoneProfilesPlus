package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class InformationPreference extends Preference {

    private final Context prefContext;

    public InformationPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        prefContext = context;

        setWidgetLayoutResource(R.layout.preference_widget_info_preference);
    }

    //@Override
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        ImageView imageView = (ImageView) holder.findViewById(R.id.info_preference_imageView1);

        Drawable drawable = AppCompatResources.getDrawable(prefContext, R.drawable.ic_info_preference_icon);
        //noinspection DataFlowIssue
        drawable.mutate();

        int disabledColor = ContextCompat.getColor(prefContext, R.color.activityDisabledTextColor);
        String applicationTheme = ApplicationPreferences.applicationTheme(prefContext, true);
        boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);
        //if (GlobalGUIRoutines.isNightModeEnabled(prefContext.getApplicationContext()))
        if (nightModeOn) {
            if (!isEnabled()) {
                disabledColor = ColorUtils.blendARGB(disabledColor, Color.WHITE, 0.1f);
                //noinspection DataFlowIssue
                drawable.setTint(disabledColor);
            }
        }
        else {
            if (!isEnabled()) {
                disabledColor = ColorUtils.blendARGB(disabledColor, Color.BLACK, 0.1f);
                //noinspection DataFlowIssue
                drawable.setTint(disabledColor);
            }
        }

        imageView.setImageDrawable(drawable); // icon resource
    }

}
