package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LiveWallpapersDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private LiveWallpapersDialogPreference preference;

    private ListView listView;
    private LinearLayout linlaProgress;
    RelativeLayout emptyList;

    private LiveWallpapersDialogPreferenceAdapter listAdapter;

    private RefreshListView1AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (LiveWallpapersDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_live_wallpapers_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.live_wallpapers_pref_dlg_listview);
        emptyList = view.findViewById(R.id.live_wallpapers_pref_dlg_empty);
        linlaProgress = view.findViewById(R.id.live_wallpapers_pref_dlg_linla_progress);

        listAdapter = new LiveWallpapersDialogPreferenceAdapter(prefContext, preference);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            ComponentName componentName = preference.liveWallpapersList.get(position).componentName;
            if (componentName != null) {
                preference.value = componentName.flattenToString();
                listAdapter.notifyDataSetChanged();
            }
        });

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

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

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

        private final WeakReference<LiveWallpapersDialogPreference> preferenceWeakRef;
        private final WeakReference<LiveWallpapersDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListView1AsyncTask(LiveWallpapersDialogPreference preference,
                                         LiveWallpapersDialogPreferenceFragment fragment,
                                         Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _wallpapersList = new ArrayList<>();

            LiveWallpapersDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.listView.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            LiveWallpapersDialogPreferenceFragment fragment = fragmentWeakRef.get();
            LiveWallpapersDialogPreference preference = preferenceWeakRef.get();
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

                    int size = availableWallpapersList.size();
                    for (int i = 0; i < size; i++) {
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
                        if (liveWallapaper.componentName != null)
                            _wallpapersList.add(new LiveWallpapersData(liveWallapaper.wallpaperName, liveWallapaper.componentName));
                    }
                    //}

                    if (_wallpapersList.size() > 0)
                        _wallpapersList.sort(new SortList());
                    else
                        _wallpapersList.add(new LiveWallpapersData(prefContext.getString(R.string.profile_preferences_deviceLiveWallpaper_noneInstalled), null));

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            LiveWallpapersDialogPreferenceFragment fragment = fragmentWeakRef.get();
            LiveWallpapersDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.listView.setVisibility(View.VISIBLE);

                    preference.liveWallpapersList = new ArrayList<>(_wallpapersList);
                    fragment.listView.setAdapter(fragment.listAdapter);

                    if (preference.liveWallpapersList.size() == 0) {
                        fragment.listView.setVisibility(View.GONE);
                        fragment.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        fragment.emptyList.setVisibility(View.GONE);
                        fragment.listView.setVisibility(View.VISIBLE);
                    }
                });
            }

        }
    }

}
