package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
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
import java.util.List;

public class ContactsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactsMultiSelectDialogPreference preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ContactsMultiSelectPreferenceAdapter listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ContactsMultiSelectDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_contacts_multiselect_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.contacts_multiselect_pref_dlg_rella_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        FastScrollRecyclerView listView = view.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ContactsMultiSelectPreferenceAdapter(preference);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contacts_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (Permissions.grantContactsDialogPermissions(prefContext))
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

        //PhoneProfilesService.getContactsCache().cancelCaching();
        //if (!PhoneProfilesService.getContactsCache().cached)
        //    PhoneProfilesService.getContactsCache().clearCache(false);

        preference.fragment = null;
    }

    void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        final boolean notForUnselect;
        private final WeakReference<ContactsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<ContactsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        ContactsMultiSelectDialogPreference preference,
                                        ContactsMultiSelectDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ContactsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContactsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ContactsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!PhoneProfilesService.getContactsCache().cached)
                //    PhoneProfilesService.getContactsCache().getContactList(prefContext);

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
                        }
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
                    PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false, false/*, true*/);
                    /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                    while (contactGroupsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
                    if (!contactGroupsCache.getCaching()) {
                        // caching not performed
                        List<ContactGroup> contactGroupList = contactGroupsCache.getList(/*withoutNumbers*/);
                        if (contactGroupList == null) {
                            // not cached, cache it
                            PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false, false/*, true*/);
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

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ContactsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            ContactsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                //if (!EditorActivity.getContactsCache().cached)
                //    EditorActivity.getContactsCache().clearCache(false);

                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    if (notForUnselect) {
                        fragment.rellaData.setVisibility(View.VISIBLE);
                    }

                    fragment.listAdapter.notifyDataSetChanged();
                });
            }
        }

    }
}
