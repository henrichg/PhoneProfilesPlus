package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class PPPPSDialogPreference extends DialogPreference {

    PPPPSDialogPreferenceFragment fragment;

    final Context _context;

    // Custom xml attributes.
    //String forPreference;

    public PPPPSDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        /*
        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPppppsDialogPreference);

        forPreference = typedArray.getString(
                R.styleable.PPppppsDialogPreference_forPreference);

        typedArray.recycle();
        */
    }

    @Override
    public void onAttached() {
        super.onAttached();
        setSummaryPPPPSDP();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        setSummaryPPPPSDP();
    }

    private void setSummaryPPPPSDP()
    {
        String prefVolumeDataSummary;

        int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(_context);
        if (ppppsVersion == 0) {
            prefVolumeDataSummary = "<b>" + _context.getString(R.string.pppps_pref_dialog_PPPPutSettings_not_installed_summary) + "</b>";
            prefVolumeDataSummary = prefVolumeDataSummary + "<br>" + _context.getString(R.string.pppps_pref_dialog_PPPPutSettings_install_summary);
        }
        else {
            String ppppsVersionName = ActivateProfileHelper.getPPPPutSettingsVersionName(_context);
            prefVolumeDataSummary =  _context.getString(R.string.pppps_pref_dialog_install_pppps_installed_version) +
                    " <b>" + ppppsVersionName + " (" + ppppsVersion + ")</b><br>";
            prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.pppps_pref_dialog_install_pppps_latest_version) +
                    " <b>" + PPApplication.VERSION_NAME_PPPPS_LATEST + " (" + PPApplication.VERSION_CODE_PPPPS_LATEST + ")</b>";
            if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_LATEST)
                prefVolumeDataSummary = prefVolumeDataSummary + "<br><br><b>" + _context.getString(R.string.pppps_pref_dialog_PPPPutSettings_new_version_summary) + "</b>";
            else
                prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" + _context.getString(R.string.pppps_pref_dialog_PPPPutSettings_upgrade_summary);
        }

        prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" +
                "<b>" + _context.getString(R.string.pppps_pref_dialog_PPPPutSettings_modify_system_settings) + "</b>";

//        Log.e("PPPPSDialogPreference.setSummaryPPPPSDP", "xxxxx");
        setSummary(StringFormatUtils.fromHtml(prefVolumeDataSummary, false, false, false, 0, 0, true));
    }

    /*
    String getSValue() {
        //int _value = value + minimumValue;
        return "";
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryPPPPSDP();
        }
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
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
    */

    @Override
    protected Parcelable onSaveInstanceState()
    {
        //savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        //noinspection UnnecessaryLocalVariable
        final PPPPSDialogPreference.SavedState myState = new PPPPSDialogPreference.SavedState(superState);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryPPPPSDP();
            return;
        }

        // restore instance state
        PPPPSDialogPreference.SavedState myState = (PPPPSDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());

        setSummaryPPPPSDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        SavedState(Parcel source)
        {
            super(source);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<PPPPSDialogPreference.SavedState>() {
                    public PPPPSDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new PPPPSDialogPreference.SavedState(in);
                    }
                    public PPPPSDialogPreference.SavedState[] newArray(int size)
                    {
                        return new PPPPSDialogPreference.SavedState[size];
                    }

                };

    }

}
