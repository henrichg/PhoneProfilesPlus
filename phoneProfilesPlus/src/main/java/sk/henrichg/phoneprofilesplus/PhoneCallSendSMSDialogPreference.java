package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.preference.DialogPreference;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PhoneCallSendSMSDialogPreference extends DialogPreference {

    PhoneCallSendSMSDialogPreferenceFragment fragment;

    private final Context _context;

    // Custom xml attributes.
    boolean sendSMS;
    private boolean defaultValue;
    private boolean savedInstanceState;

    public PhoneCallSendSMSDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.PPPhoneCallSendSMSDialogPreference);

        sendSMS = typedArray.getBoolean(
                R.styleable.PPPhoneCallSendSMSDialogPreference_sendSMS, false);

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        this.defaultValue = false;
        if (defaultValue != null)
            this.defaultValue = (Boolean) defaultValue;
        sendSMS = getPersistedBoolean(this.defaultValue);
        setSummaryVDP();
    }

    private void setSummaryVDP()
    {
        if (sendSMS)
            setSummary(_context.getString(R.string.profile_preference_phoneCallsSendSMS_summary_send));
        else
            setSummary(_context.getString(R.string.profile_preference_phoneCallsSendSMS_summary_notSend));
    }

    void persistValue() {
        if (shouldPersist()) {
            persistBoolean(sendSMS);
            setSummaryVDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sendSMS = getPersistedBoolean(defaultValue);
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

        final PhoneCallSendSMSDialogPreference.SavedState myState = new PhoneCallSendSMSDialogPreference.SavedState(superState);
        myState.sendSMS = sendSMS;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(PhoneCallSendSMSDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            sendSMS = false;
            setSummaryVDP();
            return;
        }

        // restore instance state
        PhoneCallSendSMSDialogPreference.SavedState myState = (PhoneCallSendSMSDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sendSMS = myState.sendSMS;
        defaultValue = myState.defaultValue;
        setSummaryVDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        boolean sendSMS;
        boolean defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            sendSMS = source.readBoolean();
            defaultValue = source.readBoolean();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeBoolean(sendSMS);
            dest.writeBoolean(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<PhoneCallSendSMSDialogPreference.SavedState>() {
                    public PhoneCallSendSMSDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new PhoneCallSendSMSDialogPreference.SavedState(in);
                    }
                    public PhoneCallSendSMSDialogPreference.SavedState[] newArray(int size)
                    {
                        return new PhoneCallSendSMSDialogPreference.SavedState[size];
                    }

                };

    }

}
