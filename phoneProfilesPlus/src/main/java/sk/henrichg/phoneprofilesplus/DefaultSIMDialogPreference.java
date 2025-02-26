package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class DefaultSIMDialogPreference extends DialogPreference {

    DefaultSIMDialogPreferenceFragment fragment;

    private final Context _context;

    private String sValue = "";
    private String defaultValue;
    private boolean savedInstanceState;

    int voiceValue = 0;
    int smsValue = 0;
    int dataValue = 0;

    public DefaultSIMDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        getValueDSDP();
        setSummaryDSDP();
    }

    private void getValueDSDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
        try {
            voiceValue = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            voiceValue = 0;
        }
        try {
            smsValue = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            smsValue = 0;
        }
        try {
            dataValue = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            dataValue = 0;
        }
    }

    private void setSummaryDSDP()
    {
        String prefVolumeDataSummary;

        prefVolumeDataSummary = _context.getString(R.string.default_sim_subscription_voice) + StringConstants.STR_COLON_WITH_SPACE;
        String[] arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMVoiceArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[voiceValue];
        } catch (Exception ignored) {
        }

        prefVolumeDataSummary = prefVolumeDataSummary + "; ";

        prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.default_sim_subscription_sms) + StringConstants.STR_COLON_WITH_SPACE;
        arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMSMSArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[smsValue];
        } catch (Exception ignored) {
        }

        prefVolumeDataSummary = prefVolumeDataSummary + "; ";

        prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.default_sim_subscription_data) + StringConstants.STR_COLON_WITH_SPACE;
        arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMDataArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[dataValue];
        } catch (Exception ignored) {
        }

        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        //int _value = value + minimumValue;
//        PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreference.getSValue", "voiceValue="+voiceValue);
//        PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreference.getSValue", "smsValue="+smsValue);
//        PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreference.getSValue", "dataValue="+dataValue);
        return voiceValue + "|" + smsValue + "|" + dataValue;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryDSDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueDSDP();
            setSummaryDSDP();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final DefaultSIMDialogPreference.SavedState myState = new DefaultSIMDialogPreference.SavedState(superState);
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
            getValueDSDP();
            setSummaryDSDP();
            return;
        }

        // restore instance state
        DefaultSIMDialogPreference.SavedState myState = (DefaultSIMDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueDSDP();
        setSummaryDSDP();
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

        public static final Creator<DefaultSIMDialogPreference.SavedState> CREATOR =
                new Creator<>() {
                    public DefaultSIMDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new DefaultSIMDialogPreference.SavedState(in);
                    }
                    public DefaultSIMDialogPreference.SavedState[] newArray(int size)
                    {
                        return new DefaultSIMDialogPreference.SavedState[size];
                    }

                };

    }

}
