package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

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

}
