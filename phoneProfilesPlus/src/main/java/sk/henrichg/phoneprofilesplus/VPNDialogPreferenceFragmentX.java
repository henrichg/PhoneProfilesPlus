package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class VPNDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements AdapterView.OnItemSelectedListener
{

    private Context context;
    private VPNDialogPreferenceX preference;

    private AppCompatSpinner vpnApplicationSpinner = null;
    private EditText profileNameEditText = null;
    private EditText tunnelNameEditText = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (VPNDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_vpn_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        vpnApplicationSpinner = view.findViewById(R.id.vpnPrefDialogVPNApplication);

        GlobalGUIRoutines.HighlightedSpinnerAdapter vpnApplicationSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                (ProfilesPrefsActivity) context,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.vpnApplicationArray));
        vpnApplicationSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        vpnApplicationSpinner.setAdapter(vpnApplicationSpinnerAdapter);
        vpnApplicationSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        vpnApplicationSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner_all));

        RadioButton enableVPNRBtn = view.findViewById(R.id.vpnPrefDialogEnableVPNEnableRB);
        enableVPNRBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.enableVPN = true;
            preference.callChangeListener(preference.getSValue());
        });
        RadioButton disableVPNRBtn = view.findViewById(R.id.vpnPrefDialogEnableVPNDisableRB);
        disableVPNRBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.enableVPN = false;
            preference.callChangeListener(preference.getSValue());
        });

        profileNameEditText = view.findViewById(R.id.vpnPrefDialogProfileName);
        profileNameEditText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.highlighted_spinner_all));
        profileNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                preference.profileName = profileNameEditText.getText().toString();
                preference.callChangeListener(preference.getSValue());
            }
        });

        tunnelNameEditText = view.findViewById(R.id.vpnPrefDialogTunnelName);
        tunnelNameEditText.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.highlighted_spinner_all));
        tunnelNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                preference.tunnelName = tunnelNameEditText.getText().toString();
                preference.callChangeListener(preference.getSValue());
            }
        });

        String[] entryValues = getResources().getStringArray(R.array.volumesSensorOperatorValues);
        int vpnApplicationIdx = 0;
        for (String entryValue : entryValues) {
            if (entryValue.equals(String.valueOf(preference.vpnApplication))) {
                break;
            }
            ++vpnApplicationIdx;
        }
        vpnApplicationSpinner.setSelection(vpnApplicationIdx);

        enableVPNRBtn.setChecked(preference.enableVPN);
        disableVPNRBtn.setChecked(!preference.enableVPN);

        profileNameEditText.setText(preference.profileName);
        tunnelNameEditText.setText(preference.tunnelName);

        vpnApplicationSpinner.setOnItemSelectedListener(this);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((GlobalGUIRoutines.HighlightedSpinnerAdapter)vpnApplicationSpinner.getAdapter()).setSelection(position);

        String[] vpnApplicationValues = context.getResources().getStringArray(R.array.vpnApplicationValues);
        preference.vpnApplication = Integer.parseInt(vpnApplicationValues[position]);
        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
