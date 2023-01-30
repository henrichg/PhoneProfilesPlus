package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class ExtenderDialogPreference extends DialogPreference {

    ExtenderDialogPreferenceFragment fragment;

    final Context _context;

    // Custom xml attributes.
    final String installSummary;
    final String lauchSummary;
    final String enableExtenderSummaryDisabled;
    final String enbaleExtenderPreferenceNameToTest;
    final String enbaleExtenderPreferenceValueToTest;

    public ExtenderDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPExtenderDialogPreference);

        installSummary = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_installSummary);
        lauchSummary = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_launchSummary);
        enableExtenderSummaryDisabled = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_enableExtenderSummaryDisabled);
        enbaleExtenderPreferenceNameToTest = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_enbaleExtenderPreferenceNameToTest);
        enbaleExtenderPreferenceValueToTest = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_enbaleExtenderPreferenceValueToTest);

        typedArray.recycle();
    }

    @Override
    public void onAttached() {
        super.onAttached();
        setSummaryEDP();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        setSummaryEDP();
    }

    void setSummaryEDP()
    {
        String prefVolumeDataSummary;

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(_context);
        if (extenderVersion == 0) {
            prefVolumeDataSummary = "<b>" + _context.getString(R.string.pppextender_pref_dialog_PPPExtender_not_installed_summary) + "</b>";

            if ((installSummary != null) && (!installSummary.isEmpty()))
                prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" + installSummary;
        }
        else {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(_context);
            prefVolumeDataSummary =  _context.getString(R.string.install_extender_installed_version) +
                    " <b>" + extenderVersionName + " (" + extenderVersion + ")</b><br>";
            prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.install_extender_required_version) +
                    " <b>" + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")</b>";
            if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                prefVolumeDataSummary = prefVolumeDataSummary + "<br><br><b>" + _context.getString(R.string.pppextender_pref_dialog_PPPExtender_new_version_summary) + "</b>";
            else
                prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" + _context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
        }

        int accessibilityEnabled;// = -99;
        if (extenderVersion == 0)
            // not installed
            accessibilityEnabled = -2;
        else
        if ((extenderVersion > 0) &&
                (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST))
            // old version
            accessibilityEnabled = -1;
        else
            accessibilityEnabled = -98;
        if (accessibilityEnabled == -98) {
            // Extender is in right version
            if (PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(_context, false, true))
                // accessibility enabled
                accessibilityEnabled = 1;
            else
                // accessibility disabled
                accessibilityEnabled = 0;
        }
        //if (accessibilityEnabled == -99)
        //    accessibilityEnabled = 1;
        boolean _accessibilityEnabled = accessibilityEnabled == 1;
        boolean preferenceValueOK = true;
        if ((enbaleExtenderPreferenceNameToTest != null) && (!enbaleExtenderPreferenceNameToTest.isEmpty())) {
            String preferenceValue = getSharedPreferences().getString(enbaleExtenderPreferenceNameToTest, "");
            preferenceValueOK = preferenceValue.equals(enbaleExtenderPreferenceValueToTest);
        }
        String summary;
        if (preferenceValueOK) {
            if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = "<b>" + _context.getString(R.string.accessibility_service_enabled) + "</b>";
            else {
                if (accessibilityEnabled == -1) {
                    summary = "<b>" + _context.getString(R.string.accessibility_service_not_used) + "</b>";
                    summary = summary + "<br>" + _context.getString(R.string.preference_not_used_extender_reason) + " " +
                            _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = "<b>" + _context.getString(R.string.accessibility_service_disabled) + "</b>";
                    if ((enableExtenderSummaryDisabled != null) && (!enableExtenderSummaryDisabled.isEmpty()))
                        summary = summary + "<br>" + enableExtenderSummaryDisabled;
                    else
                        summary = summary + "<br>" + _context.getString(R.string.event_preferences_applications_AccessibilitySettingsForExtender_summary);
                }
            }
        }
        else {
            summary = "<b>" + _context.getString(R.string.accessibility_service_not_used) + "</b>";
        }
        prefVolumeDataSummary = prefVolumeDataSummary + "<br><br>" +
                _context.getString(R.string.pppextender_pref_dialog_accessibility_settings_title) + ": " +
                summary;

        if ((lauchSummary != null) && (!lauchSummary.isEmpty()))
            prefVolumeDataSummary = prefVolumeDataSummary + "<br><br><b>" + lauchSummary + "</b>";

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
        final ExtenderDialogPreference.SavedState myState = new ExtenderDialogPreference.SavedState(superState);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryEDP();
            return;
        }

        // restore instance state
        ExtenderDialogPreference.SavedState myState = (ExtenderDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());

        setSummaryEDP();
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
                new Creator<ExtenderDialogPreference.SavedState>() {
                    public ExtenderDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ExtenderDialogPreference.SavedState(in);
                    }
                    public ExtenderDialogPreference.SavedState[] newArray(int size)
                    {
                        return new ExtenderDialogPreference.SavedState[size];
                    }

                };

    }

}
