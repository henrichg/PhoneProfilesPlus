package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class DefaultSIMDialogPreferenceX extends DialogPreference {

    DefaultSIMDialogPreferenceFragmentX fragment;

    private final Context _context;

    private String sValue = "";
    private String defaultValue;
    private boolean savedInstanceState;

    int voiceValue = 0;
    int smsValue = 0;
    int dataValue = 0;

    public DefaultSIMDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        //PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onSetInitialValue");
        getValueDSDP();
        setSummaryDSDP();
    }

    private void getValueDSDP()
    {
        String[] splits = sValue.split("\\|");
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

        prefVolumeDataSummary = _context.getString(R.string.default_sim_subscription_voice) + ": ";
        String[] arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMVoiceArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[voiceValue];
        } catch (Exception ignored) {}

        prefVolumeDataSummary = prefVolumeDataSummary + "; ";

        prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.default_sim_subscription_sms) + ": ";
        arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMSMSArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[smsValue];
        } catch (Exception ignored) {}

        prefVolumeDataSummary = prefVolumeDataSummary + "; ";

        prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.default_sim_subscription_data) + ": ";
        arrayStrings = _context.getResources().getStringArray(R.array.defaultSIMDataArray);
        try {
            prefVolumeDataSummary = prefVolumeDataSummary + arrayStrings[dataValue];
        } catch (Exception ignored) {}

        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        //int _value = value + minimumValue;
        PPApplication.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceX.getSValue", "voiceValue="+voiceValue);
        PPApplication.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceX.getSValue", "smsValue="+smsValue);
        PPApplication.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceX.getSValue", "dataValue="+dataValue);
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

        final DefaultSIMDialogPreferenceX.SavedState myState = new DefaultSIMDialogPreferenceX.SavedState(superState);
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
            getValueDSDP();
            setSummaryDSDP();
            return;
        }

        // restore instance state
        DefaultSIMDialogPreferenceX.SavedState myState = (DefaultSIMDialogPreferenceX.SavedState)state;
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

        public static final Creator<SavedState> CREATOR =
                new Creator<DefaultSIMDialogPreferenceX.SavedState>() {
                    public DefaultSIMDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new DefaultSIMDialogPreferenceX.SavedState(in);
                    }
                    public DefaultSIMDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new DefaultSIMDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
