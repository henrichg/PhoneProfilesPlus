package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PhoneCallSendSMSDialogPreferenceFragment extends PreferenceDialogFragmentCompat
{

    private PhoneCallSendSMSDialogPreference preference;

    SwitchCompat sendSMSChBox;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        preference = (PhoneCallSendSMSDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_phone_calls_send_sms, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        sendSMSChBox = view.findViewById(R.id.phone_calls_send_sms_checkBox);
        sendSMSChBox.setChecked(preference.sendSMS);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.sendSMS = sendSMSChBox.isChecked();
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }
        preference.fragment = null;
    }

}
