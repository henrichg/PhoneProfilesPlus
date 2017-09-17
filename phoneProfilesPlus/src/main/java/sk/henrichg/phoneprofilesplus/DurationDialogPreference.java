package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class DurationDialogPreference extends DialogPreference {

    private String value;

    private int mMin, mMax;

    private MaterialDialog mDialog;
    private NumberPicker mNumberPickerHours;
    private NumberPicker mNumberPickerMinutes;
    private NumberPicker mNumberPickerSeconds;

    //private int mColor = 0;

    public DurationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = numberPickerType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = numberPickerType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        numberPickerType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                        //.disableDefaultFonts()
                .icon(getDialogIcon())
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .customView(R.layout.activity_duration_pref_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mNumberPickerHours.clearFocus();
                        mNumberPickerMinutes.clearFocus();
                        mNumberPickerSeconds.clearFocus();

                        int hours = mNumberPickerHours.getValue();
                        int minutes = mNumberPickerMinutes.getValue();
                        int seconds = mNumberPickerSeconds.getValue();

                        int iValue = hours * 3600 + minutes * 60 + seconds;
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        value = String.valueOf(iValue);

                        if (callChangeListener(value))
                        {
                            //persistInt(mNumberPicker.getValue());
                            persistString(value);
                            setSummaryDDP();
                        }
                    }
                });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);
        mNumberPickerHours = layout.findViewById(R.id.duration_pref_dlg_hours);
        mNumberPickerMinutes = layout.findViewById(R.id.duration_pref_dlg_minutes);
        mNumberPickerSeconds = layout.findViewById(R.id.duration_pref_dlg_seconds);

        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, tv, true);
        GlobalGUIRoutines.setSeparatorColorForNumberPicker(mNumberPickerHours, tv.data);
        GlobalGUIRoutines.updateTextAttributesForNumberPicker(mNumberPickerHours, 18);
        GlobalGUIRoutines.setSeparatorColorForNumberPicker(mNumberPickerMinutes, tv.data);
        GlobalGUIRoutines.updateTextAttributesForNumberPicker(mNumberPickerMinutes, 18);
        GlobalGUIRoutines.setSeparatorColorForNumberPicker(mNumberPickerSeconds, tv.data);
        GlobalGUIRoutines.updateTextAttributesForNumberPicker(mNumberPickerSeconds, 18);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        minutes = (mMax % 3600) / 60;
        seconds = mMax % 60;
        final String sMax = GlobalGUIRoutines.getDurationString(mMax);
        mNumberPickerHours.setMaxValue(hours);
        if (hours == 0)
            mNumberPickerMinutes.setMaxValue(minutes);
        else
            mNumberPickerMinutes.setMaxValue(59);
        if ((hours == 0) && (minutes == 0))
            mNumberPickerSeconds.setMaxValue(seconds);
        else
            mNumberPickerSeconds.setMaxValue(59);
        final String sMin = GlobalGUIRoutines.getDurationString(mMin);
        mNumberPickerHours.setMinValue(0);
        mNumberPickerMinutes.setMinValue(0);
        mNumberPickerSeconds.setMinValue(0);
        int iValue = Integer.valueOf(value);
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mNumberPickerHours.setValue(hours);
        mNumberPickerMinutes.setValue(minutes);
        mNumberPickerSeconds.setValue(seconds);
        mNumberPickerHours.setWrapSelectorWheel(false);
        mNumberPickerMinutes.setWrapSelectorWheel(false);
        mNumberPickerSeconds.setWrapSelectorWheel(false);

        mTextViewRange.setText(sMin+" - "+sMax);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }
        setSummaryDDP();
    }

    private void setSummaryDDP()
    {
        setSummary(GlobalGUIRoutines.getDurationString(Integer.parseInt(value)));
    }

}