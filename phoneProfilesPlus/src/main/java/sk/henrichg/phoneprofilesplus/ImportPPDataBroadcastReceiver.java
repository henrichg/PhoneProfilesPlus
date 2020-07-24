package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class ImportPPDataBroadcastReceiver  extends BroadcastReceiver {

    boolean importStarted = false;
    boolean importEndeed = false;
    PPApplicationDataForImport applicationData = null;
    int profilesCount = 0;
    List<PPProfileForImport> profiles = null;
    int shortcutsCount = 0;
    List<PPShortcutForImport> shortcuts = null;
    int intentsCount = 0;
    List<PPIntentForImport> intents = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());

        switch (intent.getAction()) {
            case PPApplication.ACTION_EXPORT_PP_DATA_STOP:
                EditorProfilesActivity.importFromPPStopped = true;
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_STARTED:
                importStarted = true;
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_ENDED:
                importEndeed = true;
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES:
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    applicationData = extras.getParcelable(PPApplication.EXTRA_PP_APPLICATION_DATA);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_PROFILES_COUNT:
                extras = intent.getExtras();
                if (extras != null) {
                    profilesCount = extras.getParcelable(PPApplication.EXTRA_PP_PROFILES_COUNT);
                    profiles = new ArrayList<>();
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_PROFILES:
                extras = intent.getExtras();
                if (extras != null) {
                    PPProfileForImport profile = extras.getParcelable(PPApplication.EXTRA_PP_PROFILE_DATA);
                    if (profile != null)
                        profiles.add(profile);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT:
                extras = intent.getExtras();
                if (extras != null) {
                    shortcutsCount = extras.getParcelable(PPApplication.EXTRA_PP_SHORTCUTS_COUNT);
                    shortcuts = new ArrayList<>();
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS:
                extras = intent.getExtras();
                if (extras != null) {
                    PPShortcutForImport shortcut = extras.getParcelable(PPApplication.EXTRA_PP_SHORTCUT_DATA);
                    if (shortcut != null)
                        shortcuts.add(shortcut);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_INTENTS_COUNT:
                extras = intent.getExtras();
                if (extras != null) {
                    intentsCount = extras.getParcelable(PPApplication.EXTRA_PP_INTENTS_COUNT);
                    intents = new ArrayList<>();
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_INTENTS:
                extras = intent.getExtras();
                if (extras != null) {
                    PPIntentForImport ppIntent = extras.getParcelable(PPApplication.EXTRA_PP_INTENT_DATA);
                    if (ppIntent != null)
                        intents.add(ppIntent);
                }
                break;
        }

    }

}
