package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class DaysOfWeekPreferenceX extends DialogPreference {

    DaysOfWeekPreferenceFragmentX fragment;

    static final String allValue = "#ALL#";

    private final Context context;

    private String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    final List<DayOfWeek> daysOfWeekList;

    public DaysOfWeekPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

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

        String[] longNamesOfDay = DateFormatSymbols.getInstance().getWeekdays();
        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("DaysOfWeekPreferenceX.DaysOfWeekPreferenceX", "_dayOfWeek=" + _dayOfWeek);
                PPApplication.logE("DaysOfWeekPreferenceX.DaysOfWeekPreferenceX", "longNamesOfDay[_dayOfWeek+1]=" + longNamesOfDay[_dayOfWeek + 1]);
            }*/

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
        //PPApplication.logE("DaysOfWeekPreferenceX.onSetInitialValue", "call of setSummaryDOWMDP");
        setSummaryDOWMDP();
    }

    void getValueDOWMDP()
    {
        // change checked state by value
        if (daysOfWeekList != null)
        {
            //Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "value="+value);
            String[] splits = value.split("\\|");
            boolean allIsConfigured = false;
            for (String split : splits) {
                //Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "split="+split);
                if (split.equals(allValue)) {
                    //Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "allIsConfigured");
                    allIsConfigured = true;
                    for (DayOfWeek dayOfWeek : daysOfWeekList) {
                        dayOfWeek.checked = !dayOfWeek.value.equals(allValue);
                    }
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

    @SuppressWarnings("StringConcatenationInLoop")
    private void setSummaryDOWMDP() {
        String summary = "";

        boolean allIsConfigured = false;
        boolean[] daySet = new boolean[7];
        String[] splits = value.split("\\|");
        if (!value.isEmpty()) {
            for (String split : splits) {
                if (split.equals(allValue)) {
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
        //Log.e("DaysOfWeekPreferenceX.setSummaryDOWMDP", "allIsConfigured");
        if (allIsConfigured)
            summary = summary + context.getString(R.string.array_pref_event_all) + " ";
        else {
            //PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "value="+value);
            String[] shortNamesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();
            for ( int i = 1; i < 8; i++ ) {
                int _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "_dayOfWeek=" + _dayOfWeek);
                    PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "shortNamesOfDay[_dayOfWeek+1]=" + shortNamesOfDay[_dayOfWeek + 1]);
                }*/
                if (value.contains(String.valueOf(_dayOfWeek)))
                    summary = summary + shortNamesOfDay[_dayOfWeek+1] + " ";
            }
            /*
            for (String split : splits) {
                PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "split="+split);
                for ( int i = 1; i < 8; i++ ) {
                    int _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
                    if (split.equals(String.valueOf(_dayOfWeek))) {
                        PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "_dayOfWeek="+_dayOfWeek);
                        PPApplication.logE("DaysOfWeekPreferenceX.setSummaryDOWMDP", "shortNamesOfDay[_dayOfWeek+1]="+shortNamesOfDay[_dayOfWeek+1]);
                        summary = summary + shortNamesOfDay[_dayOfWeek+1] + " ";
                        break;
                    }
                }
            }
            */
        }

        setSummary(summary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void getValue() {
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
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
            persistString(value);

            //PPApplication.logE("DaysOfWeekPreferenceX.persistValue", "call of setSummaryDOWMDP");
            setSummaryDOWMDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            //PPApplication.logE("DaysOfWeekPreferenceX.resetSummary", "call of setSummaryDOWMDP");
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

        final DaysOfWeekPreferenceX.SavedState myState = new DaysOfWeekPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

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
            //PPApplication.logE("DaysOfWeekPreferenceX.onRestoreInstanceState", "call of setSummaryDOWMDP 1");
            setSummaryDOWMDP();
            return;
        }

        // restore instance state
        DaysOfWeekPreferenceX.SavedState myState = (DaysOfWeekPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValueDOWMDP();
        //PPApplication.logE("DaysOfWeekPreferenceX.onRestoreInstanceState", "call of setSummaryDOWMDP 2");
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
