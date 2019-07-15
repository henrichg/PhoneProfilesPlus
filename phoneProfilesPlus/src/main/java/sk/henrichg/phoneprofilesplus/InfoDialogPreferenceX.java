package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

@SuppressWarnings("WeakerAccess")
public class InfoDialogPreferenceX extends DialogPreference {

    InfoDialogPreferenceFragmentX fragment;

    String infoText;

    public InfoDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.InfoDialogPreference);

        infoText = typedArray.getString(R.styleable.InfoDialogPreference_infoText);

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

}
