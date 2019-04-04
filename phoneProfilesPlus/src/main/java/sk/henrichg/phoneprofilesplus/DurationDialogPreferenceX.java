package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class DurationDialogPreferenceX extends DialogPreference {

    DurationDialogPreferenceFragmentX fragment;

    String value;

    final int mMin, mMax;

    public DurationDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
        setSummaryDDP();
    }

    void setSummaryDDP() {
        setSummary(GlobalGUIRoutines.getDurationString(Integer.parseInt(value)));
    }

    void persistStringValue(String value)
    {
        persistString(value);
    }
}