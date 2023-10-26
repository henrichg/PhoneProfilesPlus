package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class InfoDialogPreference extends DialogPreference {

    InfoDialogPreferenceFragment fragment;
    private final Context prefContext;

    String infoText;
    boolean isHtml;

    static final String ACTIVITY_IMPORTANT_INFO_PROFILES = "@important_info_profiles";
    static final String PPP_APP_INFO_SCREEN = "@ppp_app_info_screen";
    static final String DROIDIFY_INSTALLATION_SITE = "@droidify_installation_site";
    static final String GRANT_ROOT = "@grant_root";

    public InfoDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        prefContext = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPInfoDialogPreference);

        infoText = typedArray.getString(R.styleable.PPInfoDialogPreference_infoText);
        isHtml = false;

        typedArray.recycle();

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
    protected void onSetInitialValue(Object defaultValue)
    {
    }

    void setInfoText(String _infoText) {
        this.infoText = _infoText;
    }

    void setIsHtml(@SuppressWarnings("SameParameterValue") boolean _isHtml) {
        this.isHtml = _isHtml;
    }

}
