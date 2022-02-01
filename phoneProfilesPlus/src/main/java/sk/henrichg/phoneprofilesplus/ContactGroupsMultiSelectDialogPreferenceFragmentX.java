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

public class ContactGroupsMultiSelectDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactGroupsMultiSelectDialogPreferenceX preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout rellaData;

    private ContactGroupsMultiSelectPreferenceAdapterX listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ContactGroupsMultiSelectDialogPreferenceX) getPreference();
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

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapterX(prefContext);
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

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        //EditorProfilesActivity.getContactGroupsCache().cancelCaching();
        //if (!EditorProfilesActivity.getContactGroupsCache().cached)
        //    EditorProfilesActivity.getContactGroupsCache().clearCache(false);

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        final boolean notForUnselect;
        private final WeakReference<ContactGroupsMultiSelectDialogPreferenceX> preferenceWeakRef;
        private final WeakReference<ContactGroupsMultiSelectDialogPreferenceFragmentX> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ContactGroupsMultiSelectDialogPreferenceX preference,
                                        ContactGroupsMultiSelectDialogPreferenceFragmentX fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ContactGroupsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContactGroupsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ContactGroupsMultiSelectDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!EditorProfilesActivity.getContactGroupsCache().cached)
                //    EditorProfilesActivity.getContactGroupsCache().getContactGroupList(prefContext);

                // must be first
                PhoneProfilesService.createContactsCache(prefContext.getApplicationContext(), false);
                ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
                if (contactsCache != null) {
                    while (contactsCache.getCaching())
                        PPApplication.sleep(100);
                }
                //must be seconds, this ads groups int contacts
                PhoneProfilesService.createContactGroupsCache(prefContext.getApplicationContext(), false);
                ContactGroupsCache contactGroupsCache = PhoneProfilesService.getContactGroupsCache();
                if (contactGroupsCache != null) {
                    while (contactGroupsCache.getCaching())
                        PPApplication.sleep(100);
                }

                preference.getValueCMSDP();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ContactGroupsMultiSelectDialogPreferenceFragmentX fragment = fragmentWeakRef.get();
            ContactGroupsMultiSelectDialogPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!EditorProfilesActivity.getContactGroupsCache().cached)
                //    EditorProfilesActivity.getContactGroupsCache().clearCache(false);

                fragment.listAdapter.notifyDataSetChanged();

                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.VISIBLE);
                    fragment.linlaProgress.setVisibility(View.GONE);
                }
            }
        }

    }

}
