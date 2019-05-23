package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class WifiSSIDPreferenceX extends DialogPreference {

    WifiSSIDPreferenceFragmentX fragment;

    Context context;

    String value;
    String defaultValue;
    boolean savedInstanceState;

    List<WifiSSIDData> SSIDList;
    final List<WifiSSIDData> customSSIDList;

    static boolean forceRegister = false;

    public WifiSSIDPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        SSIDList = new ArrayList<>();
        customSSIDList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString(value);
        this.defaultValue = (String)defaultValue;
        setSummary();
    }

    void addSSID(String ssid) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String _ssid : splits) {
            if (_ssid.equals(ssid))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + ssid;
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeSSID(String ssid) {
        String[] splits = value.split("\\|");
        value = "";
        for (String _ssid : splits) {
            if (!_ssid.isEmpty()) {
                if (!_ssid.equals(ssid)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _ssid;
                }
            }
        }
    }

    boolean isSSIDSelected(String ssid) {
        String[] splits = value.split("\\|");
        for (String _ssid : splits) {
            if (_ssid.equals(ssid))
                return true;
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(boolean forRescan, final String scrollToSSID)
    {
        if (fragment != null)
            fragment.refreshListView(forRescan, scrollToSSID);
    }

    void showEditMenu(View view)
    {
        if (fragment != null)
            fragment.showEditMenu(view);
    }

    void setSummary() {
        /*if (!ApplicationPreferences.applicationEventWifiEnableScanning(context.getApplicationContext())) {
            preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
        }
        else {*/
        String[] splits = value.split("\\|");
        for (String _ssid : splits) {
            if (_ssid.isEmpty()) {
                setSummary(R.string.applications_multiselect_summary_text_not_selected);
            } else if (splits.length == 1) {
                switch (_ssid) {
                    case EventPreferencesWifi.ALL_SSIDS_VALUE:
                        setSummary(R.string.wifi_ssid_pref_dlg_all_ssids_chb);
                        break;
                    case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                        setSummary(R.string.wifi_ssid_pref_dlg_configured_ssids_chb);
                        break;
                    default:
                        setSummary(_ssid);
                        break;
                }
            } else {
                String selectedSSIDs = context.getString(R.string.applications_multiselect_summary_text_selected);
                selectedSSIDs = selectedSSIDs + " " + splits.length;
                setSummary(selectedSSIDs);
                break;
            }
        }
        //}
        //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
    }

    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value))
            {
                persistString(value);
                setSummary();
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummary();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final WifiSSIDPreferenceX.SavedState myState = new WifiSSIDPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(WifiSSIDPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        WifiSSIDPreferenceX.SavedState myState = (WifiSSIDPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummary();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<WifiSSIDPreferenceX.SavedState> CREATOR =
                new Creator<WifiSSIDPreferenceX.SavedState>() {
                    public WifiSSIDPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new WifiSSIDPreferenceX.SavedState(in);
                    }
                    public WifiSSIDPreferenceX.SavedState[] newArray(int size)
                    {
                        return new WifiSSIDPreferenceX.SavedState[size];
                    }

                };

    }

}