package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class VPNDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements AdapterView.OnItemSelectedListener
{
    private VPNDialogPreference preference;

    private AlertDialog mDialog;
    private AppCompatSpinner vpnApplicationSpinner = null;
    private EditText profileNameEditText = null;
    private EditText tunnelNameEditText = null;
    private TextView profileNameLabel = null;
    private TextView tunnelNameLabel = null;
    private RadioButton enableVPNRBtn = null;
    private RadioButton disableVPNRBtn = null;
    private CheckBox doNotSwith = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preference = (VPNDialogPreference) getPreference();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(preference._context);
        dialogBuilder.setTitle(preference.getTitle());
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(preference._context);
        View layout = inflater.inflate(R.layout.dialog_vpn_preference, null, false);
        dialogBuilder.setView(layout);

        vpnApplicationSpinner = layout.findViewById(R.id.vpnPrefDialogVPNApplication);

        HighlightedSpinnerAdapter vpnApplicationSpinnerAdapter = new HighlightedSpinnerAdapter(
                (ProfilesPrefsActivity) preference._context,
                R.layout.spinner_highlighted,
                getResources().getStringArray(R.array.vpnApplicationArray));
        vpnApplicationSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
        vpnApplicationSpinner.setAdapter(vpnApplicationSpinnerAdapter);
        vpnApplicationSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        vpnApplicationSpinner.setBackgroundTintList(ContextCompat.getColorStateList(preference._context/*getBaseContext()*/, R.color.highlighted_spinner_all));

        enableVPNRBtn = layout.findViewById(R.id.vpnPrefDialogEnableVPNEnableRB);
        enableVPNRBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.enableVPN = enableVPNRBtn.isChecked();
            //preference.callChangeListener(preference.getSValue());
        });
        disableVPNRBtn = layout.findViewById(R.id.vpnPrefDialogEnableVPNDisableRB);
        disableVPNRBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.enableVPN = enableVPNRBtn.isChecked();
            //preference.callChangeListener(preference.getSValue());
        });

        profileNameEditText = layout.findViewById(R.id.vpnPrefDialogProfileName);
        profileNameEditText.setBackgroundTintList(ContextCompat.getColorStateList(preference._context, R.color.highlighted_spinner_all));
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
                enableOKButton();
                //preference.callChangeListener(preference.getSValue());
            }
        });

        tunnelNameEditText = layout.findViewById(R.id.vpnPrefDialogTunnelName);
        tunnelNameEditText.setBackgroundTintList(ContextCompat.getColorStateList(preference._context, R.color.highlighted_spinner_all));
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
                enableOKButton();
                //preference.callChangeListener(preference.getSValue());
            }
        });

        String[] entryValues = getResources().getStringArray(R.array.vpnApplicationValues);
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

        profileNameLabel = layout.findViewById(R.id.vpnPrefDialogProfileNameLabel);
        tunnelNameLabel = layout.findViewById(R.id.vpnPrefDialogTunnelNameLabel);

        doNotSwith = layout.findViewById(R.id.vpnPrefDialogNotSetWhenIsInState);
        doNotSwith.setChecked(preference.doNotSetWhenIsinState);
        doNotSwith.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            preference.doNotSetWhenIsinState = doNotSwith.isChecked();
            //preference.callChangeListener(preference.getSValue());
        });

        vpnApplicationSpinner.setOnItemSelectedListener(this);

        enableViews();

        dialogBuilder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> preference.persistValue());
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        mDialog = dialogBuilder.create();
        mDialog.setOnShowListener(dialog -> enableOKButton());

        return mDialog;
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

    private void enableOKButton() {
        if (mDialog != null) {
            Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            boolean ok = false;
            if (preference.vpnApplication == 0)
                ok = true;
            else
            if (((preference.vpnApplication == 1) ||
                    (preference.vpnApplication == 2) ||
                    (preference.vpnApplication == 3)) &&
                    (!preference.profileName.isEmpty()))
                ok = true;
            else
            if ((preference.vpnApplication == 4) &&
                    (!preference.tunnelName.isEmpty()))
                ok = true;
            okButton.setEnabled(ok);
        }
    }

    private boolean isCompatibleWithOpenVPNConnect() {
        try {
            /*PackageInfo info = */preference._context.getPackageManager().getPackageInfo("net.openvpn.openvpn", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private boolean isCompatibleWithOpenVPNFoAndroid() {
        try {
            /*PackageInfo info = */preference._context.getPackageManager().getPackageInfo("de.blinkt.openvpn", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private boolean isCompatibleWithWireGuard() {
        try {
            PackageInfo info = preference._context.getPackageManager().getPackageInfo("com.wireguard.android", 0);
            return PackageInfoCompat.getLongVersionCode(info) >= 466;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((HighlightedSpinnerAdapter)vpnApplicationSpinner.getAdapter()).setSelection(position);

        String[] vpnApplicationValues = preference._context.getResources().getStringArray(R.array.vpnApplicationValues);
        preference.vpnApplication = Integer.parseInt(vpnApplicationValues[position]);

        if ((preference.vpnApplication == 1) ||
                (preference.vpnApplication == 2)) {
            if (!isCompatibleWithOpenVPNConnect()) {
                PPApplication.showToast(preference._context,
                        preference._context.getString(R.string.vpn_profile_pref_dlg_openvpnconnect_not_comaptible), Toast.LENGTH_LONG);
            }
        }
        else
        if (preference.vpnApplication == 3) {
            if (!isCompatibleWithOpenVPNFoAndroid()) {
                PPApplication.showToast(preference._context,
                        preference._context.getString(R.string.vpn_profile_pref_dlg_openvpnforandroid_not_comaptible), Toast.LENGTH_LONG);
            }
        }
        else
        if (preference.vpnApplication == 4) {
            if (!isCompatibleWithWireGuard()) {
                PPApplication.showToast(preference._context,
                        preference._context.getString(R.string.vpn_profile_pref_dlg_wireguard_not_comaptible), Toast.LENGTH_LONG);
            }
        }

        enableViews();
        enableOKButton();
        //preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void enableViews() {
        if (preference.vpnApplication == 0) {
            enableVPNRBtn.setEnabled(false);
            disableVPNRBtn.setEnabled(false);
            profileNameLabel.setEnabled(false);
            profileNameEditText.setEnabled(false);
            tunnelNameLabel.setEnabled(false);
            tunnelNameEditText.setEnabled(false);
            doNotSwith.setEnabled(false);
        }
        else
        if ((preference.vpnApplication == 1) ||
                (preference.vpnApplication == 2)) {
            if (isCompatibleWithOpenVPNConnect()) {
                enableVPNRBtn.setEnabled(true);
                disableVPNRBtn.setEnabled(true);
                profileNameLabel.setEnabled(true);
                profileNameEditText.setEnabled(true);
                doNotSwith.setEnabled(true);
            } else {
                enableVPNRBtn.setEnabled(false);
                disableVPNRBtn.setEnabled(false);
                profileNameLabel.setEnabled(false);
                profileNameEditText.setEnabled(false);
                doNotSwith.setEnabled(false);
            }
            tunnelNameLabel.setEnabled(false);
            tunnelNameEditText.setEnabled(false);
        }
        else
        if (preference.vpnApplication == 3) {
            if (isCompatibleWithOpenVPNFoAndroid()) {
                enableVPNRBtn.setEnabled(true);
                disableVPNRBtn.setEnabled(true);
                profileNameLabel.setEnabled(true);
                profileNameEditText.setEnabled(true);
                doNotSwith.setEnabled(true);
            } else {
                enableVPNRBtn.setEnabled(false);
                disableVPNRBtn.setEnabled(false);
                profileNameLabel.setEnabled(false);
                profileNameEditText.setEnabled(false);
                doNotSwith.setEnabled(false);
            }
            tunnelNameLabel.setEnabled(false);
            tunnelNameEditText.setEnabled(false);
        }
        else
        if (preference.vpnApplication == 4) {
            if (isCompatibleWithWireGuard()) {
                enableVPNRBtn.setEnabled(true);
                disableVPNRBtn.setEnabled(true);
                profileNameLabel.setEnabled(false);
                profileNameEditText.setEnabled(false);
                tunnelNameLabel.setEnabled(true);
                tunnelNameEditText.setEnabled(true);
                doNotSwith.setEnabled(true);
            } else {
                enableVPNRBtn.setEnabled(false);
                disableVPNRBtn.setEnabled(false);
                profileNameLabel.setEnabled(false);
                profileNameEditText.setEnabled(false);
                tunnelNameLabel.setEnabled(false);
                tunnelNameEditText.setEnabled(false);
                doNotSwith.setEnabled(false);
            }
        }
    }

}
