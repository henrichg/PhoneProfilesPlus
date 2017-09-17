package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationsCache {

    private class SortList implements Comparator<Application> {

        public int compare(Application lhs, Application rhs) {
            return GlobalGUIRoutines.collator.compare(lhs.appLabel, rhs.appLabel);
        }

    }

    private ArrayList<Application> applicationsList;
    private ArrayList<Application> applicationsNoShortcutsList;
    private boolean cached;
    private boolean cancelled;

    public ApplicationsCache()
    {
        applicationsList = new ArrayList<>();
        applicationsNoShortcutsList = new ArrayList<>();
        cached = false;
    }

    void getApplicationsList(Context context)
    {
        if (cached) return;

        cancelled = false;

        applicationsList.clear();

        PackageManager packageManager = context.getPackageManager();

        Intent appsIntent = new Intent(Intent.ACTION_MAIN);
        appsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 23)
            flags = PackageManager.MATCH_ALL;
        List<ResolveInfo> applications = packageManager.queryIntentActivities(appsIntent, flags);
        for (int i = 0; i < applications.size(); i++)
        {
            ResolveInfo applicationInfo = applications.get(i);

            if ((applicationInfo.activityInfo.applicationInfo.packageName != null) &&
                    (packageManager.getLaunchIntentForPackage(applicationInfo.activityInfo.applicationInfo.packageName) != null))
            {
                Application newInfo = new Application();

                newInfo.shortcut = false;
                newInfo.appLabel = applicationInfo.loadLabel(packageManager).toString();
                newInfo.packageName = applicationInfo.activityInfo.applicationInfo.packageName;
                newInfo.activityName = applicationInfo.activityInfo.name;
                try {
                    newInfo.icon = applicationInfo.loadIcon(packageManager);
                } catch (Exception e) {
                    newInfo.icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                }

                applicationsList.add(newInfo);
                applicationsNoShortcutsList.add(newInfo);
            }

            if (cancelled)
                return;
        }

        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 23)
            flags = PackageManager.MATCH_ALL;
        List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(shortcutsIntent, flags);
        //Log.d("ApplicationsCache.getApplicationsList", "shortcuts.size="+shortcuts.size());
        for (int i = 0; i < shortcuts.size(); i++)
        {
            ResolveInfo shortcutInfo = shortcuts.get(i);

            //Log.d("ApplicationsCache.getApplicationsList", "shortcutInfo="+shortcutInfo);
            //Log.d("ApplicationsCache.getApplicationsList", "packageName="+shortcutInfo.activityInfo.packageName);
            //Log.d("ApplicationsCache.getApplicationsList", "name="+shortcutInfo.activityInfo.name);

            if ((shortcutInfo.activityInfo.applicationInfo.packageName != null) &&
                    (packageManager.getLaunchIntentForPackage(shortcutInfo.activityInfo.applicationInfo.packageName) != null)) {
                Application newInfo = new Application();

                newInfo.shortcut = true;
                newInfo.appLabel = shortcutInfo.loadLabel(packageManager).toString();
                newInfo.packageName = shortcutInfo.activityInfo.applicationInfo.packageName;
                newInfo.activityName = shortcutInfo.activityInfo.name;
                newInfo.icon = shortcutInfo.loadIcon(packageManager);

                applicationsList.add(newInfo);
            }

            if (cancelled)
                return;
        }

        Collections.sort(applicationsList, new SortList());
        Collections.sort(applicationsNoShortcutsList, new SortList());

        cached = true;
    }

    int getLength(boolean noShortcuts)
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

    List<Application> getList(boolean noShortcuts)
    {
        if (cached) {
            if (noShortcuts)
                return applicationsNoShortcutsList;
            else
                return applicationsList;
        }
        else
            return null;
    }

    Application getApplication(int position, boolean noShortcuts)
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

    /*
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
    */

    void clearCache(boolean nullList)
    {
        applicationsList.clear();
        applicationsNoShortcutsList.clear();
        if (nullList) {
            applicationsList = null;
            applicationsNoShortcutsList = null;
        }
        cached = false;
    }

    boolean isCached()
    {
        return cached;
    }

    void cancelCaching()
    {
        cancelled = true;
    }

    static boolean isShortcut(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            if (shortcut.equals("(s)"))
                return true;
        }
        return false;
    }

    public static String getPackageName(String value) {
        if (value.length() > 2) {
            String packageName;
            String shortcut;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                shortcut = splits2[0].substring(0, 3);
                packageName = splits2[0];
            }
            else {
                shortcut = value.substring(0, 3);
                packageName = value;
            }
            if (shortcut.equals("(s)")) {
                return packageName.substring(3);
            }
            return packageName;
        }
        else
            return "";
    }

    static String getActivityName(String value) {
        if (value.length() > 2) {
            String activityName;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                String[] splits3 = splits2[1].split("#");
                activityName = splits3[0];
            }
            else
                activityName = "";
            return activityName;
        }
        else
            return "";
    }

    static long getShortcutId(String value) {
        if (value.length() > 2) {
            long shortcutId;
            //String activityName;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                String[] splits3 = splits2[1].split("#");
                try {
                    shortcutId = Long.parseLong(splits3[1]);
                } catch (Exception e) {
                    shortcutId = 0;
                }
            }
            else
                shortcutId = 0;
            return shortcutId;
        }
        else
            return 0;
    }

}
