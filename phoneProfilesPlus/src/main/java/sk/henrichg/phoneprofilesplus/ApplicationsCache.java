package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.collection.LruCache;

class ApplicationsCache {

    private static class SortList implements Comparator<Application> {

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

    ApplicationsCache()
    {
        applicationsList = new ArrayList<>();
        applicationIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
        applicationsNoShortcutsList = new ArrayList<>();
        applicationNoShortcutIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
        cached = false;
    }

    void cacheApplicationsList(Context context)
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

                newInfo.type = Application.TYPE_APPLICATION;
                newInfo.appLabel = applicationInfo.loadLabel(packageManager).toString();
                newInfo.packageName = applicationInfo.activityInfo.applicationInfo.packageName;
                newInfo.activityName = applicationInfo.activityInfo.name;

                applicationsList.add(newInfo);
                applicationsNoShortcutsList.add(newInfo);

                Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = applicationInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                    if (bitmap != null) {
                        appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                        applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                    }
                    else {
                        //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                        icon = AppCompatResources.getDrawable(context, R.drawable.ic_empty);
                        bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        }
                    }
                }
                appIcon = applicationNoShortcutIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = applicationInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                    if (bitmap != null) {
                        appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                        applicationNoShortcutIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                    }
                    else {
                        //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                        icon = AppCompatResources.getDrawable(context, R.drawable.ic_empty);
                        bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationNoShortcutIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        }
                    }
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
        //PPApplication.logE("ApplicationsCache.cacheApplicationsList", "shortcuts.size="+shortcuts.size());
        for (int i = 0; i < shortcuts.size(); i++)
        {
            ResolveInfo shortcutInfo = shortcuts.get(i);

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("ApplicationsCache.cacheApplicationsList", "i=" + i);
                PPApplication.logE("ApplicationsCache.cacheApplicationsList", "shortcutInfo=" + shortcutInfo);
                PPApplication.logE("ApplicationsCache.cacheApplicationsList", "packageName=" + shortcutInfo.activityInfo.packageName);
                PPApplication.logE("ApplicationsCache.cacheApplicationsList", "name=" + shortcutInfo.activityInfo.name);
            }*/

            if ((shortcutInfo.activityInfo.applicationInfo.packageName != null) &&
                    (packageManager.getLaunchIntentForPackage(shortcutInfo.activityInfo.applicationInfo.packageName) != null)) {

                //PPApplication.logE("ApplicationsCache.cacheApplicationsList", "ADD");

                Application newInfo = new Application();

                newInfo.type = Application.TYPE_SHORTCUT;
                newInfo.appLabel = shortcutInfo.loadLabel(packageManager).toString();
                newInfo.packageName = shortcutInfo.activityInfo.applicationInfo.packageName;
                newInfo.activityName = shortcutInfo.activityInfo.name;

                applicationsList.add(newInfo);

                Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                if (appIcon == null){
                    Drawable icon = shortcutInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                    if (bitmap != null) {
                        appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                        applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                    }
                    else {
                        //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                        icon = AppCompatResources.getDrawable(context, R.drawable.ic_empty);
                        bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        }
                    }
                }
            }

            if (cancelled)
                return;
        }

        Collections.sort(applicationsList, new SortList());
        Collections.sort(applicationsNoShortcutsList, new SortList());

        cached = true;
    }

    List<Application> getApplicationList(boolean noShortcuts)
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

}
