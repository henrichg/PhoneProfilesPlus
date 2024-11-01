package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsSendSMS extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_send_sms, rootKey);
    }

}
