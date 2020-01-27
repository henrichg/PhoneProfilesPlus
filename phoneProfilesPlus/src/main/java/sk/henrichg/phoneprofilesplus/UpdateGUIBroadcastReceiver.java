package sk.henrichg.phoneprofilesplus;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class UpdateGUIBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    static final String EXTRA_REFRESH = "refresh";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### UpdateGUIBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "UpdateGUIBroadcastReceiver.onReceive", "UpdateGUIBroadcastReceiver_onReceive");

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.updateGUI", "ActivateProfileHelper.lockRefresh=" + ActivateProfileHelper.lockRefresh);
            PPApplication.logE("ActivateProfileHelper.updateGUI", "doImport=" + EditorProfilesActivity.doImport);
            PPApplication.logE("ActivateProfileHelper.updateGUI", "alsoEditor=" + alsoEditor);
            PPApplication.logE("ActivateProfileHelper.updateGUI", "refresh=" + refresh);
        }*/

        boolean refresh = intent.getBooleanExtra(EXTRA_REFRESH, true);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);

        if (!refresh) {
            if (ActivateProfileHelper.lockRefresh || EditorProfilesActivity.doImport)
                // no refresh widgets
                return;
        }

        long now = SystemClock.elapsedRealtime();

        if ((now - PPApplication.lastRefreshOfGUI) >= PPApplication.DURATION_FOR_GUI_REFRESH) {
            //PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "refresh");

            // icon widget
            try {
                int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
                IconWidgetProvider myWidget = new IconWidgetProvider();
                myWidget.refreshWidget = refresh;
                myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } catch (Exception ignored) {
            }

            // one row widget
            try {
                int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
                OneRowWidgetProvider myWidget = new OneRowWidgetProvider();
                myWidget.refreshWidget = refresh;
                myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } catch (Exception ignored) {
            }

            // list widget
            try {
                ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
                myWidget.updateWidgets(context, refresh);
            } catch (Exception ignored) {
            }

            // Samsung edge panel
            if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
                try {
                    SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                    myWidget.updateWidgets(context, refresh);
                } catch (Exception ignored) {
                }
            }

            // dash clock extension
            Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
            intent3.putExtra(DashClockBroadcastReceiver.EXTRA_REFRESH, refresh);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

            // activities
            Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
            intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, refresh);
            intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, refreshAlsoEditor);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);
        }
        //else {
        //    PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "do not refresh");
        //}

        PPApplication.lastRefreshOfGUI = SystemClock.elapsedRealtime();

    }

}
