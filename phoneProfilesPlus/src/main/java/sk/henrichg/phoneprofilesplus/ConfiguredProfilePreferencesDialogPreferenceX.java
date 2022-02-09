package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class ConfiguredProfilePreferencesDialogPreferenceX extends DialogPreference {

    ConfiguredProfilePreferencesDialogPreferenceFragmentX fragment;

    long profile_id;
    List<ConfiguredProfilePreferencesData> preferencesList;

    public ConfiguredProfilePreferencesDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferencesList = new ArrayList<>();

        setNegativeButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    /*
    void refreshListView() {
        if (fragment != null)
            fragment.refreshListView();
    }
    */

}
