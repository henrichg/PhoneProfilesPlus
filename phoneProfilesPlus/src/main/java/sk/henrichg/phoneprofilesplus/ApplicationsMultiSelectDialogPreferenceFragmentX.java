package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;

public class ApplicationsMultiSelectDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ApplicationsMultiSelectDialogPreferenceX preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ApplicationsMultiSelectPreferenceAdapterX listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ApplicationsMultiSelectDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_applications_multiselect_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.applications_multiselect_pref_dlg_rella_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        FastScrollRecyclerView listView = view.findViewById(R.id.applications_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ApplicationsMultiSelectPreferenceAdapterX(preference);
        listView.setAdapter(listAdapter);

        /*
        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listView);
        */

        final Button unselectAllButton = view.findViewById(R.id.applications_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        refreshListView(true);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        if (EditorProfilesActivity.getApplicationsCache() != null) {
            EditorProfilesActivity.getApplicationsCache().cancelCaching();
            if (!EditorProfilesActivity.getApplicationsCache().cached)
                EditorProfilesActivity.getApplicationsCache().clearCache(false);
        }

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    private void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean notForUnselect;
        private final WeakReference<ApplicationsMultiSelectDialogPreferenceX> preferenceWeakRef;
        private final WeakReference<ApplicationsMultiSelectDialogPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ApplicationsMultiSelectDialogPreferenceX preference,
                                        ApplicationsMultiSelectDialogPreferenceFragmentX fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            ApplicationsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ApplicationsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ApplicationsMultiSelectDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().cacheApplicationsList(prefContext);

                preference.getValueAMSDP();
            }

            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ApplicationsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ApplicationsMultiSelectDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().clearCache(false);

                fragment.listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.VISIBLE);
                    fragment.linlaProgress.setVisibility(View.GONE);
                }
            }
        }
    }

}
