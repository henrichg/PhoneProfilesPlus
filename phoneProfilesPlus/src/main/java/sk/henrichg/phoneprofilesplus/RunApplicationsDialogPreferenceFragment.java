package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class RunApplicationsDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements OnStartDragItemListener {

    private Context prefContext;
    private RunApplicationsDialogPreference preference;

    private RecyclerView applicationsListView;
    private RelativeLayout emptyList;
    private ItemTouchHelper itemTouchHelper;

    private RunApplicationsDialogPreferenceAdapter listAdapter;

    private LinearLayout linlaProgress;
    private LinearLayout linlaDialog;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (RunApplicationsDialogPreference) getPreference();

        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_run_applications_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        AppCompatImageButton addButton = view.findViewById(R.id.run_applications_pref_dlg_add);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(addButton, getString(R.string.applications_pref_dlg_add_button_tooltip));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        applicationsListView = view.findViewById(R.id.run_applications_pref_dlg_listview);
        //applicationsListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        //noinspection DataFlowIssue
        applicationsListView.setLayoutManager(layoutManager);
        applicationsListView.setHasFixedSize(true);
        emptyList = view.findViewById(R.id.run_applications_pref_dlg_empty);

        linlaProgress = view.findViewById(R.id.run_applications_pref_dlg_linla_progress);
        linlaDialog = view.findViewById(R.id.run_applications_pref_dlg_linla_dialog);

        listAdapter = new RunApplicationsDialogPreferenceAdapter(prefContext, preference, this);

        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(applicationsListView);

        final ImageView helpIcon = view.findViewById(R.id.run_applications_pref_dlg_helpIcon);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> {
            String helpString = getString(R.string.run_applications_preference_application_type);

            DialogHelpPopupWindow.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, /*getDialog(),*/ helpString, false);
        });

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

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

        if (itemTouchHelper != null)
            itemTouchHelper.attachToRecyclerView(null);
        itemTouchHelper = null;

        if (PPApplicationStatic.getApplicationsCache() != null) {
            PPApplicationStatic.getApplicationsCache().cancelCaching();
            if (!PPApplicationStatic.getApplicationsCache().cached)
                PPApplicationStatic.getApplicationsCache().clearCache(false);
        }

        preference.fragment = null;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    void refreshListView(final boolean afterEdit) {
        asyncTask = new RefreshListViewAsyncTask(afterEdit, preference, this, prefContext);
        asyncTask.execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateGUI() {
        if (preference.applicationsList.isEmpty()) {
            applicationsListView.setVisibility(View.GONE);
            emptyList.setVisibility(View.VISIBLE);
        } else {
            emptyList.setVisibility(View.GONE);
            applicationsListView.setVisibility(View.VISIBLE);
        }

        applicationsListView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
        listAdapter.notifyDataSetChanged();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean afterEdit;
        private final WeakReference<RunApplicationsDialogPreference> preferenceWeakRef;
        private final WeakReference<RunApplicationsDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean afterEdit,
                                        RunApplicationsDialogPreference preference,
                                        RunApplicationsDialogPreferenceFragment fragment,
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

            RunApplicationsDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                fragment.linlaDialog.setVisibility(View.GONE);
                fragment.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            RunApplicationsDialogPreferenceFragment fragment = fragmentWeakRef.get();
            RunApplicationsDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                if (PPApplicationStatic.getApplicationsCache() != null)
                    if (!PPApplicationStatic.getApplicationsCache().cached)
                        PPApplicationStatic.getApplicationsCache().cacheApplicationsList(prefContext);

                List<PPIntent> _intentDBList = DatabaseHandler.getInstance(prefContext.getApplicationContext()).getAllIntents();
                preference.intentDBList.clear();
                preference.intentDBList.addAll(_intentDBList);

                preference.getValueAMSDP(/*false*/);
                //preference.getValueAMSDP(true);

                /*
                if (!afterEdit) {
                    preference.oldApplicationsList.clear();
                    preference.oldApplicationsList.addAll(preference.applicationsList);
                }
                */
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            RunApplicationsDialogPreferenceFragment fragment = fragmentWeakRef.get();
            RunApplicationsDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.linlaDialog.setVisibility(View.VISIBLE);

                    fragment.applicationsListView.setAdapter(fragment.listAdapter);

                    if (preference.applicationsList.isEmpty()) {
                        fragment.applicationsListView.setVisibility(View.GONE);
                        fragment.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        fragment.emptyList.setVisibility(View.GONE);
                        fragment.applicationsListView.setVisibility(View.VISIBLE);
                    }

                    if (afterEdit && (preference.mEditorDialog != null))
                        preference.mEditorDialog.updateAfterEdit();
                });
            }
        }
    }

}
