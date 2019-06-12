package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class LocationGeofencePreferenceX extends DialogPreference {

    LocationGeofencePreferenceFragmentX fragment;

    private final Context context;

    final int onlyEdit;

    private String defaultValue;
    private boolean savedInstanceState;

    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    //private TextView geofenceName;
    LocationGeofencesPreferenceAdapterX listAdapter;

    public final DataWrapper dataWrapper;

    static final String EXTRA_GEOFENCE_ID = "geofence_id";
    static final int RESULT_GEOFENCE_EDITOR = 2100;

    public LocationGeofencePreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray locationGeofenceType = context.obtainStyledAttributes(attrs,
                R.styleable.LocationGeofencePreference, 0, 0);

        onlyEdit = locationGeofenceType.getInt(R.styleable.LocationGeofencePreference_onlyEdit, 0);

        locationGeofenceType.recycle();

        this.context = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        if (onlyEdit != 0)
            setNegativeButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (onlyEdit == 0) {
            String value = getPersistedString((String) defaultValue);
            this.defaultValue = (String)defaultValue;

            // clear all checks
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence("", 0);
            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
            setSummary();
        }
    }

    /*
    String getPersistedGeofence() {
        return getPersistedString("");
    }
    */

    void persistGeofence(boolean reset) {
        PPApplication.logE("LocationGeofencePreferenceX.persistGeofence", "onlyEdit="+onlyEdit);
        if (onlyEdit == 0) {
            if (shouldPersist()) {
                PPApplication.logE("LocationGeofencePreferenceX.persistGeofence", "shouldPersist=true");
                // get value for checked
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                if (callChangeListener(value)) {
                    if (reset)
                        persistString("");
                    PPApplication.logE("LocationGeofencePreferenceX.persistGeofence", "value="+value);
                    persistString(value);
                }
            }
            setSummary();
        }
    }

    void resetSummary() {
        if ((onlyEdit == 0) && (!savedInstanceState)) {
            PPApplication.logE("LocationGeofencePreferenceX.resetSummary", "xxx");
            String value = getPersistedString(defaultValue);
            // clear all checks
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence("", 0);
            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
            setSummary();
        }
        savedInstanceState = false;
    }

    /*
    public void updateGUIWithGeofence(long geofenceId)
    {
        String name = "";
        if (onlyEdit == 0) {
            name = dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId);
            if (name.isEmpty())
                name = "[" + context.getString(R.string.event_preferences_locations_location_not_selected) + "]";
        }

        this.geofenceName.setText(name);
    }
    */

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    public void refreshListView()
    {
        if (listAdapter != null)
            listAdapter.reload(dataWrapper);
    }

    void setGeofenceFromEditor(/*long geofenceId*/) {
        PPApplication.logE("LocationGeofencePreferenceX.setGeofenceFromEditor", "xxx");
        persistGeofence(true);
        refreshListView();
        //updateGUIWithGeofence(geofenceId);
    }

    private void setSummary() {
        if (onlyEdit == 0) {
            if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + context.getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
            }
            /*else
            if (!ApplicationPreferences.applicationEventLocationEnableScanning(context.getApplicationContext())) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
            }*/
            else {
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                String[] splits = value.split("\\|");
                for (String _geofence : splits) {
                    if (_geofence.isEmpty()) {
                        setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    } else if (splits.length == 1) {
                        setSummary(EventPreferencesLocation.getGeofenceName(Long.valueOf(_geofence), context));
                    } else {
                        String selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedLocations = selectedLocations + " " + splits.length;
                        setSummary(selectedLocations);
                        break;
                    }
                }
            }
            //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        PPApplication.logE("LocationGeofencePreferenceX.onSaveInstanceState", "xxx");

        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final LocationGeofencePreferenceX.SavedState myState = new LocationGeofencePreferenceX.SavedState(superState);
        if (onlyEdit == 0) {
            myState.value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
            myState.defaultValue = defaultValue;
        }
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(LocationGeofencePreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary();
            return;
        }

        PPApplication.logE("LocationGeofencePreferenceX.onRestoreInstanceState", "xxx");

        // restore instance state
        if (onlyEdit == 0) {
            LocationGeofencePreferenceX.SavedState myState = (LocationGeofencePreferenceX.SavedState)state;
            super.onRestoreInstanceState(myState.getSuperState());
            String value = myState.value;
            defaultValue = myState.defaultValue;

            // clear all checks
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence("", 0);
            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
            refreshListView();
            setSummary();
        }
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

            // save profileId
            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<LocationGeofencePreferenceX.SavedState> CREATOR =
                new Creator<LocationGeofencePreferenceX.SavedState>() {
                    public LocationGeofencePreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new LocationGeofencePreferenceX.SavedState(in);
                    }
                    public LocationGeofencePreferenceX.SavedState[] newArray(int size)
                    {
                        return new LocationGeofencePreferenceX.SavedState[size];
                    }

                };

    }

}