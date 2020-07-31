package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import sk.henrichg.phoneprofiles.PPApplicationDataForExport;
import sk.henrichg.phoneprofiles.PPIntentForExport;
import sk.henrichg.phoneprofiles.PPProfileForExport;
import sk.henrichg.phoneprofiles.PPShortcutForExport;

public class ImportPPDataBroadcastReceiver  extends BroadcastReceiver {

    boolean importStarted = false;
    boolean importEndeed = false;
    PPApplicationDataForExport applicationData = null;
    //int profilesCount = 0;
    List<PPProfileForExport> profiles = null;
    //int shortcutsCount = 0;
    List<PPShortcutForExport> shortcuts = null;
    //int intentsCount = 0;
    List<PPIntentForExport> intents = null;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] ImportPPDataBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());

        //final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        switch (intent.getAction()) {
            case PPApplication.ACTION_EXPORT_PP_DATA_STOP_FROM_PP:
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
                    //profilesCount = extras.getInt(PPApplication.EXTRA_PP_PROFILES_COUNT);
                    profiles = new ArrayList<>();
                    //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "profilesCount="+profilesCount);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_PROFILES:
                extras = intent.getExtras();
                if (extras != null) {
                    PPProfileForExport profile = extras.getParcelable(PPApplication.EXTRA_PP_PROFILE_DATA);
                    if (profile != null) {
                        profiles.add(profile);
                        //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "profile.KEY_NAME=" + profile.KEY_NAME);
                    }
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT:
                extras = intent.getExtras();
                if (extras != null) {
                    //shortcutsCount = extras.getInt(PPApplication.EXTRA_PP_SHORTCUTS_COUNT);
                    shortcuts = new ArrayList<>();
                    //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "shortcutsCount="+shortcutsCount);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS:
                extras = intent.getExtras();
                if (extras != null) {
                    PPShortcutForExport shortcut = extras.getParcelable(PPApplication.EXTRA_PP_SHORTCUT_DATA);
                    if (shortcut != null) {
                        shortcuts.add(shortcut);
                        //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "shortcut.KEY_S_NAME="+shortcut.KEY_S_NAME);
                    }
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_INTENTS_COUNT:
                extras = intent.getExtras();
                if (extras != null) {
                    //intentsCount = extras.getInt(PPApplication.EXTRA_PP_INTENTS_COUNT);
                    intents = new ArrayList<>();
                    //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "intentsCount="+intentsCount);
                }
                break;
            case PPApplication.ACTION_EXPORT_PP_DATA_INTENTS:
                extras = intent.getExtras();
                if (extras != null) {
                    PPIntentForExport ppIntent = extras.getParcelable(PPApplication.EXTRA_PP_INTENT_DATA);
                    if (ppIntent != null) {
                        intents.add(ppIntent);
                        //PPApplication.logE("ImportPPDataBroadcastReceiver.onReceive", "ppIntent.KEY_IN_NAME="+ppIntent.KEY_IN_NAME);
                    }
                }
                break;
        }

    }

}
