package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ApplicationsDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements OnStartDragItemListener {

    private Context prefContext;
    private ApplicationsDialogPreferenceX preference;

    private RecyclerView applicationsListView;
    private ItemTouchHelper itemTouchHelper;

    private ApplicationsDialogPreferenceAdapterX listAdapter;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ApplicationsDialogPreferenceX) getPreference();

        //PPApplication.logE("ApplicationsDialogPreferenceFragmentX.onCreateDialogView", "xxx");
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_applications_pref_dialog, null, false);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preference.startEditor(null);
            }
        });

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
        asyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                rellaDialog.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
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

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().clearCache(false);

                applicationsListView.setAdapter(listAdapter);
                rellaDialog.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                if (afterEdit && (preference.mEditorDialog != null))
                    preference.mEditorDialog.updateAfterEdit();
            }

        }.execute();
    }

    void updateGUI() {
        //applicationsListView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();
    }

}
