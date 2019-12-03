package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.kunzisoft.androidclearchroma.ChromaUtil;

import androidx.preference.DialogPreference;

public class CustomColorDialogPreferenceX extends DialogPreference {

    CustomColorDialogPreferenceFragmentX fragment;

    int chromaColorMode;
    int chromaIndicatorMode;

    // Custom xml attributes.
    int value;

    private int defaultValue;
    private boolean savedInstanceState;


    public CustomColorDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ChromaPreference);

        chromaColorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaColorMode, 1);
        chromaIndicatorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaIndicatorMode, 1);

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        PPApplication.logE("CustomColorDialogPreferenceX.onSetInitialValue", "defaultValue="+defaultValue);
        if (defaultValue != null) {
            value = getPersistedInt((int) defaultValue);
            this.defaultValue = (int)defaultValue;
        }
        else {
            value = getPersistedInt(0xFFFFFFFF);
            this.defaultValue = 0xFFFFFF;
        }
        setSummaryCCDP(value);
    }

    void persistValue() {
        if (shouldPersist()) {
            persistInt(value);
            setSummaryCCDP(value);
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedInt(defaultValue);
            setSummaryCCDP(value);
        }
        savedInstanceState = false;
    }

    private void setSummaryCCDP(int value)
    {
        setSummary(ChromaUtil.getFormattedColorString(value, false));
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final CustomColorDialogPreferenceX.SavedState myState = new CustomColorDialogPreferenceX.SavedState(superState);
        if (fragment != null) {
            myState.value = fragment.chromaColorView.getCurrentColor();
            myState.defaultValue = defaultValue;
        }
        else {
            myState.value = value;
            myState.defaultValue = defaultValue;
        }
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            int value = this.value;
            if (fragment != null)
                fragment.chromaColorView.setCurrentColor(value);
            setSummaryCCDP(value);
            return;
        }

        // restore instance state
        CustomColorDialogPreferenceX.SavedState myState = (CustomColorDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        if (fragment != null)
            fragment.chromaColorView.setCurrentColor(value);
        setSummaryCCDP(value);
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
        public static final Creator<SavedState> CREATOR =
                new Creator<CustomColorDialogPreferenceX.SavedState>() {
                    public CustomColorDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new CustomColorDialogPreferenceX.SavedState(in);
                    }
                    public CustomColorDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new CustomColorDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
