package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

public class SmallerTextSizeListDialogPreferenceX extends DialogPreference {

    SmallerTextSizeListDialogPreferenceFragmentX fragment;

    private final Context prefContext;

    String value = "";
    private String defaultValue;
//    private boolean savedInstanceState;

    CharSequence[] entries;
    CharSequence[] entryValues;

    public SmallerTextSizeListDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SmallerTextSizeListDialogPreference);

        int entriesRes = typedArray.getResourceId(
                R.styleable.SmallerTextSizeListDialogPreference_ppEntries, 0);
        int entryValuesRes = typedArray.getResourceId(
                R.styleable.SmallerTextSizeListDialogPreference_ppEntryValues, 0);

        this.prefContext = context;

        typedArray.recycle();

        entries = context.getResources().getStringArray(entriesRes);
        entryValues = context.getResources().getStringArray(entryValuesRes);

        setPositiveButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        setSummarySTSDP();
    }

    void setSummarySTSDP()
    {
        String prefSummary = "";
        if (!value.isEmpty()) {
            try {
                for (int i = 0; i < entryValues.length; i++) {
                    String entryValue = entryValues[i].toString();
                    if (entryValue.equals(value)) {
                        prefSummary = entries[i].toString();
                        break;
                    }
                }
            } catch (Exception e) {
                prefSummary = "";
            }
        }
        setSummary(prefSummary);
    }

    void persistValue() {
        Log.e("SmallerTextSizeListDialogPreferenceX.persistValue", "value="+value);
        persistString(value);
        setSummarySTSDP();
        notifyChanged();
    }

    /*
    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummarySTSDP();
        }
        savedInstanceState = false;
    }
    */

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
        setSummarySTSDP();
    }

    void setEntries(CharSequence[] pEntries) {
        entries = pEntries;
        setSummarySTSDP();
    }

    void setEntryValues(int entryValuesRes) {
        entryValues = prefContext.getResources().getStringArray(entryValuesRes);
        setSummarySTSDP();
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
//        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final SmallerTextSizeListDialogPreferenceX.SavedState myState = new SmallerTextSizeListDialogPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SmallerTextSizeListDialogPreferenceX.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummarySTSDP();
            return;
        }

        // restore instance state
        SmallerTextSizeListDialogPreferenceX.SavedState myState = (SmallerTextSizeListDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummarySTSDP();
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

        public static final Creator<SmallerTextSizeListDialogPreferenceX.SavedState> CREATOR =
                new Creator<SmallerTextSizeListDialogPreferenceX.SavedState>() {
                    public SmallerTextSizeListDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new SmallerTextSizeListDialogPreferenceX.SavedState(in);
                    }
                    public SmallerTextSizeListDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new SmallerTextSizeListDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
