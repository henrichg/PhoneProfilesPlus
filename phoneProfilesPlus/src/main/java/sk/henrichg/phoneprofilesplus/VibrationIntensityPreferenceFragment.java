package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class VibrationIntensityPreferenceFragment extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener
{

    //private Context context;
    private VibrationIntensityPreference preference;

    private SeekBar seekBar = null;
    private TextView valueText = null;
    //private CheckBox sharedProfileChBox = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        //this.context = context;
        preference = (VibrationIntensityPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_vibration_intensity_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        CheckBox noChangeChBox = view.findViewById(R.id.volumePrefDialogNoChange);

        seekBar = view.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = view.findViewById(R.id.volumePrefDialogValueText);

        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue/* - preference.minimumValue*/);
        seekBar.setProgress(preference.value);

        valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

        if (noChangeChBox != null) {
            noChangeChBox.setVisibility(View.VISIBLE);
            noChangeChBox.setChecked((preference.noChange == 1));
        }

        valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
        seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);

        seekBar.setOnSeekBarChangeListener(this);
        if (noChangeChBox != null)
            noChangeChBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.volumePrefDialogNoChange) {
            preference.noChange = (isChecked) ? 1 : 0;

            valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);

        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // Round the value to the closest integer value.
            //noinspection ConstantConditions
            if (preference.stepSize >= 1) {
                preference.value = Math.round((float) progress / preference.stepSize) * preference.stepSize;
            } else {
                preference.value = progress;
            }

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
