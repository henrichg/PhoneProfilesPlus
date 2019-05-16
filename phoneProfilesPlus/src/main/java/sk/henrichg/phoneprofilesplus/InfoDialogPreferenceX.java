package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

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
