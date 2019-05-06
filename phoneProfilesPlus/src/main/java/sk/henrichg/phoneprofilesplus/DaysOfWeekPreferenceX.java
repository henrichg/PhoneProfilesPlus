package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class DaysOfWeekPreferenceX extends DialogPreference {

    DaysOfWeekPreferenceFragmentX fragment;

    static final String allValue = "#ALL#";

    private String value = "";

    final List<DayOfWeek> daysOfWeekList;

    public DaysOfWeekPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        daysOfWeekList = new ArrayList<>();

        //CharSequence[] newEntries = new CharSequence[8];
        //CharSequence[] newEntryValues = new CharSequence[8];

        /*
        String[] newEntries = _context.getResources().getStringArray(R.array.daysOfWeekArray);
        String[] newEntryValues = _context.getResources().getStringArray(R.array.daysOfWeekValues);
        */

        daysOfWeekList.clear();
        DayOfWeek dayOfWeek = new DayOfWeek();
        dayOfWeek.name = context.getString(R.string.array_pref_event_all);
        dayOfWeek.value = allValue;
        daysOfWeekList.add(dayOfWeek);

        String[] namesOfDay = DateFormatSymbols.getInstance().getWeekdays();

        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);

            dayOfWeek = new DayOfWeek();
            dayOfWeek.name = namesOfDay[_dayOfWeek+1];
            dayOfWeek.value = String.valueOf(_dayOfWeek);
            daysOfWeekList.add(dayOfWeek);
        }

    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Get the persistent value
        value = getPersistedString(value);
        getValueDOWMDP();
        setSummaryDOWMDP();
    }

    private void getValueDOWMDP()
    {
        // change checked state by value
        if (daysOfWeekList != null)
        {
            String[] splits = value.split("\\|");
            for (DayOfWeek dayOfWeek : daysOfWeekList)
            {
                dayOfWeek.checked = false;
                for (String split : splits) {
                    if (dayOfWeek.value.equals(split))
                        dayOfWeek.checked = true;
                }
            }
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void setSummaryDOWMDP()
    {
        String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

        String summary = "";

        if ((daysOfWeekList != null) && (daysOfWeekList.size() > 0))
        {
            if (daysOfWeekList.get(0).checked)
            {
                for ( int i = 1; i <= namesOfDay.length; i++ )
                    summary = summary + namesOfDay[EventPreferencesTime.getDayOfWeekByLocale(i-1)+1] + " ";
            }
            else
            {
                for ( int i = 1; i < daysOfWeekList.size(); i++ )
                {
                    DayOfWeek dayOfWeek = daysOfWeekList.get(i);
                    if (dayOfWeek.checked)
                        summary = summary + namesOfDay[EventPreferencesTime.getDayOfWeekByLocale(i-1)+1] + " ";
                }
            }
        }

        setSummary(summary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void persistValue() {
        if (shouldPersist())
        {
            // fill with days of week separated with |
            value = "";
            if (daysOfWeekList != null)
            {
                for (DayOfWeek dayOfWeek : daysOfWeekList)
                {
                    if (dayOfWeek.checked)
                    {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + dayOfWeek.value;
                    }
                }
            }
            persistString(value);

            setSummaryDOWMDP();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final DaysOfWeekPreferenceX.SavedState myState = new DaysOfWeekPreferenceX.SavedState(superState);
        myState.value = value;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(DaysOfWeekPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryDOWMDP();
            return;
        }

        // restore instance state
        DaysOfWeekPreferenceX.SavedState myState = (DaysOfWeekPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;

        setSummaryDOWMDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<DaysOfWeekPreferenceX.SavedState> CREATOR =
                new Creator<DaysOfWeekPreferenceX.SavedState>() {
                    public DaysOfWeekPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new DaysOfWeekPreferenceX.SavedState(in);
                    }
                    public DaysOfWeekPreferenceX.SavedState[] newArray(int size)
                    {
                        return new DaysOfWeekPreferenceX.SavedState[size];
                    }

                };

    }

}
