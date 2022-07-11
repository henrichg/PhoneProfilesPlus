package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class VPNDialogPreferenceX extends DialogPreference {

    VPNDialogPreferenceFragmentX fragment;

    private final Context _context;

    private String sValue = "0|0||";
    private String defaultValue;
    private boolean savedInstanceState;

    int vpnApplication;
    boolean enableVPN;
    String profileName;
    String tunnelName;

    public VPNDialogPreferenceX(Context context, AttributeSet attrs) {
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
        getValueVDP();
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        //PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "sValue="+sValue);
        String[] splits = sValue.split("\\|");
        try {
            vpnApplication = Integer.parseInt(splits[0]);
            enableVPN = splits[1].equals("0");
            profileName = splits[2];
            tunnelName = splits[3];
        } catch (Exception e) {
            //Log.e("VolumeDialogPreferenceX.getValueVDP", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            vpnApplication = 0;
            enableVPN = true;
            profileName = "";
            tunnelName = "";
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

        if (enableVPN)
            prefVolumeDataSummary = prefVolumeDataSummary + "; " + _context.getString(R.string.vpn_profile_pref_dlg_enable_vpn);
        else
            prefVolumeDataSummary = prefVolumeDataSummary + "; " + _context.getString(R.string.vpn_profile_pref_dlg_disable_vpn);

        if ((vpnApplication == 1) || (vpnApplication == 2))
            prefVolumeDataSummary = prefVolumeDataSummary + "; " + profileName;
        if (vpnApplication == 3)
            prefVolumeDataSummary = prefVolumeDataSummary + "; " + tunnelName;

        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        String sEnableVPN = "0";
        if (!enableVPN)
            sEnableVPN = "1";
        return vpnApplication
                + "|" + sEnableVPN
                + "|" + profileName
                + "|" + tunnelName;
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

        final VPNDialogPreferenceX.SavedState myState = new VPNDialogPreferenceX.SavedState(superState);
        myState.sValue = getSValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(VPNDialogPreferenceX.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            //PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onRestoreInstanceState");
            getValueVDP();
            setSummaryVDP();
            return;
        }

        // restore instance state
        VPNDialogPreferenceX.SavedState myState = (VPNDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        //PPApplication.logE("VolumeDialogPreferenceX.getValueVDP", "form onRestoreInstanceState");
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
                new Creator<VPNDialogPreferenceX.SavedState>() {
                    public VPNDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new VPNDialogPreferenceX.SavedState(in);
                    }
                    public VPNDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new VPNDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
