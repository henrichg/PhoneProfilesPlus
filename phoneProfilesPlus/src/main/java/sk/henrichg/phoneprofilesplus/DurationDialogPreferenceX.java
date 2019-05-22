package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class DurationDialogPreferenceX extends DialogPreference {

    DurationDialogPreferenceFragmentX fragment;

    String value;
    String defaultValue;

    final int mMin, mMax;

    public DurationDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

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
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        setSummaryDDP();
    }

    void setSummaryDDP() {
        setSummary(GlobalGUIRoutines.getDurationString(Integer.parseInt(value)));
    }

    void persistValue(String value)
    {
        persistString(value);
    }

    void resetSummary() {
        value = getPersistedString((String) defaultValue);
        setSummaryDDP();
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final DurationDialogPreferenceX.SavedState myState = new DurationDialogPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        /*myState.mMin = mMin;
        myState.mMax = mMax;*/
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(DurationDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryDDP();
            return;
        }

        // restore instance state
        DurationDialogPreferenceX.SavedState myState = (DurationDialogPreferenceX.SavedState)state;
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

        @SuppressWarnings("unused")
        public static final Creator<DurationDialogPreferenceX.SavedState> CREATOR =
                new Creator<DurationDialogPreferenceX.SavedState>() {
                    public DurationDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new DurationDialogPreferenceX.SavedState(in);
                    }
                    public DurationDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new DurationDialogPreferenceX.SavedState[size];
                    }

                };

    }

}