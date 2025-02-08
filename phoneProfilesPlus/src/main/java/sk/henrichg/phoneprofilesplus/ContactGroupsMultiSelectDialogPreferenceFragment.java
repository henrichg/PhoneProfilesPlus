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
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.List;

public class ContactGroupsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactGroupsMultiSelectDialogPreference preference;

    // Layout widgets.
    ListView listView;
    private LinearLayout linlaProgress;
    private LinearLayout linlaData;
    RelativeLayout emptyList;

    private ContactGroupsMultiSelectPreferenceAdapter listAdapter;

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
        preference = (ContactGroupsMultiSelectDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_contact_groups_multiselect_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        linlaData = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_data);
        listView = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);
        emptyList = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_empty);

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapter(prefContext, preference);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
            if (contactGroup != null) {
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        final Button unselectAllButton = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_unselect_all);
        //noinspection DataFlowIssue
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(true);
        });

        if (Permissions.grantContactGroupsDialogPermissions(prefContext)) {
            if (preference.contactGroupList != null)
                preference.contactGroupList.clear();
            listAdapter.notifyDataSetChanged();
            final Handler handler = new Handler(prefContext.getMainLooper());
            final WeakReference<ContactGroupsMultiSelectDialogPreferenceFragment> fragmentWeakRef
                    = new WeakReference<>(this);
            handler.postDelayed(() -> {
                ContactGroupsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
                if (fragment != null)
                    fragment.refreshListView(false);
            }, 200);
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

    void refreshListView(final boolean forUnselect) {
        asyncTask = new RefreshListViewAsyncTask(forUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        final boolean forUnselect;
        private final WeakReference<ContactGroupsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<ContactGroupsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean forUnselect,
                                        ContactGroupsMultiSelectDialogPreference preference,
                                        ContactGroupsMultiSelectDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.forUnselect = forUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ContactGroupsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (!forUnselect) {
                    fragment.linlaData.setVisibility(View.GONE);
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
//                PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.getContactsCache()");
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null) {
                    // cache not created, create it
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactsCache()");
                    PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/, false);
                    /*contactsCache = PPApplicationStatic.getContactsCache();
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getCaching()");
                    if (!contactsCache.getCaching()) {
                        // caching not performed
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getList()");
                        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
                        if (contactList == null) {
                            // not cached, cache it
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactsCache()");
                            PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/, false);
                            /*contactsCache = PPApplicationStatic.getContactsCache();
                            while (contactsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        } else
                            contactList.clear();
                    } else {
                        // wait for cache end
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getCaching()");
                        while (contactsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
                }
                //must be seconds, this ads groups into contacts
//                PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.getContactGroupsCache()");
                ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                if (contactGroupsCache == null) {
                    // cache not created, create it
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactGroupsCache()");
                    PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/, false);
                    /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                    while (contactGroupsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getCaching()");
                    if (!contactGroupsCache.getCaching()) {
                        // caching not performed
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getList()");
                        List<ContactGroup> contactGroupList = contactGroupsCache.getList(/*withoutNumbers*/);
                        if (contactGroupList == null) {
                            // not cached, cache it
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactGroupsCache()");
                            PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/, false);
                            /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                            while (contactGroupsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        }
                    } else {
                        // wait for cache end
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getCaching()");
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
            final boolean _forUnselect = forUnselect;
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.linlaData.setVisibility(View.VISIBLE);

                    if (!_forUnselect) {
                        if (preference.contactGroupList.isEmpty()) {
                            fragment.listView.setVisibility(View.GONE);
                            fragment.emptyList.setVisibility(View.VISIBLE);
                        } else {
                            fragment.emptyList.setVisibility(View.GONE);
                            fragment.listView.setVisibility(View.VISIBLE);
                        }
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.onPostExecute", "PPApplicationStatic.getContactGroupsCache()");
//                        ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
//                        if (contactGroupsCache != null) {
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactGroupsMultiSelectDialogPreferenceFragment.onPostExecute", "contactGroupsCache.getList()");
//                            List<ContactGroup> contactGroupList = contactGroupsCache.getList();
//                            if ((contactGroupList != null) && (contactGroupList.isEmpty())) {
//                                fragment.listView.setVisibility(View.GONE);
//                                fragment.emptyList.setVisibility(View.VISIBLE);
//                            } else {
//                                fragment.emptyList.setVisibility(View.GONE);
//                                fragment.listView.setVisibility(View.VISIBLE);
//                            }
//                        }
                    }

                    fragment.listAdapter.notifyDataSetChanged();
                });
            }
        }

    }

}
