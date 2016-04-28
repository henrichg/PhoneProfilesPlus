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
    private boolean cached;
    private boolean cancelled;

    public ApplicationsCache()
    {
        applicationsList = new ArrayList<Application>();
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

            Log.d("ApplicationsCache.getApplicationsList", "shortcutInfo="+shortcutInfo);
            Log.d("ApplicationsCache.getApplicationsList", "packageName="+shortcutInfo.activityInfo.packageName);
            Log.d("ApplicationsCache.getApplicationsList", "name="+shortcutInfo.activityInfo.name);
            Log.d("ApplicationsCache.getApplicationsList", "isDefault="+shortcutInfo.isDefault);
            Log.d("ApplicationsCache.getApplicationsList", "intentFilter="+shortcutInfo.filter);

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

        cached = true;
    }

    public int getLength()
    {
        if (cached)
            return applicationsList.size();
        else
            return 0;
    }

    public List<Application> getList()
    {
        if (cached)
            return applicationsList;
        else
            return null;
    }

    public Application getApplication(int position)
    {
        if (cached)
            return applicationsList.get(position);
        else
            return null;
    }

    public String getPackageName(int position)
    {
        if (cached)
            return applicationsList.get(position).packageName;
        else
            return "";
    }

    public String getActivityName(int position) {
        if (cached)
            return applicationsList.get(position).activityName;
        else
            return "";
    }

    public String getApplicationLabel(int position)
    {
        if (cached)
            return applicationsList.get(position).appLabel;
        else
            return "";
    }

    public Drawable getApplicationIcon(int position)
    {
        if (cached)
            return applicationsList.get(position).icon;
        else
            return null;
    }

    public Application findApplication(String packageName, String activityName) {
        if (cached) {
            for (Application application : applicationsList) {
                if (application.packageName.equals(packageName) && application.activityName.equals(activityName))
                    return application;
            }
        }
        return null;
    }

    public void clearCache(boolean nullList)
    {
        applicationsList.clear();
        if (nullList)
            applicationsList = null;
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
