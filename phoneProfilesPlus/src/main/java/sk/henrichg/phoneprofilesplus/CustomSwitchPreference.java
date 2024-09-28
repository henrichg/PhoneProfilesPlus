package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

/** @noinspection unused*/
public class CustomSwitchPreference extends SwitchPreferenceCompat {

    //private SwitchCompat switchControl;

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        SwitchCompat switchControl = (SwitchCompat) holder.findViewById(R.id.switchControl);
        switchControl.setChecked(isChecked());
        switchControl.setOnClickListener(v -> {
            setChecked(!isChecked());
            persistBoolean(isChecked());
        });
    }

}
