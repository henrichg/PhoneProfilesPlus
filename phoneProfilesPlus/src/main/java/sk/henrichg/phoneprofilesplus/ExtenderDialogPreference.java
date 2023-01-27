package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class ExtenderDialogPreference extends DialogPreference {

    ExtenderDialogPreferenceFragment fragment;

    final Context _context;

    // Custom xml attributes.
    String installSummary;
    String lauchSummary;

    public ExtenderDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPExtenderDialogPreference);

        installSummary = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_installSummary);
        lauchSummary = typedArray.getString(
                R.styleable.PPExtenderDialogPreference_launchSummary);

        typedArray.recycle();
    }

    @Override
    public void onAttached() {
        setSummaryEDP();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        setSummaryEDP();
    }

    private void setSummaryEDP()
    {
        String prefVolumeDataSummary;

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(_context);
        if (extenderVersion == 0) {
            prefVolumeDataSummary = _context.getString(R.string.extender_pref_dialog_PPPExtender_not_installed_summary);

            if ((installSummary != null) && (!installSummary.isEmpty()))
                prefVolumeDataSummary = prefVolumeDataSummary + "\n\n" + installSummary;
        }
        else {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(_context);
            prefVolumeDataSummary =  _context.getString(R.string.extender_pref_dialog_PPPExtender_installed_summary) +
                    " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
            if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.extender_pref_dialog_PPPExtender_new_version_summary);
            else
                prefVolumeDataSummary = prefVolumeDataSummary + _context.getString(R.string.extender_pref_dialog_PPPExtender_upgrade_summary);
        }

        if ((lauchSummary != null) && (!lauchSummary.isEmpty()))
            prefVolumeDataSummary = prefVolumeDataSummary + "\n\n" + lauchSummary;

//        Log.e("PPPPSDialogPreference.setSummaryPPPPSDP", "xxxxx");
        setSummary(prefVolumeDataSummary);
    }

    /*
    String getSValue() {
        //int _value = value + minimumValue;
        return "";
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryPPPPSDP();
        }
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[0]) == 1;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }
    */

    @Override
    protected Parcelable onSaveInstanceState()
    {
        //savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        //noinspection UnnecessaryLocalVariable
        final ExtenderDialogPreference.SavedState myState = new ExtenderDialogPreference.SavedState(superState);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryEDP();
            return;
        }

        // restore instance state
        ExtenderDialogPreference.SavedState myState = (ExtenderDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());

        setSummaryEDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        SavedState(Parcel source)
        {
            super(source);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<ExtenderDialogPreference.SavedState>() {
                    public ExtenderDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ExtenderDialogPreference.SavedState(in);
                    }
                    public ExtenderDialogPreference.SavedState[] newArray(int size)
                    {
                        return new ExtenderDialogPreference.SavedState[size];
                    }

                };

    }

}
