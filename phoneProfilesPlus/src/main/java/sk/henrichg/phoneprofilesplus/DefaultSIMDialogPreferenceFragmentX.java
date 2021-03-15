package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class DefaultSIMDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context context;
    private DefaultSIMDialogPreferenceX preference;

    // Layout widgets.
    private AppCompatSpinner voiceSpinner = null;
    private AppCompatSpinner smsSpinner = null;
    private AppCompatSpinner dataSpinner = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (DefaultSIMDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_default_sim_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        voiceSpinner = view.findViewById(R.id.default_sim_voice_spinner);
        smsSpinner = view.findViewById(R.id.default_sim_sms_spinner);
        dataSpinner = view.findViewById(R.id.default_sim_data_spinner);

        GlobalGUIRoutines.HighlightedSpinnerAdapter voiceSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                (ProfilesPrefsActivity)context,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.defaultSIMVoiceArray));
        voiceSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        voiceSpinner.setAdapter(voiceSpinnerAdapter);
        voiceSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        voiceSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner));
        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)voiceSpinner.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        voiceSpinner.setSelection(preference.voiceValue);

        GlobalGUIRoutines.HighlightedSpinnerAdapter smsSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                (ProfilesPrefsActivity)context,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.defaultSIMSMSArray));
        smsSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        smsSpinner.setAdapter(smsSpinnerAdapter);
        smsSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        smsSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner));
        smsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)smsSpinner.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        smsSpinner.setSelection(preference.smsValue);

        GlobalGUIRoutines.HighlightedSpinnerAdapter dataSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                (ProfilesPrefsActivity)context,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.defaultSIMDataArray));
        dataSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        dataSpinner.setAdapter(dataSpinnerAdapter);
        dataSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        dataSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner));
        dataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)dataSpinner.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dataSpinner.setSelection(preference.dataValue);

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

}
