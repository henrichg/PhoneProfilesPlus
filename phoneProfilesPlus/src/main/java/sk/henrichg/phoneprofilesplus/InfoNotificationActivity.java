package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class InfoNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        // not working on Android 2.3.x
        GUIData.setTheme(this, true, false);
        GUIData.setLanguage(this.getBaseContext());

        FragmentManager fm = getSupportFragmentManager();
        InfoNotificationDialog infoNotificationDialog = new InfoNotificationDialog();
        infoNotificationDialog.show(fm, "info_notification_dialog");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    public static class InfoNotificationDialog extends DialogFragment {

        public InfoNotificationDialog() {
            // Empty constructor required for DialogFragment
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_info_notification_dialog, container);
            getDialog().setTitle(R.string.info_notification_title);

            TextView infoText = (TextView)view.findViewById(R.id.activity_info_notification_dialog_info_text3);
            infoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PhoneProfilesPreferencesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, GlobalData.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "checkbox");
                    getActivity().startActivity(intent);

                    getActivity().finish();
                }
            });
            Button okButton = (Button)view.findViewById(R.id.activity_info_notification_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            return view;
        }
    }

}
