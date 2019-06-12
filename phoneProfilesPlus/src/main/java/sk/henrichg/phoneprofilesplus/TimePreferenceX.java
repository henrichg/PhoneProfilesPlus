package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TimePicker;

import java.sql.Date;
import java.util.Calendar;

//import androidx.leanback.widget.picker.TimePicker;
import androidx.preference.DialogPreference;

public class TimePreferenceX extends DialogPreference {

    TimePreferenceFragmentX fragment;

    int value;
    private int defaultValue;
    private boolean savedInstanceState;

    private final Context context;
    //private Calendar calendar;

    public TimePreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

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
        return DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
    }

    void persistValue(TimePicker picker) {
        if (shouldPersist()) {
            picker.clearFocus();

            value = picker.getCurrentHour() * 60 + picker.getCurrentMinute();

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

        final TimePreferenceX.SavedState myState = new TimePreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(TimePreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary(getSummary());
            return;
        }

        // restore instance state
        TimePreferenceX.SavedState myState = (TimePreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        PPApplication.logE("TimePreferenceX.onRestoreInstanceState", "value="+getSummary());

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
        public static final Creator<TimePreferenceX.SavedState> CREATOR =
                new Creator<TimePreferenceX.SavedState>() {
                    public TimePreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new TimePreferenceX.SavedState(in);
                    }
                    public TimePreferenceX.SavedState[] newArray(int size)
                    {
                        return new TimePreferenceX.SavedState[size];
                    }

                };

    }

} 
