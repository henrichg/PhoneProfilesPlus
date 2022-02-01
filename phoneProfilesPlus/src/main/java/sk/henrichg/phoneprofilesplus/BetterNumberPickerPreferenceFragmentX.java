package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.math.BigDecimal;

public class BetterNumberPickerPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private PPNumberPicker mNumberPicker;

    private Context context;
    private BetterNumberPickerPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (BetterNumberPickerPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_better_number_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mNumberPicker = view.findViewById(R.id.better_number_picker);

        // Initialize state
        mNumberPicker.setMin(BigDecimal.valueOf(preference.mMin));
        mNumberPicker.setMax(BigDecimal.valueOf(preference.mMax));
        mNumberPicker.setPlusMinusVisibility(View.INVISIBLE);
        mNumberPicker.setDecimalVisibility(View.INVISIBLE);
        //mNumberPicker.setLabelText(getContext().getString(R.string.minutes_label_description));
        mNumberPicker.setNumber(Integer.valueOf(preference.value), null, null);
        if (ApplicationPreferences.applicationTheme(context, true).equals("dark"))
            mNumberPicker.setTheme(R.style.BetterPickersDialogFragment);
        else
            mNumberPicker.setTheme(R.style.BetterPickersDialogFragment_Light);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            boolean persist = true;
            BigDecimal number = mNumberPicker.getEnteredNumber();
            if (isSmaller(number) || isBigger(number)) {
                /*String errorText = context.getString(R.string.number_picker_min_max_error, String.valueOf(preference.mMin), String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                persist = false;
            } else if (isSmaller(number)) {
                /*String errorText = context.getString(R.string.number_picker_min_error, String.valueOf(preference.mMin));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                persist = false;
            } else if (isBigger(number)) {
                /*String errorText = context.getString(R.string.number_picker_max_error, String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();*/
                persist = false;
            }

            if (persist) {
                preference.value = String.valueOf(mNumberPicker.getNumber());
                preference.persistValue();
            }
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

    private boolean isBigger(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(preference.mMax)) > 0;
    }

    private boolean isSmaller(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(preference.mMin)) < 0;
    }

}
