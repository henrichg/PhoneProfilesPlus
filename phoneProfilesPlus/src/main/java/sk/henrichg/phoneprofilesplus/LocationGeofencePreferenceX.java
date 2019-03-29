package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class LocationGeofencePreferenceX extends DialogPreference {

    private final Context context;

    final int onlyEdit;

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
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
        }
    }

    /*
    String getPersistedGeofence() {
        return getPersistedString("");
    }
    */

    void persistGeofence(boolean reset) {
        if (onlyEdit == 0) {
            if (shouldPersist()) {
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                if (callChangeListener(value)) {
                    if (reset)
                        persistString("");
                    persistString(value);
                }
            }
        }
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
    
    public void refreshListView()
    {
        listAdapter.reload(dataWrapper);
    }

    void setGeofenceFromEditor(/*long geofenceId*/) {
        PPApplication.logE("LocationGeofencePreferenceX.setGeofenceFromEditor", "xxx");
        persistGeofence(true);
        refreshListView();
        //updateGUIWithGeofence(geofenceId);
    }

}