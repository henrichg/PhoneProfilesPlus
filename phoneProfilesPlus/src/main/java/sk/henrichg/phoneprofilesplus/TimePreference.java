package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.sql.Date;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;

public class TimePreference extends DialogPreference {
    
    private final Context context;
    //private Calendar calendar;
    private int value;
    private AlertDialog mDialog;
    private TimePicker picker = null;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        Calendar now = Calendar.getInstance();
        value = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    }

    protected void showDialog(Bundle state) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_time_pref_dialog, null);
        dialogBuilder.setView(layout);

        picker = layout.findViewById(R.id.time_pref_dlg_timePicker);
        picker.setIs24HourView(DateFormat.is24HourFormat(context));
        onBindDialogView(picker);

        mDialog = dialogBuilder.create();

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)context).isFinishing())
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
