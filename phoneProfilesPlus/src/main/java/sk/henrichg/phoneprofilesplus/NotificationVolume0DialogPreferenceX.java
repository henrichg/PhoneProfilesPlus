package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

@SuppressWarnings("WeakerAccess")
public class NotificationVolume0DialogPreferenceX extends DialogPreference {

    NotificationVolume0DialogPreferenceFragmentX fragment;

    final Context _context;

    public NotificationVolume0DialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
    }

}
