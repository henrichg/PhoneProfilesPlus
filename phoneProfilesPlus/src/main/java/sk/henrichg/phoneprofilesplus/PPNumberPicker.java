package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.codetroopers.betterpickers.numberpicker.NumberPicker;

import java.math.BigDecimal;

public class PPNumberPicker extends NumberPicker {

    public PPNumberPicker(Context context) {
        super(context);
    }

    public PPNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void updateNumber() {
        super.updateNumber();
        PPApplication.logE("PPNumberPicker.updateNumber", "xxx");

        BigDecimal number = getEnteredNumber();

        if ((number == null) || (mMinNumber == null) || (mMaxNumber == null))
            return;

        if (isSmaller(number) || isBigger(number)) {
            String errorText = mContext.getString(R.string.number_picker_min_max_error, String.valueOf(mMinNumber), String.valueOf(mMaxNumber));
            getErrorView().setText(errorText);
            getErrorView().show();
        } else if (isSmaller(number)) {
            String errorText = mContext.getString(R.string.number_picker_min_error, String.valueOf(mMinNumber));
            getErrorView().setText(errorText);
            getErrorView().show();
        } else if (isBigger(number)) {
            String errorText = mContext.getString(R.string.number_picker_max_error, String.valueOf(mMaxNumber));
            getErrorView().setText(errorText);
            getErrorView().show();
        } else {
            getErrorView().hideImmediately();
        }
    }

    private boolean isBigger(BigDecimal number) {
        return number.compareTo(mMaxNumber) > 0;
    }

    private boolean isSmaller(BigDecimal number) {
        return number.compareTo(mMinNumber) < 0;
    }

}
