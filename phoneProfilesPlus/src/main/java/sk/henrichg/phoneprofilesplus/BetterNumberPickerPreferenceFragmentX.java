package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import java.math.BigDecimal;

import androidx.preference.PreferenceDialogFragmentCompat;

public class BetterNumberPickerPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private PPNumberPicker mNumberPicker;

    private Context context;
    private BetterNumberPickerPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (BetterNumberPickerPreferenceX) getPreference();

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_better_number_pref_dialog, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
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
            BigDecimal number = mNumberPicker.getEnteredNumber();
            if (isSmaller(number) || isBigger(number)) {
                String errorText = context.getString(R.string.number_picker_min_max_error, String.valueOf(preference.mMin), String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();
                return;
            } else if (isSmaller(number)) {
                String errorText = context.getString(R.string.number_picker_min_error, String.valueOf(preference.mMin));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();
                return;
            } else if (isBigger(number)) {
                String errorText = context.getString(R.string.number_picker_max_error, String.valueOf(preference.mMax));
                mNumberPicker.getErrorView().setText(errorText);
                mNumberPicker.getErrorView().show();
                return;
            }

            preference.value = String.valueOf(mNumberPicker.getNumber());
            preference.persistValue();
        }
    }

    private boolean isBigger(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(preference.mMax)) > 0;
    }

    private boolean isSmaller(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(preference.mMin)) < 0;
    }

}
