package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

class BetterNumberPickerPreferenceX extends DialogPreference {

    BetterNumberPickerPreferenceFragmentX fragment;

    String value;

    final int mMin, mMax;

    public BetterNumberPickerPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.BetterNumberPickerPreference, 0, 0);

        mMax = numberPickerType.getInt(R.styleable.BetterNumberPickerPreference_max, 5);
        mMin = numberPickerType.getInt(R.styleable.BetterNumberPickerPreference_min, 0);

        numberPickerType.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
        setSummary(value);
    }

    void persistValue() {
        if (callChangeListener(value))
        {
            persistString(value);
        }
    }
}
