package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.service.wallpaper.WallpaperService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class LiveWallpapersDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private LiveWallpapersDialogPreferenceX preference;

    private ListView listView;
    private LinearLayout linlaProgress;

    private LiveWallpapersDialogPreferenceAdapterX listAdapter;

    boolean wifiEnabled;
    @SuppressWarnings("rawtypes")
    private RefreshListView1AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (LiveWallpapersDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_live_wallpapers_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.live_wallpapers_pref_dlg_listview);
        linlaProgress = view.findViewById(R.id.live_wallpapers_pref_dlg_linla_progress);

        listAdapter = new LiveWallpapersDialogPreferenceAdapterX(prefContext, preference);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            preference.value = preference.liveWallpapersList.get(position).componentName.toString();
            listAdapter.notifyDataSetChanged();
        });

        wifiEnabled = false;

        //if (Permissions.grantConnectToSSIDDialogPermissions(prefContext))
            refreshListView();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (preference.callChangeListener(preference.value))
                preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        preference.fragment = null;
    }

    void refreshListView() {
        asyncTask = new RefreshListView1AsyncTask(preference, this, prefContext);
        asyncTask.execute();
    }

    private static class SortList implements Comparator<LiveWallpapersData> {

        public int compare(LiveWallpapersData lhs, LiveWallpapersData rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.wallpaperName, rhs.wallpaperName);
            else
                return 0;
        }

    }

    private static class RefreshListView1AsyncTask extends AsyncTask<Void, Integer, Void> {

        List<LiveWallpapersData> _wallpapersList = null;

        private final WeakReference<LiveWallpapersDialogPreferenceX> preferenceWeakRef;
        private final WeakReference<LiveWallpapersDialogPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListView1AsyncTask(LiveWallpapersDialogPreferenceX preference,
                                         LiveWallpapersDialogPreferenceFragmentX fragment,
                                         Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _wallpapersList = new ArrayList<>();

            LiveWallpapersDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.listView.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            LiveWallpapersDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            LiveWallpapersDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                try {

                    List<LiveWallpapersData> liveWallpapersList = new ArrayList<>();

                    PackageManager packageManager = prefContext.getPackageManager();

                    // get ResolveInfo of live wallpapers
                    List<ResolveInfo> availableWallpapersList =
                            packageManager.queryIntentServices(
                                    new Intent(WallpaperService.SERVICE_INTERFACE),
                                    PackageManager.GET_META_DATA);

                    for (int i = 0; i < availableWallpapersList.size(); i++) {
                        ResolveInfo wallpaperResInfo = availableWallpapersList.get(i);

                        WallpaperInfo info;
                        try {
                            // WallaperInfo from ResolveInfo
                            info = new WallpaperInfo(prefContext, wallpaperResInfo);

                            LiveWallpapersData wallpaperData = new LiveWallpapersData(
                                    wallpaperResInfo.loadLabel(packageManager).toString(),
                                    info.getComponent()
                            );
                            liveWallpapersList.add(wallpaperData);
                        } catch (Exception ignored) {
                        }
                    }

                    for (LiveWallpapersData liveWallapaper : liveWallpapersList) {
                        //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.2", "wifiConfiguration.ssid="+wifiConfiguration.ssid);
                        if (liveWallapaper.componentName != null)
                            _wallpapersList.add(new LiveWallpapersData(liveWallapaper.wallpaperName, liveWallapaper.componentName));
                    }
                    //}

                    //noinspection Java8ListSort
                    Collections.sort(_wallpapersList, new SortList());

                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //PPApplication.logE("ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView.1", "call async 2");

            LiveWallpapersDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            LiveWallpapersDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.liveWallpapersList = new ArrayList<>(_wallpapersList);
                fragment.listView.setAdapter(fragment.listAdapter);

                fragment.linlaProgress.setVisibility(View.GONE);
                fragment.listView.setVisibility(View.VISIBLE);
            }

        }
    }

}
