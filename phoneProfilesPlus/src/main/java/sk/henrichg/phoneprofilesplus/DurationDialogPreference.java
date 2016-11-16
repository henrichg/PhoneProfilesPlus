package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;

public class DurationDialogPreference extends DialogPreference {

    private String value;

    private int mMin, mMax;

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

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_duration_pref_dialog, null);
        onBindDialogView(layout);

        TextView mTextViewRange = (TextView) layout.findViewById(R.id.duration_pref_dlg_range);
        mNumberPickerHours = (NumberPicker) layout.findViewById(R.id.duration_pref_dlg_hours);
        mNumberPickerMinutes = (NumberPicker) layout.findViewById(R.id.duration_pref_dlg_minutes);
        mNumberPickerSeconds = (NumberPicker) layout.findViewById(R.id.duration_pref_dlg_seconds);

        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, tv, true);
        GUIData.setSeparatorColorForNumberPicker(mNumberPickerHours, tv.data);
        GUIData.updateTextAttributesForNumberPicker(mNumberPickerHours, 18);
        GUIData.setSeparatorColorForNumberPicker(mNumberPickerMinutes, tv.data);
        GUIData.updateTextAttributesForNumberPicker(mNumberPickerMinutes, 18);
        GUIData.setSeparatorColorForNumberPicker(mNumberPickerSeconds, tv.data);
        GUIData.updateTextAttributesForNumberPicker(mNumberPickerSeconds, 18);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        minutes = (mMax % 3600) / 60;
        seconds = mMax % 60;
        final String sMax = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        mNumberPickerHours.setMaxValue(hours);
        if (hours == 0)
            mNumberPickerMinutes.setMaxValue(minutes);
        else
            mNumberPickerMinutes.setMaxValue(59);
        if ((hours == 0) && (minutes == 0))
            mNumberPickerSeconds.setMaxValue(seconds);
        else
            mNumberPickerSeconds.setMaxValue(59);
        hours = mMin / 3600;
        minutes = (mMin % 3600) / 60;
        seconds = mMin % 60;
        final String sMin = String.format("%02d:%02d:%02d", hours, minutes, seconds);
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

        mBuilder.customView(layout, false);

        MaterialDialog mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
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
        int iValue = Integer.parseInt(value);
        int hours = iValue / 3600;
        int minutes = (iValue % 3600) / 60;
        int seconds = iValue % 60;
        setSummary(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

}