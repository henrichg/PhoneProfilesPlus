package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class InfoDialogPreference extends DialogPreference {

    InfoDialogPreferenceFragment fragment;

    String infoText;
    boolean isHtml;

    static final String ACTIVITY_IMPORTANT_INFO_PROFILES = "@important_info_profiles";
    static final String PPP_APP_INFO_SCREEN = "@ppp_app_info_screen";
    static final String DROIDIFY_INSTALLATION_SITE = "@droidify_installation_site";
    static final String GRANT_ROOT = "@grant_root";

    public InfoDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPInfoDialogPreference);

        infoText = typedArray.getString(R.styleable.PPInfoDialogPreference_infoText);
        isHtml = false;

        typedArray.recycle();

        setNegativeButtonText(null);
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
