package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

public class MobileCellsRegistrationDialogPreferenceX extends DialogPreference {

    MobileCellsRegistrationDialogPreferenceFragmentX fragment;

    String value;
    String cellName;

    private final Context context;

    final int mMin, mMax;
    long event_id;

    //private int mColor = 0;

    public MobileCellsRegistrationDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.PPDurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.PPDurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.PPDurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);

        this.context = context;

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = Integer.toString(MobileCellsScanner.durationForAutoRegistration);
        setSummaryDDP(0);
    }

    void setSummaryDDP(long millisUntilFinished)
    {
        String summary = "";
        boolean started = false;
        if (MobileCellsScanner.enabledAutoRegistration) {
            if (millisUntilFinished > 0) {
                summary = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_started);
                String time = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
                long iValue = millisUntilFinished / 1000;
                time = time + ": " + GlobalGUIRoutines.getDurationString((int)iValue);
                summary = summary + "; " + time;
                started = true;
            }
        }
        if (!started) {
            summary = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
            String newCount = context.getString(R.string.mobile_cells_registration_pref_dlg_status_new_cells_count);
            long iValue = DatabaseHandler.getInstance(context.getApplicationContext()).getNewMobileCellsCount();
            newCount = newCount + " " + iValue;
            summary = summary + "; " + newCount;
        }

        setSummary(summary);
    }

    @SuppressWarnings("SameParameterValue")
    void updateInterface(long millisUntilFinished, boolean forceStop) {
        if (fragment != null)
            fragment.updateInterface(millisUntilFinished, forceStop);
    }

    void startRegistration() {
        if (fragment != null)
            fragment.startRegistration();
    }

    void setCellNameText(String text) {
        //PPApplication.logE("MobileCellsRegistrationDialogPreferenceX.setCellNameText", "text="+text);
        cellName = text;
        if (fragment != null)
            fragment.setCellNameText(text);
    }

    String getCellNameText() {
        if (fragment != null)
            return  fragment.getCellNameText();
        else
            return null;
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final MobileCellsRegistrationDialogPreferenceX.SavedState myState = new MobileCellsRegistrationDialogPreferenceX.SavedState(superState);
        myState.value = value;
        myState.cellName = cellName;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //PPApplication.logE("MobileCellsRegistrationDialogPreferenceX.onCreateDialog", "xxx");

        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(MobileCellsRegistrationDialogPreferenceX.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        MobileCellsRegistrationDialogPreferenceX.SavedState myState = (MobileCellsRegistrationDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        cellName = myState.cellName;

        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String cellName;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            cellName = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(cellName);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<MobileCellsRegistrationDialogPreferenceX.SavedState> CREATOR =
                new Creator<MobileCellsRegistrationDialogPreferenceX.SavedState>() {
                    public MobileCellsRegistrationDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new MobileCellsRegistrationDialogPreferenceX.SavedState(in);
                    }
                    public MobileCellsRegistrationDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new MobileCellsRegistrationDialogPreferenceX.SavedState[size];
                    }

                };

    }
}