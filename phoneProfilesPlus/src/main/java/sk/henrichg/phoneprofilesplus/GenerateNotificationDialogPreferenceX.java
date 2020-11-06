package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class GenerateNotificationDialogPreferenceX extends DialogPreference {

    GenerateNotificationDialogPreferenceFragmentX fragment;

    private final Context _context;

    // Custom xml attributes.
    int generate;
    int iconType;
    String notificationTitle;
    String notificationBody;

    private String sValue = "";
    private String defaultValue;
    private boolean savedInstanceState;

    int value = 0;

    public GenerateNotificationDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPGenerateNotificationDialogPreference);

        generate = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnGenerate, 0);
        iconType = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnIconType, 0);
        notificationTitle = typedArray.getString(
                R.styleable.PPGenerateNotificationDialogPreference_gnNotificationTitle);
        notificationBody = typedArray.getString(
                R.styleable.PPGenerateNotificationDialogPreference_gnNotificationBody);

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        //PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onSetInitialValue");
        getValueGNDP();
        setSummaryGNDP();
    }

    private void getValueGNDP()
    {
        String[] splits = sValue.split("\\|");
        try {
            generate = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            generate = 0;
        }
        try {
            iconType = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            iconType = 0;
        }
        try {
            notificationTitle = splits[2];
        } catch (Exception e) {
            notificationTitle = "x";
        }
        try {
            notificationBody = splits[3];
        } catch (Exception e) {
            notificationBody = "";
        }
    }

    private void setSummaryGNDP()
    {
        String prefVolumeDataSummary;
        if (generate == 0)
            prefVolumeDataSummary = _context.getString(R.string.preference_profile_generate_notification_no_generate);
        else {
            prefVolumeDataSummary = _context.getString(R.string.preference_profile_generate_notification_generate) + ": ";
            if (iconType == 0)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_information_icon) + "; ";
            else
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_profile_icon) + "; ";

            prefVolumeDataSummary = prefVolumeDataSummary + notificationTitle + ", " + notificationBody;
        }
        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        //int _value = value + minimumValue;
        return value
                + "|" + generate
                + "|" + iconType
                + "|" + notificationTitle
                + "|" + notificationBody;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryGNDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueGNDP();
            setSummaryGNDP();
        }
        savedInstanceState = false;
    }

    /*
    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }
    */

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final GenerateNotificationDialogPreferenceX.SavedState myState = new GenerateNotificationDialogPreferenceX.SavedState(superState);
        myState.sValue = sValue;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueGNDP();
            setSummaryGNDP();
            return;
        }

        // restore instance state
        GenerateNotificationDialogPreferenceX.SavedState myState = (GenerateNotificationDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueGNDP();
        setSummaryGNDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String sValue;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            sValue = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(sValue);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<GenerateNotificationDialogPreferenceX.SavedState>() {
                    public GenerateNotificationDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new GenerateNotificationDialogPreferenceX.SavedState(in);
                    }
                    public GenerateNotificationDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new GenerateNotificationDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
