package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;

public class ApplicationsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ApplicationsMultiSelectDialogPreference preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout linlaData;
    private Button unselectAllButton;

    private ApplicationsMultiSelectPreferenceAdapter listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ApplicationsMultiSelectDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_applications_multiselect_preference, null, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        linlaData = view.findViewById(R.id.applications_multiselect_pref_dlg_linla_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        FastScrollRecyclerView listView = view.findViewById(R.id.applications_multiselect_pref_dlg_listview);
        //noinspection DataFlowIssue
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ApplicationsMultiSelectPreferenceAdapter(preference);
        listView.setAdapter(listAdapter);

        /*
        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listView);
        */

        unselectAllButton = view.findViewById(R.id.applications_multiselect_pref_dlg_unselect_all);
        //noinspection DataFlowIssue
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (preference.applicationList != null)
            preference.applicationList.clear();
        listAdapter.notifyDataSetChanged();
        final Handler handler = new Handler(prefContext.getMainLooper());
        final WeakReference<ApplicationsMultiSelectDialogPreferenceFragment> fragmentWeakRef
                = new WeakReference<>(this);
        handler.postDelayed(() -> {
            ApplicationsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null)
                fragment.refreshListView(true);
        }, 200);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING)){
            asyncTask.cancel(true);
        }
        asyncTask = null;

        if (PPApplicationStatic.getApplicationsCache() != null) {
            PPApplicationStatic.getApplicationsCache().cancelCaching();
            if (!PPApplicationStatic.getApplicationsCache().cached)
                PPApplicationStatic.getApplicationsCache().clearCache(false);
        }

        preference.fragment = null;
    }

    private void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean notForUnselect;
        private final WeakReference<ApplicationsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<ApplicationsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ApplicationsMultiSelectDialogPreference preference,
                                        ApplicationsMultiSelectDialogPreferenceFragment fragment,
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
            ApplicationsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.linlaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
                fragment.unselectAllButton.setEnabled(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ApplicationsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ApplicationsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (PPApplicationStatic.getApplicationsCache() != null)
                    if (!PPApplicationStatic.getApplicationsCache().cached)
                        PPApplicationStatic.getApplicationsCache().cacheApplicationsList(prefContext);

                preference.getValueAMSDP();
            }

            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ApplicationsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ApplicationsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                /*if (PPApplicationStatic.getApplicationsCache() != null)
                    if (!PPApplicationStatic.getApplicationsCache().cached)
                        PPApplicationStatic.getApplicationsCache().clearCache(false);*/

                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    if (notForUnselect) {
                        fragment.linlaData.setVisibility(View.VISIBLE);
                    }

                    fragment.unselectAllButton.setEnabled(true);

                    fragment.listAdapter.notifyDataSetChanged();
                });
            }
        }
    }

}
