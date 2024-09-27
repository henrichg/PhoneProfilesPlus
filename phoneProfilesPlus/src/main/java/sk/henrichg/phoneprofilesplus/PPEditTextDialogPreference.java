package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class PPEditTextDialogPreference extends DialogPreference {

    PPEditTextDialogPreferenceFragment fragment;

    //private final Context _context;

    // Custom xml attributes.
    String editTextValue;
    private String defaultValue;
    private boolean savedInstanceState;

    public PPEditTextDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

//        _context = context;

        //noinspection resource
//        TypedArray typedArray = context.obtainStyledAttributes(attrs,
//            R.styleable.PPPhoneCallSendSMSDialogPreference);
//
//        sendSMS = typedArray.getBoolean(
//                R.styleable.PPPhoneCallSendSMSDialogPreference_sendSMS, false);
//
//        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        this.defaultValue = "";
        if (defaultValue != null)
            this.defaultValue = (String) defaultValue;
        editTextValue = getPersistedString(this.defaultValue);
        setSummaryVDP();
    }

    private void setSummaryVDP()
    {
        setSummary(editTextValue);
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(editTextValue);
            setSummaryVDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            editTextValue = getPersistedString(defaultValue);
            setSummaryVDP();
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

        final PPEditTextDialogPreference.SavedState myState = new PPEditTextDialogPreference.SavedState(superState);
        myState.editTextValue = editTextValue;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(PPEditTextDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            editTextValue = "";
            setSummaryVDP();
            return;
        }

        // restore instance state
        PPEditTextDialogPreference.SavedState myState = (PPEditTextDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        editTextValue = myState.editTextValue;
        defaultValue = myState.defaultValue;
        setSummaryVDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String editTextValue;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            editTextValue = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(editTextValue);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<PPEditTextDialogPreference.SavedState>() {
                    public PPEditTextDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new PPEditTextDialogPreference.SavedState(in);
                    }
                    public PPEditTextDialogPreference.SavedState[] newArray(int size)
                    {
                        return new PPEditTextDialogPreference.SavedState[size];
                    }

                };

    }

}
