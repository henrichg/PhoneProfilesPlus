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
    //final int requiredExtenderVersionCode;
    //String requiredExtenderVersionName;

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
        //requiredExtenderVersionCode = typedArray.getInt(
        //        R.styleable.PPExtenderDialogPreference_requiredExtenderVersionCode, PPApplication.VERSION_CODE_EXTENDER_REQUIRED);
        //requiredExtenderVersionName = typedArray.getString(
        //        R.styleable.PPExtenderDialogPreference_requiredExtenderVersionName);
        //if ((requiredExtenderVersionName == null) || (requiredExtenderVersionName.isEmpty()))
        //    requiredExtenderVersionName = PPApplication.VERSION_NAME_EXTENDER_REQUIRED;

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

        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(_context);
        if (extenderVersion == 0) {
            prefVolumeDataSummary = StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary) + StringConstants.TAG_BOLD_END_HTML;

            if ((installSummary != null) && (!installSummary.isEmpty()))
                prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_SEPARATOR_BREAK_HTML + installSummary;
        }
        else {
            String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(_context);
            prefVolumeDataSummary =  _context.getString(R.string.install_extender_installed_version) +
                    " "+StringConstants.TAG_BOLD_START_HTML + extenderVersionName + " (" + extenderVersion + ")"+StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML;

            prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.install_extender_required_version) +
                    " "+StringConstants.TAG_BOLD_START_HTML + PPApplication.VERSION_NAME_EXTENDER_REQUIRED + " (" + PPApplication.VERSION_CODE_EXTENDER_REQUIRED + ")"+StringConstants.TAG_BOLD_END_HTML;
            if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)
                prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_SEPARATOR_BREAK_HTML +StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary) + StringConstants.TAG_BOLD_END_HTML;
            else
                prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_SEPARATOR_BREAK_HTML + _context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
        }

        int accessibilityEnabled;// = -99;
        if (extenderVersion == 0)
            // not installed
            accessibilityEnabled = -2;
        else
        if ((extenderVersion > 0) &&
                (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED))
            // old version
            accessibilityEnabled = -1;
        else
            accessibilityEnabled = -98;
        if (accessibilityEnabled == -98) {
            // Extender is in right version
            if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(_context, false, true))
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
                summary = StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.accessibility_service_enabled) + StringConstants.TAG_BOLD_END_HTML;
            else {
                if (accessibilityEnabled == -1) {
                    summary = StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.accessibility_service_not_used) + StringConstants.TAG_BOLD_END_HTML;
                    summary = summary + StringConstants.TAG_BREAK_HTML + _context.getString(R.string.preference_not_used_extender_reason) + " " +
                            _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.accessibility_service_disabled) + StringConstants.TAG_BOLD_END_HTML;
                    if ((enableExtenderSummaryDisabled != null) && (!enableExtenderSummaryDisabled.isEmpty()))
                        summary = summary + StringConstants.TAG_BREAK_HTML + enableExtenderSummaryDisabled;
                    else
                        summary = summary + StringConstants.TAG_BREAK_HTML + _context.getString(R.string.event_preferences_applications_AccessibilitySettingsForExtender_summary);
                }
            }
        }
        else {
            summary = StringConstants.TAG_BOLD_START_HTML + _context.getString(R.string.accessibility_service_not_used) + StringConstants.TAG_BOLD_END_HTML;
        }
        prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_SEPARATOR_BREAK_HTML +
                _context.getString(R.string.event_preferences_applications_AccessibilitySettings_title) + StringConstants.STR_COLON_WITH_SPACE +
                summary;

        if ((lauchSummary != null) && (!lauchSummary.isEmpty()))
            prefVolumeDataSummary = prefVolumeDataSummary + StringConstants.TAG_SEPARATOR_BREAK_HTML +StringConstants.TAG_BOLD_START_HTML + lauchSummary + StringConstants.TAG_BOLD_END_HTML;

        setSummary(StringFormatUtils.fromHtml(prefVolumeDataSummary, false,  false, 0, 0, true));
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

        //final ExtenderDialogPreference.SavedState myState = new ExtenderDialogPreference.SavedState(superState);
        //return myState;
        return new ExtenderDialogPreference.SavedState(superState);
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

        public static final Creator<ExtenderDialogPreference.SavedState> CREATOR =
                new Creator<>() {
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
