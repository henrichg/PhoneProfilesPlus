package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;

public class NumberPickerPreference extends DialogPreference {
	
	private String value;
	
	private int mMin, mMax;
    
    private String mMaxExternalKey, mMinExternalKey;

    private NumberPicker mNumberPicker;

    private int mColor = 0;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPickerPreference, 0, 0);

        mMaxExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_minExternal);

        mMax = numberPickerType.getInt(R.styleable.NumberPickerPreference_max, 5);
        mMin = numberPickerType.getInt(R.styleable.NumberPickerPreference_min, 0);

        numberPickerType.recycle();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .callback(callback)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_number_pref_dialog, null);
        onBindDialogView(layout);

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            getEditText().getBackground().setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
        */

        // External values
        if (mMaxExternalKey != null) {
            mMax = getSharedPreferences().getInt(mMaxExternalKey, mMax);
        }
        if (mMinExternalKey != null) {
            mMin = getSharedPreferences().getInt(mMinExternalKey, mMin);
        }

        mNumberPicker = (NumberPicker) layout.findViewById(R.id.number_picker);

        // Initialize state
        mNumberPicker.setMaxValue(mMax);
        mNumberPicker.setMinValue(mMin);
        mNumberPicker.setValue(Integer.valueOf(value));
        mNumberPicker.setWrapSelectorWheel(false);

        mBuilder.customView(layout, false);

        MaterialDialog mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
            mNumberPicker.clearFocus();

            value = String.valueOf(mNumberPicker.getValue());

            if (callChangeListener(value))
            {
                //persistInt(mNumberPicker.getValue());
                persistString(value);
            }
        }
    };

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
        
    }    

}