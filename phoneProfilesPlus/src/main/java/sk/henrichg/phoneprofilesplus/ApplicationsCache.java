package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationsCache {

    private class SortList implements Comparator<Application> {

        public int compare(Application lhs, Application rhs) {
            return GUIData.collator.compare(lhs.appLabel, rhs.appLabel);
        }

    }

    private ArrayList<Application> applicationsList;
    private ArrayList<Application> applicationsNoShortcutsList;
    private boolean cached;
    private boolean cancelled;

    public ApplicationsCache()
    {
        applicationsList = new ArrayList<Application>();
        applicationsNoShortcutsList = new ArrayList<Application>();
        cached = false;
    }

    public void getApplicationsList(Context context)
    {
        if (cached) return;

        cancelled = false;

        applicationsList.clear();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);

        for (int i = 0; i < packs.size(); i++)
        {
            PackageInfo packageInfo = packs.get(i);

            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null)
            {
                Application newInfo = new Application();

                newInfo.shortcut = false;
                newInfo.appLabel = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                newInfo.packageName = packageInfo.packageName;
                //newInfo.versionName = packageInfo.versionName;
                //newInfo.versionCode = packageInfo.versionCode;
                newInfo.icon = packageInfo.applicationInfo.loadIcon(packageManager);

                applicationsList.add(newInfo);
                applicationsNoShortcutsList.add(newInfo);
            }

            if (cancelled)
                return;
        }

        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(shortcutsIntent, PackageManager.MATCH_ALL);
        //Log.d("ApplicationsCache.getApplicationsList", "shortcuts.size="+shortcuts.size());
        for (int i = 0; i < shortcuts.size(); i++)
        {
            ResolveInfo shortcutInfo = shortcuts.get(i);

            //Log.d("ApplicationsCache.getApplicationsList", "shortcutInfo="+shortcutInfo);
            //Log.d("ApplicationsCache.getApplicationsList", "packageName="+shortcutInfo.activityInfo.packageName);
            //Log.d("ApplicationsCache.getApplicationsList", "name="+shortcutInfo.activityInfo.name);

            Application newInfo = new Application();

            newInfo.shortcut = true;
            newInfo.appLabel = shortcutInfo.loadLabel(packageManager).toString();
            newInfo.packageName = shortcutInfo.activityInfo.applicationInfo.packageName;
            newInfo.activityName = shortcutInfo.activityInfo.name;
            newInfo.icon = shortcutInfo.loadIcon(packageManager);

            applicationsList.add(newInfo);

            if (cancelled)
                return;
        }

        Collections.sort(applicationsList, new SortList());
        Collections.sort(applicationsNoShortcutsList, new SortList());

        cached = true;
    }

    public int getLength(boolean noShortcuts)
    {
        if (cached) {
            if (noShortcuts)
                return applicationsNoShortcutsList.size();
            else
                return applicationsList.size();
        }
        else
            return 0;
    }

    public List<Application> getList(boolean noShorcuts)
    {
        if (cached) {
            if (noShorcuts)
                return applicationsNoShortcutsList;
            else
                return applicationsList;
        }
        else
            return null;
    }

    public Application getApplication(int position, boolean noShortcuts)
    {
        if (cached) {
            if (noShortcuts)
                return applicationsNoShortcutsList.get(position);
            else
                return applicationsList.get(position);
        }
        else
            return null;
    }

    public String getPackageName(int position, boolean noShortcuts)
    {
        if (cached) {
            if (noShortcuts)
                return applicationsNoShortcutsList.get(position).packageName;
            else
                return applicationsList.get(position).packageName;
        }
        else
            return "";
    }

    public void clearCache(boolean nullList)
    {
        applicationsList.clear();
        applicationsNoShortcutsList.clear();
        if (nullList) {
            applicationsList = null;
            applicationsNoShortcutsList = null;
        }
        cached = false;
    }

    public boolean isCached()
    {
        return cached;
    }

    public void cancelCaching()
    {
        cancelled = true;
    }

    public static boolean isShortcut(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            if (shortcut.equals("(s)"))
                return true;
        }
        return false;
    }

    public static String getPackageName(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            String[] splits2 = value.split("/");
            if (shortcut.equals("(s)")) {
                return splits2[0].substring(3);
            }
            return value;
        }
        else {
            return value;
        }
    }

    public static String getActivityName(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            String[] splits2 = value.split("/");
            if (shortcut.equals("(s)")) {
                return splits2[1];
            }
            return "";
        }
        else {
            return "";
        }
    }

}
