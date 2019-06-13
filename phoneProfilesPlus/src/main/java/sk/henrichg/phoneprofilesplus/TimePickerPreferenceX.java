package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import java.sql.Date;
import java.util.Calendar;

import androidx.preference.DialogPreference;

public class TimePickerPreferenceX extends DialogPreference {

    TimePickerPreferenceFragmentX fragment;

    int value;
    private int defaultValue;
    private boolean savedInstanceState;

    final Context _context;

    public TimePickerPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        Calendar now = Calendar.getInstance();
        value = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        super.onGetDefaultValue(a, index);
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        Calendar now = Calendar.getInstance();
        if (defaultValue == null) {
            value = getPersistedInt(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE));
        } else {
            value = getPersistedInt((Integer) defaultValue);
            this.defaultValue = (Integer)defaultValue;
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, value / 60);
        calendar.set(Calendar.MINUTE, value % 60);
        return DateFormat.getTimeFormat(_context).format(new Date(calendar.getTimeInMillis()));
    }

    void persistValue(long duration) {
        if (shouldPersist()) {

            value = (int)(duration / 1000 / 60);
            if (value < 0)
                value = 0;
            if (value > 23 * 60 + 59)
                value = 23 * 60 + 59;

            setSummary(getSummary());
            if (callChangeListener(value)) {
                persistInt(value);
                notifyChanged();
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            Calendar now = Calendar.getInstance();
            if (defaultValue == 0) {
                value = getPersistedInt(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE));
            } else {
                value = getPersistedInt(defaultValue);
            }
            setSummary(getSummary());
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

        final TimePickerPreferenceX.SavedState myState = new TimePickerPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(TimePickerPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary(getSummary());
            return;
        }

        // restore instance state
        TimePickerPreferenceX.SavedState myState = (TimePickerPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        PPApplication.logE("TimePickerPreferenceX.onRestoreInstanceState", "value="+getSummary());

        setSummary(getSummary());
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        int value;
        int defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readInt();
            defaultValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(value);
            dest.writeInt(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<TimePickerPreferenceX.SavedState> CREATOR =
                new Creator<TimePickerPreferenceX.SavedState>() {
                    public TimePickerPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new TimePickerPreferenceX.SavedState(in);
                    }
                    public TimePickerPreferenceX.SavedState[] newArray(int size)
                    {
                        return new TimePickerPreferenceX.SavedState[size];
                    }

                };

    }

}
