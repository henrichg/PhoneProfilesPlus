package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class ApplicationsDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements OnStartDragItemListener {

    private Context prefContext;
    private ApplicationsDialogPreferenceX preference;

    private RecyclerView applicationsListView;
    private ItemTouchHelper itemTouchHelper;

    private ApplicationsDialogPreferenceAdapterX listAdapter;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    @SuppressWarnings("rawtypes")
    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ApplicationsDialogPreferenceX) getPreference();

        //PPApplication.logE("ApplicationsDialogPreferenceFragmentX.onCreateDialogView", "xxx");
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_applications_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        AppCompatImageButton addButton = view.findViewById(R.id.applications_pref_dlg_add);
        TooltipCompat.setTooltipText(addButton, getString(R.string.applications_pref_dlg_add_button_tooltip));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        applicationsListView = view.findViewById(R.id.applications_pref_dlg_listview);
        //applicationsListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        applicationsListView.setLayoutManager(layoutManager);
        applicationsListView.setHasFixedSize(true);

        linlaProgress = view.findViewById(R.id.applications_pref_dlg_linla_progress);
        rellaDialog = view.findViewById(R.id.applications_pref_dlg_rella_dialog);

        listAdapter = new ApplicationsDialogPreferenceAdapterX(prefContext, preference, this);

        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(applicationsListView);

        addButton.setOnClickListener(v -> preference.startEditor(null));

        refreshListView(false);
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(final boolean afterEdit) {
        asyncTask = new RefreshListViewAsyncTask(afterEdit, preference, this, prefContext);
        asyncTask.execute();
    }

    void updateGUI() {
        applicationsListView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean afterEdit;
        private final WeakReference<ApplicationsDialogPreferenceX> preferenceWeakRef;
        private final WeakReference<ApplicationsDialogPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean afterEdit,
                                        ApplicationsDialogPreferenceX preference,
                                        ApplicationsDialogPreferenceFragmentX fragment,
                                        Context prefContext) {
            this.afterEdit = afterEdit;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ApplicationsDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.rellaDialog.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ApplicationsDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ApplicationsDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().cacheApplicationsList(prefContext);

                List<PPIntent> _intentDBList = DatabaseHandler.getInstance(prefContext.getApplicationContext()).getAllIntents();
                preference.intentDBList.clear();
                preference.intentDBList.addAll(_intentDBList);

                //PPApplication.logE("ApplicationsDialogPreference.refreshListView", "intentDBList.size="+preference.intentDBList.size());

                preference.getValueAMSDP();

                if (!afterEdit) {
                    preference.oldApplicationsList.clear();
                    preference.oldApplicationsList.addAll(preference.applicationsList);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ApplicationsDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ApplicationsDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().clearCache(false);

                fragment.applicationsListView.setAdapter(fragment.listAdapter);
                fragment.rellaDialog.setVisibility(View.VISIBLE);
                fragment.linlaProgress.setVisibility(View.GONE);

                if (afterEdit && (preference.mEditorDialog != null))
                    preference.mEditorDialog.updateAfterEdit();
            }
        }
    }

}
