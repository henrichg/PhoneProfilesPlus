package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationsCache {

    private class SortList implements Comparator<Application> {

        public int compare(Application lhs, Application rhs) {
            if (GlobalGUIRoutines.collator != null) {
                if (lhs == null)
                    return -1;
                else
                if (rhs == null)
                    return 1;
                else
                    return GlobalGUIRoutines.collator.compare(lhs.appLabel, rhs.appLabel);
            }
            else
                return 0;
        }

    }

    private ArrayList<Application> applicationsList;
    private LruCache<Object, Object> applicationIconsLru;
    private ArrayList<Application> applicationsNoShortcutsList;
    private LruCache<Object, Object> applicationNoShortcutIconsLru;
    boolean cached;
    private boolean cancelled;

    public ApplicationsCache()
    {
        applicationsList = new ArrayList<>();
        applicationIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
        applicationsNoShortcutsList = new ArrayList<>();
        applicationNoShortcutIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
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

                applicationsList.add(newInfo);
                applicationsNoShortcutsList.add(newInfo);

                Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = applicationInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon);
                    appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                    applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                }
                appIcon = applicationNoShortcutIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = applicationInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon);
                    appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                    applicationNoShortcutIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                }

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

                applicationsList.add(newInfo);

                Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = shortcutInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon);
                    appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                    applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                }
            }

            if (cancelled)
                return;
        }

        Collections.sort(applicationsList, new SortList());
        Collections.sort(applicationsNoShortcutsList, new SortList());

        cached = true;
    }

    /*
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
    */

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

    /*
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
    */

    Bitmap getApplicationIcon(Application application, boolean noShortcuts) {
        if (cached) {
            if (noShortcuts)
                return (Bitmap)applicationNoShortcutIconsLru.get(application.packageName + "/" + application.activityName);
            else
                return (Bitmap)applicationIconsLru.get(application.packageName + "/" + application.activityName);
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
        applicationIconsLru.evictAll();
        applicationsNoShortcutsList.clear();
        applicationNoShortcutIconsLru.evictAll();
        if (nullList) {
            applicationsList = null;
            applicationIconsLru = null;
            applicationsNoShortcutsList = null;
            applicationNoShortcutIconsLru = null;
        }
        cached = false;
    }

    void cancelCaching()
    {
        cancelled = true;
    }

    static boolean isShortcut(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            return shortcut.equals("(s)");
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
            long shortcutId = 0;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                // activity exists
                String shortcut = splits2[0].substring(0, 3);
                //packageName = splits2[0];
                String[] splits4 = splits2[1].split("#"); // shortcut id, startApplicationDelay
                //activityName = splits4[0];
                if (shortcut.equals("(s)")) {
                    if (splits4.length >= 2)
                        try {
                            shortcutId = Long.parseLong(splits4[1]);
                        } catch (Exception ignored) {}
                    //if (splits4.length >= 3)
                    //    startApplicationDelay = splits4[2];
                }
                //else {
                //    if (splits4.length >= 2)
                //        startApplicationDelay = splits4[1];
                //}
            } /*else {
                // activity not exists
                shortcut = value.substring(0, 3);
                String[] splits4 = value.split("#"); // startApplicationDelay
                if (splits4.length >= 2) {
                    packageName = splits4[0];
                    startApplicationDelay = splits4[1];
                }
                else {
                    packageName = split;
                }
                activityName = "";
            }*/
            return shortcutId;
        }
        else
            return 0;
    }

    static int getStartApplicationDelay(String value) {
        if (value.length() > 2) {
            int startApplicationDelay = 0;
            String[] splits2 = value.split("/");
            if (splits2.length == 2) {
                // activity exists
                String shortcut = splits2[0].substring(0, 3);
                //packageName = splits2[0];
                String[] splits4 = splits2[1].split("#"); // shortcut id, startApplicationDelay
                //activityName = splits4[0];
                if (shortcut.equals("(s)")) {
                    //if (splits4.length >= 2)
                    //    try {
                    //        shortcutId = Long.parseLong(splits4[1]);
                    //    } catch (Exception ignored) {}
                    if (splits4.length >= 3)
                        try {
                            startApplicationDelay = Integer.parseInt(splits4[2]);
                        } catch (Exception ignored) {}
                }
                else {
                    if (splits4.length >= 2)
                        try {
                            startApplicationDelay = Integer.parseInt(splits4[1]);
                        } catch (Exception ignored) {}
                }
            } else {
                // activity not exists
                //shortcut = value.substring(0, 3);
                String[] splits4 = value.split("#"); // startApplicationDelay
                if (splits4.length >= 2) {
                    //packageName = splits4[0];
                    try {
                        startApplicationDelay = Integer.parseInt(splits4[1]);
                    } catch (Exception ignored) {}
                }
                //else {
                //    packageName = split;
                //}
                //activityName = "";
            }
            return startApplicationDelay;
        }
        else
            return 0;
    }

}
