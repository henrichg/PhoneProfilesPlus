package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.util.Calendar;

import androidx.preference.DialogPreference;

public class TimeDialogPreferenceX extends DialogPreference {

    TimeDialogPreferenceFragmentX fragment;

    int value;
    private int defaultValue;
    private boolean savedInstanceState;

    final int mMin, mMax;

    public TimeDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = 23 * 60 + 59;
        mMin = 0;

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
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
        setSummaryTDP();
    }

    void setSummaryTDP() {
        setSummary(GlobalGUIRoutines.getTimeString(value));
    }

    void persistValue(int value)
    {
        if (shouldPersist()) {
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
            setSummaryTDP();
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

        final TimeDialogPreferenceX.SavedState myState = new TimeDialogPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        /*myState.mMin = mMin;
        myState.mMax = mMax;*/
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(TimeDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryTDP();
            return;
        }

        // restore instance state
        TimeDialogPreferenceX.SavedState myState = (TimeDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;
        /*mMin = myState.mMin;
        mMax = myState.mMax;*/

        setSummaryTDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        int value;
        int defaultValue;
        //int mMin, mMax;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readInt();
            defaultValue = source.readInt();
            /*mMin = source.readInt();
            mMax = source.readInt();*/
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeInt(value);
            dest.writeInt(defaultValue);
            /*dest.writeInt(mMin);
            dest.writeInt(mMax);*/
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<TimeDialogPreferenceX.SavedState> CREATOR =
                new Creator<TimeDialogPreferenceX.SavedState>() {
                    public TimeDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new TimeDialogPreferenceX.SavedState(in);
                    }
                    public TimeDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new TimeDialogPreferenceX.SavedState[size];
                    }

                };

    }

}