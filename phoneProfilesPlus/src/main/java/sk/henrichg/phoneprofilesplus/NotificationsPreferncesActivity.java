package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationsPreferncesActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        activityStarted = true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (activityStarted) {
            if (ApplicationPreferences.notificationProfileListDisplayNotification) {
                GlobalGUIRoutines.setTheme(this, true, true, false, false, false, false);

                SingleSelectListDialog dialog = new SingleSelectListDialog(
                        false,
                        getString(R.string.notifications_preferences_notification_type),
                        null,
                        R.array.notificationPreferencesNotificationTypeArray,
                        SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                        (dialog1, which) -> {
                            switch (which) {
                                case 0:
                                    try {
                                        Intent intent = new Intent(this, PhoneProfilesPrefsActivity.class);
                                        intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_APP_NOTIFICATION_CATEGORY_ROOT);
                                        //noinspection deprecation
                                        startActivityForResult(intent, 100);
                                    } catch (Exception e) {
                                        finish();
                                    }
                                    break;
                                case 1:
                                    try {
                                        Intent intent = new Intent(this, PhoneProfilesPrefsActivity.class);
                                        intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PROFILE_LIST_NOTIFICATIONLIST_CATEGORY_ROOT);
                                        //noinspection deprecation
                                        startActivityForResult(intent, 100);
                                    } catch (Exception e) {
                                        finish();
                                    }
                                    break;
                                default:
                            }
                        },
                        dialog12 -> finish(),
                        //false,
                        this);

                if (!isFinishing())
                    dialog.showDialog();

            } else {
                try {
                    Intent intent = new Intent(this, PhoneProfilesPrefsActivity.class);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_APP_NOTIFICATION_CATEGORY_ROOT);
                    //noinspection deprecation
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    finish();
                }
            }
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
