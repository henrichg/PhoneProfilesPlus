package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class DaysOfWeekPreference extends DialogPreference {

    DaysOfWeekPreferenceFragment fragment;

    static final String allValue = "#ALL#";

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    final List<DayOfWeek> daysOfWeekList;

    public DaysOfWeekPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        daysOfWeekList = new ArrayList<>();

        DayOfWeek dayOfWeek;
        String[] longNamesOfDay = DateFormatSymbols.getInstance().getWeekdays();
        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);

            dayOfWeek = new DayOfWeek();
            dayOfWeek.name = longNamesOfDay[_dayOfWeek+1];
            dayOfWeek.value = String.valueOf(_dayOfWeek);
            daysOfWeekList.add(dayOfWeek);
        }

    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Get the persistent value
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueDOWMDP();
        setSummaryDOWMDP();
    }

    void getValueDOWMDP()
    {
        // change checked state by value
        if (daysOfWeekList != null)
        {
            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
            boolean allIsConfigured = false;
            for (String split : splits) {
                if (split.equals(allValue)) {
                    allIsConfigured = true;
                    for (DayOfWeek dayOfWeek : daysOfWeekList)
                        dayOfWeek.checked = true;
                    break;
                }
            }
            if (!allIsConfigured) {
                for (DayOfWeek dayOfWeek : daysOfWeekList) {
                    dayOfWeek.checked = false;
                    for (String split : splits) {
                        if (dayOfWeek.value.equals(split)) {
                            dayOfWeek.checked = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setSummaryDOWMDP() {
        String summary;// = "";

        boolean allIsConfigured = false;
        boolean[] daySet = new boolean[7];
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        if (!value.isEmpty()) {
            for (String split : splits) {
                if (split.equals(allValue)) {
                    for (int i = 0; i < 7; i++)
                        daySet[i] = true;
                    allIsConfigured = true;
                    break;
                }
                daySet[Integer.parseInt(split)] = true;
            }
        }
        if (!allIsConfigured) {
            allIsConfigured = true;
            for (int i = 0; i < 7; i++)
                allIsConfigured = allIsConfigured && daySet[i];
        }
        String[] shortNamesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

        StringBuilder _value = new StringBuilder();
        for ( int i = 1; i < 8; i++ ) {
            int _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
            if (value.contains(String.valueOf(_dayOfWeek)))
                //summary = summary + shortNamesOfDay[_dayOfWeek+1] + " ";
                _value.append(shortNamesOfDay[_dayOfWeek+1]).append(" ");
        }
        summary = _value.toString();

        setSummary(summary);
    }

    void getValue() {
        // fill with days of week separated with |
        value = "";
        StringBuilder _value = new StringBuilder();
        if (daysOfWeekList != null)
        {
            for (DayOfWeek dayOfWeek : daysOfWeekList)
            {
                if (dayOfWeek.checked)
                {
                    //if (!value.isEmpty())
                    //    value = value + "|";
                    //value = value + dayOfWeek.value;
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(dayOfWeek.value);
                }
            }
        }
        value = _value.toString();
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
            persistString(value);

            setSummaryDOWMDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryDOWMDP();
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

        final DaysOfWeekPreference.SavedState myState = new DaysOfWeekPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(DaysOfWeekPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryDOWMDP();
            return;
        }

        // restore instance state
        DaysOfWeekPreference.SavedState myState = (DaysOfWeekPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValueDOWMDP();
        setSummaryDOWMDP();
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

        public static final Creator<DaysOfWeekPreference.SavedState> CREATOR =
                new Creator<>() {
                    public DaysOfWeekPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new DaysOfWeekPreference.SavedState(in);
                    }
                    public DaysOfWeekPreference.SavedState[] newArray(int size)
                    {
                        return new DaysOfWeekPreference.SavedState[size];
                    }

                };

    }

}
