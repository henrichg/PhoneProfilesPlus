package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class SearchStringPreferenceX extends DialogPreference {

    SearchStringPreferenceFragmentX fragment;

    String value = "";

    public SearchStringPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String)defaultValue);
        setSummary(value);
    }

    void persistValue(String _value) {
        if (shouldPersist()) {
            value = _value;
            persistString(value);
            setSummary(value);
        }
    }

}
