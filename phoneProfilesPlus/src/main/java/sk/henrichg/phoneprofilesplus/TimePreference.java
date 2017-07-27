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
    
    private Context context;
    private AttributeSet attributeSet;
    private Calendar calendar;
    private MaterialDialog mDialog;
    private TimePicker picker = null;

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        context = ctxt;
        attributeSet = attrs;
        
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        calendar = Calendar.getInstance();
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

                            //noinspection deprecation
                            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
                            //noinspection deprecation
                            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

                            setSummary(getSummary());
                            if (callChangeListener(calendar.getTimeInMillis())) {
                                persistLong(calendar.getTimeInMillis());
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

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        mDialog = mBuilder.build();
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
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        //noinspection deprecation
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        //noinspection deprecation
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
    }
} 
