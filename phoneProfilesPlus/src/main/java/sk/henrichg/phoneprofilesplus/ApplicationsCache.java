package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
//import androidx.collection.LruCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ApplicationsCache {

    private ArrayList<Application> applicationsList;
    //private LruCache<Object, Object> applicationIconsLru;
    private ArrayList<Application> applicationsNoShortcutsList;
    //private LruCache<Object, Object> applicationNoShortcutIconsLru;

    boolean cached;
    private boolean cancelled;

    ApplicationsCache()
    {
        applicationsList = new ArrayList<>();
        //applicationIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
        applicationsNoShortcutsList = new ArrayList<>();
        //applicationNoShortcutIconsLru = new LruCache<>(5 * 1024 * 1024); //Max is 5MB
        cached = false;
    }

    void cacheApplicationsList(Context context)
    {
        if (cached) return;

//        PPApplicationStatic.logE("[SYNCHRONIZED] ApplicationsCache.cacheApplicationsList", "PPApplication.applicationCacheMutex");
        synchronized (PPApplication.applicationCacheMutex) {
            cancelled = false;

            //applicationsList.clear();
            clearCache(false);

            Context appContext = context.getApplicationContext();

            PackageManager packageManager = appContext.getPackageManager();

            //int count = 0;

            Intent appsIntent = new Intent(Intent.ACTION_MAIN);
            appsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            int flags = PackageManager.MATCH_ALL;
            List<ResolveInfo> applications = packageManager.queryIntentActivities(appsIntent, flags);
            int size = applications.size();
            for (int i = 0; i < size; i++) {
                ResolveInfo applicationInfo = applications.get(i);

                if ((applicationInfo.activityInfo.applicationInfo.packageName != null) &&
                        (packageManager.getLaunchIntentForPackage(applicationInfo.activityInfo.applicationInfo.packageName) != null)) {
                    Application newInfo = new Application();

                    newInfo.type = Application.TYPE_APPLICATION;
                    newInfo.appLabel = applicationInfo.loadLabel(packageManager).toString();
                    newInfo.packageName = applicationInfo.activityInfo.applicationInfo.packageName;
                    newInfo.activityName = applicationInfo.activityInfo.name;

                    /*
                    Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                    if (appIcon == null) {
                        Drawable icon = applicationInfo.loadIcon(packageManager);
                        Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        } else {
                            //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                            icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_empty);
                            bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                            if (bitmap != null) {
                                appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                                applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                            }
                        }
                    }
                    appIcon = applicationNoShortcutIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                    if (appIcon == null) {
                        Drawable icon = applicationInfo.loadIcon(packageManager);
                        Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationNoShortcutIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        } else {
                            //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                            icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_empty);
                            bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                            if (bitmap != null) {
                                appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                                applicationNoShortcutIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                            }
                        }
                    }
                    */

                    Bitmap appIcon;
                    Drawable icon = applicationInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                    if (bitmap != null) {
                        appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                        newInfo.icon = appIcon;
                    } else {
                        //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                        icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_empty);
                        bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            newInfo.icon = appIcon;
                        }
                    }

                    applicationsList.add(newInfo);
                    applicationsNoShortcutsList.add(newInfo);

                    //++count;
                }

                if (cancelled)
                    return;
            }
            //Log.e("ApplicationsCache.cacheApplicationsList", "added into from applicationNoShortcutIconsLru - count="+count);

            Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            List<ResolveInfo> shortcuts = packageManager.queryIntentActivities(shortcutsIntent, flags);
            size = shortcuts.size();
            for (int i = 0; i < size; i++) {
                ResolveInfo shortcutInfo = shortcuts.get(i);

                if ((shortcutInfo.activityInfo.applicationInfo.packageName != null) &&
                        (packageManager.getLaunchIntentForPackage(shortcutInfo.activityInfo.applicationInfo.packageName) != null)) {

                    Application newInfo = new Application();

                    newInfo.type = Application.TYPE_SHORTCUT;
                    newInfo.appLabel = shortcutInfo.loadLabel(packageManager).toString();
                    newInfo.packageName = shortcutInfo.activityInfo.applicationInfo.packageName;
                    newInfo.activityName = shortcutInfo.activityInfo.name;

                    /*
                    Object appIcon = applicationIconsLru.get(newInfo.packageName + "/" + newInfo.activityName);
                    if (appIcon == null) {
                        Drawable icon = shortcutInfo.loadIcon(packageManager);
                        Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                        } else {
                            //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                            icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_empty);
                            bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                            if (bitmap != null) {
                                appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                                applicationIconsLru.put(newInfo.packageName + "/" + newInfo.activityName, appIcon);
                            }
                        }
                    }
                    */

                    Bitmap appIcon;
                    Drawable icon = shortcutInfo.loadIcon(packageManager);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                    if (bitmap != null) {
                        appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                        newInfo.icon = appIcon;
                    } else {
                        //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                        icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_empty);
                        bitmap = BitmapManipulator.getBitmapFromDrawable(icon, true);
                        if (bitmap != null) {
                            appIcon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                            newInfo.icon = appIcon;
                        }
                    }


                    applicationsList.add(newInfo);

                    //++count;
                }

                if (cancelled)
                    return;
            }
            //Log.e("ApplicationsCache.cacheApplicationsList", "added into from applicationsList - count="+count);

            applicationsList.sort(new SortList());
            applicationsNoShortcutsList.sort(new SortList());

            cached = true;

        }
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

    Bitmap getApplicationIcon(Application application/*, boolean noShortcuts*/) {
        if (cached) {

            //if (noShortcuts)
            //    return (Bitmap)applicationNoShortcutIconsLru.get(application.packageName + "/" + application.activityName);
            //else
            //    return (Bitmap)applicationIconsLru.get(application.packageName + "/" + application.activityName);

            //if (noShortcuts)
            //    return application.icon;
            //else
                return application.icon;
        }
        else
            return null;
    }

    void setApplicationIcon(Context context, Application application)
    {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        Drawable dIcon = null;
        if (application.shortcutId != 0) {
            Intent intent = new Intent();
            intent.setClassName(application.packageName, application.activityName);
            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
            if (info != null)
                dIcon = info.loadIcon(packageManager);
        } else {
            String activityName = application.activityName;
            if (activityName.isEmpty()) {
                try {
                    ApplicationInfo app = packageManager.getApplicationInfo(application.packageName, PackageManager.MATCH_ALL);
                    //if (app != null)
                        dIcon = packageManager.getApplicationIcon(app);
                } catch (Exception ignored) {}
            } else {
                Intent intent = new Intent();
                intent.setClassName(Application.getPackageName(application.packageName), activityName);
                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                if (info != null)
                    dIcon = info.loadIcon(packageManager);
            }
        }

        if (dIcon != null) {
            Bitmap bitmap = BitmapManipulator.getBitmapFromDrawable(dIcon, true);
            if (bitmap != null) {
                application.icon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
            } else {
                //icon = ContextCompat.getDrawable(context, R.drawable.ic_empty);
                dIcon = AppCompatResources.getDrawable(context, R.drawable.ic_empty);
                bitmap = BitmapManipulator.getBitmapFromDrawable(dIcon, true);
                if (bitmap != null) {
                    application.icon = Bitmap.createScaledBitmap(bitmap, 40, 40, true);
                }
            }
        }
    }


    void clearCache(boolean nullList)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] ApplicationsCache.clearCache", "PPApplication.applicationCacheMutex");
        synchronized (PPApplication.applicationCacheMutex) {
            //int count = 0;
            for (Application application : applicationsList) {
                Bitmap icon = getApplicationIcon(application/*, false*/);
                if ((icon != null) && (!icon.isRecycled())) {
                    try {
                        icon.recycle();
                        //++count;
                    } catch (Exception ignored) {}
                }
            }
            applicationsList.clear();
            //applicationIconsLru.evictAll();
            //Log.e("ApplicationsCache.clearCache", "clear icon from applicationList - count="+count);

            //count = 0;
            for (Application application : applicationsNoShortcutsList) {
                Bitmap icon = getApplicationIcon(application/*, true*/);
                if ((icon != null) && (!icon.isRecycled())) {
                    try {
                        icon.recycle();
                        //++count;
                    } catch (Exception ignored) {}
                }
            }
            applicationsNoShortcutsList.clear();
            //applicationNoShortcutIconsLru.evictAll();
            //Log.e("ApplicationsCache.clearCache", "clear icon from applicationsNoShortcutsList - count="+count);

            if (nullList) {
                applicationsList = null;
                //applicationIconsLru = null;
                applicationsNoShortcutsList = null;
                //applicationNoShortcutIconsLru = null;
            }
            cached = false;
        }
    }

    void cancelCaching()
    {
        cancelled = true;
    }

    private static class SortList implements Comparator<Application> {

        public int compare(Application lhs, Application rhs) {
            if (PPApplication.collator != null) {
                if (lhs == null)
                    return -1;
                else
                if (rhs == null)
                    return 1;
                else
                    return PPApplication.collator.compare(lhs.appLabel, rhs.appLabel);
            }
            else
                return 0;
        }

    }

}
