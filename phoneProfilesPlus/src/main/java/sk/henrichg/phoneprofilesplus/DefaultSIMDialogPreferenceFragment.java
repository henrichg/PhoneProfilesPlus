package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.preference.PreferenceDialogFragmentCompat;

public class DefaultSIMDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context context;
    private DefaultSIMDialogPreference preference;

    // Layout widgets.
    private AppCompatSpinner voiceSpinner = null;
    private AppCompatSpinner smsSpinner = null;
    private AppCompatSpinner dataSpinner = null;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (DefaultSIMDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_default_sim_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        TextView text = view.findViewById(R.id.default_sim_voice_textView);
        //noinspection DataFlowIssue
        text.setText(getString(R.string.default_sim_pref_dlg_voice)+":");
        text = view.findViewById(R.id.default_sim_sms_textView);
        //noinspection DataFlowIssue
        text.setText(getString(R.string.default_sim_pref_dlg_sms)+":");
        text = view.findViewById(R.id.default_sim_data_textView);
        //noinspection DataFlowIssue
        text.setText(getString(R.string.default_sim_pref_dlg_data)+":");

        voiceSpinner = view.findViewById(R.id.default_sim_voice_spinner);
        smsSpinner = view.findViewById(R.id.default_sim_sms_spinner);
        dataSpinner = view.findViewById(R.id.default_sim_data_spinner);

        /*int transactionCodeVoice = -1;
        int transactionCodeSMS = -1;
        int transactionCodeData = -1;
        Object serviceManager;
        synchronized (PPApplication.rootMutex) {
            serviceManager = PPApplication.rootMutex.serviceManagerIsub;
        }
        if (serviceManager != null) {
            // support for only data devices
            synchronized (PPApplication.rootMutex) {
                transactionCodeVoice = PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId;
                transactionCodeSMS = PPApplication.rootMutex.transactionCode_setDefaultSmsSubId;
                transactionCodeData = PPApplication.rootMutex.transactionCode_setDefaultDataSubId;
//            PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.onBindDialogView", "transactionCodeVoice="+transactionCodeVoice);
//            PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.onBindDialogView", "transactionCodeSMS="+transactionCodeSMS);
//            PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.onBindDialogView", "transactionCodeData="+transactionCodeData);
            }
        }*/

        //preference.dualSIMSupported = false;

        //if (transactionCodeVoice != -1) {
            PPSpinnerAdapter voiceSpinnerAdapter = new PPSpinnerAdapter(
                    (ProfilesPrefsActivity) context,
                    R.layout.ppp_spinner,
                    getResources().getStringArray(R.array.defaultSIMVoiceArray));
            voiceSpinnerAdapter.setDropDownViewResource(R.layout.ppp_spinner_dropdown);
            voiceSpinner.setAdapter(voiceSpinnerAdapter);
            voiceSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
//            voiceSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.spinner_control_color));
            voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((PPSpinnerAdapter) voiceSpinner.getAdapter()).setSelection(position);
//                    PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.voiceSpinner.onItemSelected", "position="+position);
                    preference.voiceValue = position;
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            if ((voiceSpinner.getAdapter() == null) || (voiceSpinner.getAdapter().getCount() <= preference.voiceValue))
                preference.voiceValue = 0;
            voiceSpinner.setSelection(preference.voiceValue);
        /*}
        else {
            TextView textView = view.findViewById(R.id.default_sim_voice_textView);
            textView.setVisibility(View.GONE);
            voiceSpinner.setVisibility(View.GONE);
        }*/

        //if (transactionCodeSMS != -1) {
            PPSpinnerAdapter smsSpinnerAdapter = new PPSpinnerAdapter(
                    (ProfilesPrefsActivity) context,
                    R.layout.ppp_spinner,
                    getResources().getStringArray(R.array.defaultSIMSMSArray));
            smsSpinnerAdapter.setDropDownViewResource(R.layout.ppp_spinner_dropdown);
            smsSpinner.setAdapter(smsSpinnerAdapter);
            smsSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
//            smsSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.spinner_control_color));
            smsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((PPSpinnerAdapter) smsSpinner.getAdapter()).setSelection(position);
//                    PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.smsSpinner.onItemSelected", "position="+position);
                    preference.smsValue = position;
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            if ((smsSpinner.getAdapter() == null) || (smsSpinner.getAdapter().getCount() <= preference.smsValue))
                preference.smsValue = 0;
            smsSpinner.setSelection(preference.smsValue);
        /*}
        else {
            TextView textView = view.findViewById(R.id.default_sim_sms_textView);
            textView.setVisibility(View.GONE);
            smsSpinner.setVisibility(View.GONE);
        }*/

        //if (transactionCodeData != -1) {
            PPSpinnerAdapter dataSpinnerAdapter = new PPSpinnerAdapter(
                    (ProfilesPrefsActivity) context,
                    R.layout.ppp_spinner,
                    getResources().getStringArray(R.array.defaultSIMDataArray));
            dataSpinnerAdapter.setDropDownViewResource(R.layout.ppp_spinner_dropdown);
            dataSpinner.setAdapter(dataSpinnerAdapter);
            dataSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
//            dataSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.spinner_control_color));
            dataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((PPSpinnerAdapter) dataSpinner.getAdapter()).setSelection(position);
//                    PPApplicationStatic.logE("[DEFAULT_SIM] DefaultSIMDialogPreferenceFragment.dataSpinner.onItemSelected", "position="+position);
                    preference.dataValue = position;
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            if ((dataSpinner.getAdapter() == null) || (dataSpinner.getAdapter().getCount() <= preference.dataValue))
                preference.dataValue = 0;
            dataSpinner.setSelection(preference.dataValue);
        /*}
        else {
            TextView textView = view.findViewById(R.id.default_sim_data_textView);
            textView.setVisibility(View.GONE);
            dataSpinner.setVisibility(View.GONE);
        }*/

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
