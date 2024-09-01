package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class VibrationIntensityPreference extends DialogPreference {

    VibrationIntensityPreferenceFragment fragment;

    private final Context _context;

    // Custom xml attributes.
    final String vibrationIntensityType;
    int noChange;

    //final int minimumValue;
    final int maximumValue;
    final int stepSize = 1;

    private String sValue = "0|1";
    private String defaultValue;
    private boolean savedInstanceState;

    int value = 0;

    static final String RINGING_VYBRATION_INTENSITY_TYPE = "RINGING";
    static final String NOTIFICATIONS_VYBRATION_INTENSITY_TYPE = "NOTIFICATIONS";
    static final String TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE = "TOUCHINTERACTION";

    public VibrationIntensityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.PPVibrationIntensityDialogPreference);

        vibrationIntensityType = typedArray.getString(
            R.styleable.PPVibrationIntensityDialogPreference_vibrationIntensityType);
        noChange = typedArray.getInteger(
                R.styleable.PPVibrationIntensityDialogPreference_viNoChange, 1);

        typedArray.recycle();

        maximumValue = getMaxValue(vibrationIntensityType);
        //minimumValue = getMinValue(vibrationIntensityType);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueVIDP();
        setSummaryVIDP();
    }

    private void getValueVIDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
            {
                //if (Build.VERSION.SDK_INT < 33) {
                    if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                        if (vibrationIntensityType.equalsIgnoreCase(RINGING_VYBRATION_INTENSITY_TYPE))
                            value = 3;
                        else if (vibrationIntensityType.equalsIgnoreCase(NOTIFICATIONS_VYBRATION_INTENSITY_TYPE))
                            value = 3;
                        else if (vibrationIntensityType.equalsIgnoreCase(TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE))
                            value = 1;
                    } else if (PPApplication.deviceIsOnePlus) {
                        if (vibrationIntensityType.equalsIgnoreCase(RINGING_VYBRATION_INTENSITY_TYPE))
                            value = 1060;
                        else if (vibrationIntensityType.equalsIgnoreCase(NOTIFICATIONS_VYBRATION_INTENSITY_TYPE))
                            value = 1060;
                        else if (vibrationIntensityType.equalsIgnoreCase(TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE))
                            value = 430;
                    } else {
                        if (vibrationIntensityType.equalsIgnoreCase(RINGING_VYBRATION_INTENSITY_TYPE))
                            value = 3;
                        else if (vibrationIntensityType.equalsIgnoreCase(NOTIFICATIONS_VYBRATION_INTENSITY_TYPE))
                            value = 3;
                        else if (vibrationIntensityType.equalsIgnoreCase(TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE))
                            value = 1;
                    }
                /*} else {
                    if (vibrationIntensityType.equalsIgnoreCase(RINGING_VYBRATION_INTENSITY_TYPE))
                        value = 2;
                    else if (vibrationIntensityType.equalsIgnoreCase(NOTIFICATIONS_VYBRATION_INTENSITY_TYPE))
                        value = 2;
                    else if (vibrationIntensityType.equalsIgnoreCase(TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE))
                        value = 1;
                }*/
            }
        } catch (Exception e) {
            //Log.e("VibrationIntensityPreference.getValueVDP", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            value = 0;
        }
        //value = value - minimumValue;

        try {
            noChange = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            noChange = 1;
        }

        // You're never know...
        if (value < 0) {
            value = 0;
        }

    }

    private void setSummaryVIDP()
    {
        String prefVibrationIntensityDataSummary;
        if (noChange == 1)
            prefVibrationIntensityDataSummary = _context.getString(R.string.preference_profile_no_change);
        else
            prefVibrationIntensityDataSummary = value + " / " + maximumValue;
        setSummary(prefVibrationIntensityDataSummary);
    }

    String getSValue() {
        return value
                + "|" + noChange;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryVIDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueVIDP();
            setSummaryVIDP();
        }
        savedInstanceState = false;
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final VibrationIntensityPreference.SavedState myState = new VibrationIntensityPreference.SavedState(superState);
        myState.sValue = getSValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(VibrationIntensityPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueVIDP();
            setSummaryVIDP();
            return;
        }

        // restore instance state
        VibrationIntensityPreference.SavedState myState = (VibrationIntensityPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueVIDP();
        setSummaryVIDP();
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
                new Creator<VibrationIntensityPreference.SavedState>() {
                    public VibrationIntensityPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new VibrationIntensityPreference.SavedState(in);
                    }
                    public VibrationIntensityPreference.SavedState[] newArray(int size)
                    {
                        return new VibrationIntensityPreference.SavedState[size];
                    }

                };

    }

    static int getMaxValue(String vibrationIntensityType) {
        int maxValue;
        //if (Build.VERSION.SDK_INT < 33) {
            if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy)
                maxValue = 5;
            else if (PPApplication.deviceIsOnePlus) {
                int minValue = getMinValue(vibrationIntensityType);
                maxValue = 2400 - minValue;
            }
            else
                maxValue = 3;
        //} else
        //    maxValue = 3;

        return maxValue;
    }
    static int getMinValue(String vibrationIntensityType) {
        int minValue;
        //if (Build.VERSION.SDK_INT < 33) {
            if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy)
                minValue = 0;
            else if (PPApplication.deviceIsOnePlus) {
                if (vibrationIntensityType.equalsIgnoreCase(RINGING_VYBRATION_INTENSITY_TYPE))
                    minValue = 800;
                else if (vibrationIntensityType.equalsIgnoreCase(NOTIFICATIONS_VYBRATION_INTENSITY_TYPE))
                    minValue = 800;
                else if (vibrationIntensityType.equalsIgnoreCase(TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE))
                    minValue = 1100;
                else
                    minValue = 800;
            }
            else
                minValue = 0;
        //} else
        //    minValue = 0;

        return minValue;
    }

}
