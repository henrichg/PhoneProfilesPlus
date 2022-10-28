package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

public class DurationDialogPreference extends DialogPreference {

    DurationDialogPreferenceFragment fragment;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    final int mMin, mMax;

    public DurationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.PPDurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.PPDurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.PPDurationDialogPreference_dMin, 0);

        durationDialogType.recycle();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index) {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        setSummaryDDP();
    }

    void setSummaryDDP() {
        setSummary(StringFormatUtils.getDurationString(Integer.parseInt(value)));
    }

    void persistValue(String value)
    {
        persistString(value);
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryDDP();
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

        final DurationDialogPreference.SavedState myState = new DurationDialogPreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        /*myState.mMin = mMin;
        myState.mMax = mMax;*/
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(DurationDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryDDP();
            return;
        }

        // restore instance state
        DurationDialogPreference.SavedState myState = (DurationDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;
        /*mMin = myState.mMin;
        mMax = myState.mMax;*/

        setSummaryDDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;
        //int mMin, mMax;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readString();
            defaultValue = source.readString();
            /*mMin = source.readInt();
            mMax = source.readInt();*/
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(value);
            dest.writeString(defaultValue);
            /*dest.writeInt(mMin);
            dest.writeInt(mMax);*/
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<DurationDialogPreference.SavedState> CREATOR =
                new Creator<DurationDialogPreference.SavedState>() {
                    public DurationDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new DurationDialogPreference.SavedState(in);
                    }
                    public DurationDialogPreference.SavedState[] newArray(int size)
                    {
                        return new DurationDialogPreference.SavedState[size];
                    }

                };

    }

}