package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.sql.Date;
import java.util.Calendar;

public class TimePreference extends DialogPreference {
    
    private final Context context;
    private final AttributeSet attributeSet;
    //private Calendar calendar;
    private int value;
    private MaterialDialog mDialog;
    private TimePicker picker = null;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        attributeSet = attrs;
        
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        Calendar now = Calendar.getInstance();
        value = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            picker.clearFocus();

                            value = picker.getCurrentHour() * 60 + picker.getCurrentMinute();

                            setSummary(getSummary());
                            if (callChangeListener(value)) {
                                persistInt(value);
                                notifyChanged();
                            }
                        }
                    }
                });

        /*
        if (PPApplication.applicationTheme.equals("dark"))
            picker = new TimePicker(context, attributeSet, TimePickerDialog.THEME_HOLO_DARK);
        else
            picker = new TimePicker(context, attributeSet, TimePickerDialog.THEME_HOLO_LIGHT);
        */
        picker = new TimePicker(context, attributeSet);
        picker.setIs24HourView(DateFormat.is24HourFormat(context));
        onBindDialogView(picker);

        mBuilder.customView(picker, false);

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        mDialog = mBuilder.build();

        /*
        MDButton negative = mDialog.getActionButton(DialogAction.NEGATIVE);
        if (negative != null) negative.setAllCaps(false);
        MDButton  neutral = mDialog.getActionButton(DialogAction.NEUTRAL);
        if (neutral != null) neutral.setAllCaps(false);
        MDButton  positive = mDialog.getActionButton(DialogAction.POSITIVE);
        if (positive != null) positive.setAllCaps(false);
        */

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(value / 60);
        picker.setCurrentMinute(value % 60);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        Calendar now = Calendar.getInstance();
        if (restoreValue) {
            if (defaultValue == null) {
                value = getPersistedInt(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE));
            } else {
                value = Integer.parseInt(getPersistedString((String) defaultValue));
            }
        } else {
            if (defaultValue == null) {
                value = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            } else {
                value = Integer.parseInt((String) defaultValue);
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, value / 60);
        calendar.set(Calendar.MINUTE, value % 60);
        return DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
    }
} 
