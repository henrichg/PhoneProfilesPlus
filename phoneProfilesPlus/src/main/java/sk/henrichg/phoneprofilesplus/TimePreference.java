package sk.henrichg.phoneprofilesplus;

import java.sql.Date;
import java.util.Calendar;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
    
	private Context context;
	private AttributeSet attributeSet;
	private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        context = ctxt;
        attributeSet = attrs;
        
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        calendar = Calendar.getInstance();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(context, attributeSet);
        //String clockType = android.provider.Settings.System.getString(
        //						context.getContentResolver(), 
        //						android.provider.Settings.System.TIME_12_24);
        //String clockType  = Settings.System.getString(context.getContentResolver(), 
        //		                           Settings.System.TIME_12_24);
        //if (clockTtype = null)
        //{
        //	 DateFormat.is24HourFormat(context);
        //}
        //Log.e("TimePreference.onCreateDialogView", "clockType="+clockType);
        //Log.e("TimePreference.onCreateDialogView", "is24Hour="+DateFormat.is24HourFormat(context));
        picker.setIs24HourView(DateFormat.is24HourFormat(context));
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
        	picker.clearFocus();
        	
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
        }
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
