package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class NotificationVolume0DialogPreference extends DialogPreference {

    NotificationVolume0DialogPreferenceFragment fragment;

    final Context prefContext;

    public NotificationVolume0DialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        prefContext = context;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
    }

}
