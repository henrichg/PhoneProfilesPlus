package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PPMultiSelectListPreference extends DialogPreference {

    PPMultiSelectListPreferenceFragment fragment;

    //private final Context prefContext;

    Set<String> value = null;
    private Set<String> defaultValue;
    private boolean savedInstanceState;

    final CharSequence[] entries;
    final CharSequence[] entryValues;

    public PPMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPListDialogPreference);

        int entriesRes = typedArray.getResourceId(
                R.styleable.PPListDialogPreference_ppEntries, 0);
        int entryValuesRes = typedArray.getResourceId(
                R.styleable.PPListDialogPreference_ppEntryValues, 0);

        //this.prefContext = context;

        typedArray.recycle();

        entries = context.getResources().getStringArray(entriesRes);
        entryValues = context.getResources().getStringArray(entryValuesRes);

        //setPositiveButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedStringSet((Set<String>) defaultValue);
        this.defaultValue = (Set<String>) defaultValue;

        setSummaryMSLDP();
    }

    void setSummaryMSLDP()
    {
        String prefSummary = "";
        if (!value.isEmpty()) {
            try {
                StringBuilder _summary = new StringBuilder();
                for (String _value : value) {
                    //if (!prefSummary.isEmpty())
                    //    prefSummary = prefSummary + ", ";
                    if (_summary.length() > 0)
                        _summary.append(", ");
                    int index = 0;
                    boolean found = false;
                    for (CharSequence entryValue : entryValues) {
                        if (_value.equals(entryValue.toString())) {
                            found = true;
                            break;
                        }
                        ++index;
                    }
                    if (found)
                        //prefSummary = prefSummary + entries[index];
                        _summary.append(entries[index]);
                }
                prefSummary = _summary.toString();
            } catch (Exception e) {
                prefSummary = "";
            }
        }
        setSummary(prefSummary);
    }

    void persistValue() {
//        Log.e("PPMultiSelectListPreference.persistValue", "---");
//        for (String v : value) {
//            Log.e("PPMultiSelectListPreference.persistValue", v);
//        }

        // WARNING!! This must be called, without this notifyChanged() not working!!
        //           because of preference is StringSet
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getKey());
        editor.apply();
        //--------

        persistStringSet(value);
        setSummaryMSLDP();
        notifyChanged();
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedStringSet(defaultValue);
            setSummaryMSLDP();
        }
        savedInstanceState = false;
    }

    /*
    int findIndexOfValue(String pValue) {
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValues[i].equals(pValue))
                return i;
        }
        return -1;
    }

    CharSequence[] getEntries() {
        return entries;
    }

    void setEntries(int entriesRes) {
        entries = prefContext.getResources().getStringArray(entriesRes);
        setSummaryMSLDP();
    }

    void setEntries(CharSequence[] pEntries) {
        entries = pEntries;
        setSummaryMSLDP();
    }

    void setEntryValues(int entryValuesRes) {
        entryValues = prefContext.getResources().getStringArray(entryValuesRes);
        setSummaryMSLDP();
    }

    void setEntryValues(CharSequence[] pEntryValues) {
        entryValues = pEntryValues;
        setSummaryMSLDP();
    }
    */

    Set<String> getValues() {
        return value;
    }

    public void setValues(Set<String> pValue) {
        value = pValue;
        setSummaryMSLDP();
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final PPMultiSelectListPreference.SavedState myState = new PPMultiSelectListPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(PPMultiSelectListPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryMSLDP();
            return;
        }

        // restore instance state
        PPMultiSelectListPreference.SavedState myState = (PPMultiSelectListPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummaryMSLDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        Set<String>  value;
        Set<String>  defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            String[] _value = source.readStringArray();
            String[] _defaultValue = source.readStringArray();

            value = new HashSet<>();
            value.addAll(Arrays.asList(_value));

            defaultValue = new HashSet<>();
            defaultValue.addAll(Arrays.asList(_defaultValue));
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            String[] _value;
            String[] _defaultValue;
            if (value != null)
                _value = value.toArray(new String[0]);
            else
                _value = null;
            if (defaultValue != null)
                _defaultValue = defaultValue.toArray(new String[0]);
            else
                _defaultValue = null;

            dest.writeStringArray(_value);
            dest.writeStringArray(_defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<PPMultiSelectListPreference.SavedState> CREATOR =
                new Creator<PPMultiSelectListPreference.SavedState>() {
                    public PPMultiSelectListPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new PPMultiSelectListPreference.SavedState(in);
                    }
                    public PPMultiSelectListPreference.SavedState[] newArray(int size)
                    {
                        return new PPMultiSelectListPreference.SavedState[size];
                    }

                };

    }

}
