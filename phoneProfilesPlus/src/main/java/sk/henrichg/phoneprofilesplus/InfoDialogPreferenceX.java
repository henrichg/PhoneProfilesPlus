package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

@SuppressWarnings("WeakerAccess")
public class InfoDialogPreferenceX extends DialogPreference {

    InfoDialogPreferenceFragmentX fragment;

    String infoText;
    boolean isHtml;

    public InfoDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

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
