package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class VPNDialogPreference extends DialogPreference {

    VPNDialogPreferenceFragment fragment;

    final Context _context;

    private String sValue = "0|0|||0";
    private String defaultValue;
    private boolean savedInstanceState;

    int vpnApplication;
    boolean enableVPN;
    String profileName;
    String tunnelName;
    boolean doNotSetWhenIsinState;

    public VPNDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueVDP();
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
        try {
            vpnApplication = Integer.parseInt(splits[0]);
            enableVPN = splits[1].equals("0");
            if (splits.length > 2)
                profileName = splits[2];
            else
                profileName = "";
            if (splits.length > 3)
                tunnelName = splits[3];
            else
                tunnelName = "";
            if (splits.length > 4)
                doNotSetWhenIsinState = splits[4].equals("1");
        } catch (Exception e) {
            //Log.e("VPNDialogPreference.getValueVDP", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            vpnApplication = 0;
            enableVPN = true;
            profileName = "";
            tunnelName = "";
            doNotSetWhenIsinState = false;
        }
    }

    private void setSummaryVDP()
    {

        String prefVolumeDataSummary;
        String[] entries = _context.getResources().getStringArray(R.array.vpnApplicationArray);
        String[] entryValues = _context.getResources().getStringArray(R.array.vpnApplicationValues);

        int applicaitonIdx = 0;
        for (String entryValue : entryValues) {
            if (entryValue.equals(String.valueOf(vpnApplication))) {
                break;
            }
            ++applicaitonIdx;
        }

        prefVolumeDataSummary = entries[applicaitonIdx];

        if (vpnApplication > 0) {
            if (enableVPN)
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + _context.getString(R.string.vpn_profile_pref_dlg_enable_vpn);
            else
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + _context.getString(R.string.vpn_profile_pref_dlg_disable_vpn);

            if ((vpnApplication == 1) || (vpnApplication == 2) || (vpnApplication == 3))
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + profileName;
            if (vpnApplication == 4)
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + tunnelName;

            if (doNotSetWhenIsinState)
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + _context.getString(R.string.vpn_not_set_when_is_in_state_pref_dlg);
        }

        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        String sEnableVPN = "0";
        if (!enableVPN)
            sEnableVPN = "1";

        String sDoNotSet = "0";
        if (doNotSetWhenIsinState)
            sDoNotSet = "1";

        return vpnApplication
                + "|" + sEnableVPN
                + "|" + profileName
                + "|" + tunnelName
                + "|" + sDoNotSet;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryVDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueVDP();
            setSummaryVDP();
        }
        savedInstanceState = false;
    }

    static boolean changeEnabled(String value) {
        return !value.startsWith("0");
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final VPNDialogPreference.SavedState myState = new VPNDialogPreference.SavedState(superState);
        myState.sValue = getSValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(VPNDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueVDP();
            setSummaryVDP();
            return;
        }

        // restore instance state
        VPNDialogPreference.SavedState myState = (VPNDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueVDP();
        setSummaryVDP();
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
                new Creator<VPNDialogPreference.SavedState>() {
                    public VPNDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new VPNDialogPreference.SavedState(in);
                    }
                    public VPNDialogPreference.SavedState[] newArray(int size)
                    {
                        return new VPNDialogPreference.SavedState[size];
                    }

                };

    }

}
