package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;

public class MobileCellsRegistrationDialogPreferenceX extends DialogPreference {

    MobileCellsRegistrationDialogPreferenceFragmentX fragment;

    String value;
    private final Context context;

    final int mMin, mMax;
    long event_id;

    //private int mColor = 0;

    public MobileCellsRegistrationDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);

        this.context = context;

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = Integer.toString(PhoneStateScanner.durationForAutoRegistration);
        setSummaryDDP(0);
    }

    void setSummaryDDP(long millisUntilFinished)
    {
        String summary = "";
        boolean started = false;
        if (PhoneStateScanner.enabledAutoRegistration) {
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
        if (fragment != null)
            fragment.setCellNameText(text);
    }

    String getCellNameText() {
        if (fragment != null)
            return  fragment.getCellNameText();
        else
            return null;
    }

}