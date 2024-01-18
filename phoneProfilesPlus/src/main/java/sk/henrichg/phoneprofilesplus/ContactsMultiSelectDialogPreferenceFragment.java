package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout linlaData;
    private TextView contactsFilter;
    private ContactsFilterDialog mContactsFilterDialog;

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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        linlaData = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        FastScrollRecyclerView listView = view.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ContactsMultiSelectPreferenceAdapter(preference);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contacts_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(v -> {
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceFragment.onClick", "unselectAllButton click");
            preference.value="";
            if (preference.contactList != null) {
                for (Contact contact : preference.contactList)
                    contact.checked = false;
            }
            refreshListView(false);
        });

        if (Permissions.grantContactsDialogPermissions(prefContext)) {
            /*
            // save checked contacts
            preference.checkedContactList = new ArrayList<>();
            if (preference.contactList != null) {
                for (Contact contact : preference.contactList) {
                    if (contact.checked)
                        preference.checkedContactList.add(contact);
                }
                preference.contactList.clear();
            }
            listAdapter.notifyDataSetChanged();
            */
            final Handler handler = new Handler(prefContext.getMainLooper());
            final WeakReference<ContactsMultiSelectDialogPreferenceFragment> fragmentWeakRef
                    = new WeakReference<>(this);
            handler.postDelayed(() -> {
                ContactsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
                if (fragment != null)
                    fragment.refreshListView(true);
            }, 200);
        }

        contactsFilter = view.findViewById(R.id.contacts_multiselect_pref_dlg_contacts_filter);
        if ((preference.contactsFilter == null) || preference.contactsFilter.displayName.isEmpty()) {
            //if (preference.value.isEmpty())
            //    cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
            //else
            contactsFilter.setText(R.string.contacts_filter_dialog_item_show_all);
        }
        else
            contactsFilter.setText(preference.contactsFilter.displayName);
        contactsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshListView(false);
            }
        });

        mContactsFilterDialog = new ContactsFilterDialog((Activity)prefContext, preference.withoutNumbers, preference);
        contactsFilter.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mContactsFilterDialog.show();
        });

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

    void setContactsFilter(ContactFilter filter) {
        contactsFilter.setText(filter.displayName);
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
                    fragment.linlaData.setVisibility(View.GONE);
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
                        fragment.linlaData.setVisibility(View.VISIBLE);
                    }

                    fragment.listAdapter.notifyDataSetChanged();
                });
            }
        }

    }
}
