package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;

public class ContactGroupsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactGroupsMultiSelectDialogPreference preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout rellaData;

    private ContactGroupsMultiSelectPreferenceAdapter listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ContactGroupsMultiSelectDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_contact_groups_multiselect_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_rella_data);
        ListView listView = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
            if (contactGroup != null) {
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapter(prefContext);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (Permissions.grantContactGroupsDialogPermissions(prefContext))
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

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

        //EditorActivity.getContactGroupsCache().cancelCaching();
        //if (!EditorActivity.getContactGroupsCache().cached)
        //    EditorActivity.getContactGroupsCache().clearCache(false);

        preference.fragment = null;
    }

    void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        final boolean notForUnselect;
        private final WeakReference<ContactGroupsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<ContactGroupsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ContactGroupsMultiSelectDialogPreference preference,
                                        ContactGroupsMultiSelectDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ContactGroupsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContactGroupsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ContactGroupsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!EditorActivity.getContactGroupsCache().cached)
                //    EditorActivity.getContactGroupsCache().getContactGroupList(prefContext);

                // must be first
                PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false);
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache != null) {
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);
                }
                //must be seconds, this ads groups into contacts
                PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false);
                ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                if (contactGroupsCache != null) {
                    while (contactGroupsCache.getCaching())
                        GlobalUtils.sleep(100);
                }

                preference.getValueCMSDP();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ContactGroupsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ContactGroupsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!EditorActivity.getContactGroupsCache().cached)
                //    EditorActivity.getContactGroupsCache().clearCache(false);

                fragment.listAdapter.notifyDataSetChanged();

                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.VISIBLE);
                    fragment.linlaProgress.setVisibility(View.GONE);
                }
            }
        }

    }

}
