package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.List;

public class ContactGroupsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactGroupsMultiSelectDialogPreference preference;

    // Layout widgets.
    ListView listView;
    private LinearLayout linlaProgress;
    private LinearLayout rellaData;
    RelativeLayout emptyList;

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
        listView = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);
        emptyList = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_empty);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
            if (contactGroup != null) {
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapter(prefContext, preference);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (Permissions.grantContactGroupsDialogPermissions(prefContext)) {
            if (preference.contactGroupList != null)
                preference.contactGroupList.clear();
            listAdapter.notifyDataSetChanged();
            final Handler handler = new Handler(prefContext.getMainLooper());
            final ContactGroupsMultiSelectDialogPreferenceFragment fragment = this;
            // TODO weak reference na fragment
            handler.postDelayed(() -> fragment.refreshListView(true), 200);
        }
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
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null) {
                    // cache not created, create it
                    PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/);
                    /*contactsCache = PPApplicationStatic.getContactsCache();
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
                    if (!contactsCache.getCaching()) {
                        // caching not performed
                        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
                        if (contactList == null) {
                            // not cached, cache it
                            PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/);
                            /*contactsCache = PPApplicationStatic.getContactsCache();
                            while (contactsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        } else
                            contactList.clear();
                    } else {
                        // wait for cache end
                        while (contactsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
                }
                //must be seconds, this ads groups into contacts
                ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                if (contactGroupsCache == null) {
                    // cache not created, create it
                    PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/);
                    /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                    while (contactGroupsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
                    if (!contactGroupsCache.getCaching()) {
                        // caching not performed
                        List<ContactGroup> contactGroupList = contactGroupsCache.getList(/*withoutNumbers*/);
                        if (contactGroupList == null) {
                            // not cached, cache it
                            PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/);
                            /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                            while (contactGroupsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        }
                    } else {
                        // wait for cache end
                        while (contactGroupsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
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

                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.rellaData.setVisibility(View.VISIBLE);

                    fragment.listAdapter.notifyDataSetChanged();

                    if (notForUnselect) {
                        ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                        if (contactGroupsCache != null) {
                            List<ContactGroup> contactGroupList = contactGroupsCache.getList();
                            if ((contactGroupList != null) && (contactGroupList.size() == 0)) {
                                fragment.listView.setVisibility(View.GONE);
                                fragment.emptyList.setVisibility(View.VISIBLE);
                            } else {
                                fragment.emptyList.setVisibility(View.GONE);
                                fragment.listView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }

    }

}
