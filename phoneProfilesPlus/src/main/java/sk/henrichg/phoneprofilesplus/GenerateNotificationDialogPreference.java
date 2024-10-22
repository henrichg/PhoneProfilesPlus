package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class GenerateNotificationDialogPreference extends DialogPreference {

    GenerateNotificationDialogPreferenceFragment fragment;

    final Context _context;

    // Custom xml attributes.
    int generate;
    int iconType;
    int replaceWithPPPIcon;
    int showLargeIcon;
    String notificationTitle;
    String notificationBody;

    private String sValue = "";
    private String defaultValue;
    //private boolean savedInstanceState;

    public GenerateNotificationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPGenerateNotificationDialogPreference);

        generate = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnGenerate, 0);
        iconType = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnIconType, 0);
        replaceWithPPPIcon = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnReplaceWithPPPIcon, 0);
        showLargeIcon = typedArray.getInteger(
                R.styleable.PPGenerateNotificationDialogPreference_gnShowLargeIcon, 0);
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

        getValueGNDP();
        setSummaryGNDP();
    }

    private void getValueGNDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
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
        //noinspection UnreachableCode
        try {
            notificationTitle = splits[2];
        } catch (Exception e) {
            notificationTitle = "";
        }
        //noinspection UnreachableCode
        try {
            notificationBody = splits[3];
        } catch (Exception e) {
            notificationBody = "";
        }
        try {
            showLargeIcon = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            showLargeIcon = 0;
        }
        try {
            replaceWithPPPIcon = Integer.parseInt(splits[5]);
        } catch (Exception e) {
            replaceWithPPPIcon = 0;
        }
    }

    private void setSummaryGNDP()
    {
        String prefVolumeDataSummary;
        if (generate == 0)
            prefVolumeDataSummary = _context.getString(R.string.preference_profile_generate_notification_no_generate);
        else {
            prefVolumeDataSummary = _context.getString(R.string.preference_profile_generate_notification_generate) + StringConstants.STR_COLON_WITH_SPACE;
            if (iconType == 0)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_information_icon) + "; ";
            else
            if (iconType == 1)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_exclamation_icon) + "; ";
            else
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_profile_icon) + "; ";

            if (replaceWithPPPIcon == 1)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_replace_with_ppp_icon) + "; ";

            if (showLargeIcon == 1)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.preference_profile_generate_notification_show_large_icon) + "; ";

            prefVolumeDataSummary = prefVolumeDataSummary + "\"" + notificationTitle + "\"; \"" + notificationBody + "\"";
        }
        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        //int _value = value + minimumValue;
        return generate
                + "|" + iconType
                + "|" + notificationTitle
                + "|" + notificationBody
                + "|" + showLargeIcon
                + "|" + replaceWithPPPIcon;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryGNDP();
        }
    }

    /*
    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueGNDP();
            setSummaryGNDP();
        }
        savedInstanceState = false;
    }
    */

    static boolean changeEnabled(String value) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[0]) == 1;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        //savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final GenerateNotificationDialogPreference.SavedState myState = new GenerateNotificationDialogPreference.SavedState(superState);
        myState.sValue = sValue;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueGNDP();
            setSummaryGNDP();
            return;
        }

        // restore instance state
        GenerateNotificationDialogPreference.SavedState myState = (GenerateNotificationDialogPreference.SavedState)state;
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

        public static final Creator<GenerateNotificationDialogPreference.SavedState> CREATOR =
                new Creator<>() {
                    public GenerateNotificationDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new GenerateNotificationDialogPreference.SavedState(in);
                    }
                    public GenerateNotificationDialogPreference.SavedState[] newArray(int size)
                    {
                        return new GenerateNotificationDialogPreference.SavedState[size];
                    }

                };

    }

}
