package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.codetroopers.betterpickers.numberpicker.NumberPicker;

import java.math.BigDecimal;

import androidx.appcompat.app.AlertDialog;

class BetterNumberPickerPreference extends DialogPreference {

    private String value;

    private int mMin, mMax;

    private final String mMaxExternalKey, mMinExternalKey;

    private AlertDialog mDialog;
    private NumberPicker mNumberPicker;

    private final Context context;

    public BetterNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.BetterNumberPickerPreference, 0, 0);

        mMaxExternalKey = numberPickerType.getString(R.styleable.BetterNumberPickerPreference_maxExternal);
        mMinExternalKey = numberPickerType.getString(R.styleable.BetterNumberPickerPreference_minExternal);

        mMax = numberPickerType.getInt(R.styleable.BetterNumberPickerPreference_max, 5);
        mMin = numberPickerType.getInt(R.styleable.BetterNumberPickerPreference_min, 0);

        numberPickerType.recycle();
    }

    @Override
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
                BigDecimal number = mNumberPicker.getEnteredNumber();
                if (isSmaller(number) || isBigger(number)) {
                    String errorText = context.getString(R.string.number_picker_min_max_error, String.valueOf(mMin), String.valueOf(mMax));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();
                    return;
                } else if (isSmaller(number)) {
                    String errorText = context.getString(R.string.number_picker_min_error, String.valueOf(mMin));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();
                    return;
                } else if (isBigger(number)) {
                    String errorText = context.getString(R.string.number_picker_max_error, String.valueOf(mMax));
                    mNumberPicker.getErrorView().setText(errorText);
                    mNumberPicker.getErrorView().show();
                    return;
                }

                value = String.valueOf(mNumberPicker.getNumber());

                if (callChangeListener(value))
                {
                    persistString(value);
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_better_number_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        // External values
        if (mMaxExternalKey != null) {
            mMax = getSharedPreferences().getInt(mMaxExternalKey, mMax);
        }
        if (mMinExternalKey != null) {
            mMin = getSharedPreferences().getInt(mMinExternalKey, mMin);
        }

        //noinspection ConstantConditions
        mNumberPicker = layout.findViewById(R.id.better_number_picker);

        // Initialize state
        mNumberPicker.setMin(BigDecimal.valueOf(mMin));
        mNumberPicker.setMax(BigDecimal.valueOf(mMax));
        mNumberPicker.setPlusMinusVisibility(View.INVISIBLE);
        mNumberPicker.setDecimalVisibility(View.INVISIBLE);
        //mNumberPicker.setLabelText(getContext().getString(R.string.minutes_label_description));
        mNumberPicker.setNumber(Integer.valueOf(value), null, null);
        if (ApplicationPreferences.applicationTheme(context, true).equals("dark"))
            mNumberPicker.setTheme(R.style.BetterPickersDialogFragment);
        else
            mNumberPicker.setTheme(R.style.BetterPickersDialogFragment_Light);

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

    private boolean isBigger(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(mMax)) > 0;
    }

    private boolean isSmaller(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(mMin)) < 0;
    }

}
