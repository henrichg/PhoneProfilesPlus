package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class SearchStringPreference extends DialogPreference {

    SearchStringPreferenceFragment fragment;

    String value = "";

    public SearchStringPreference(Context context, AttributeSet attrs) {
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
