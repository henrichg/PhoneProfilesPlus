package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    FastScrollRecyclerView listView;
    private LinearLayout linlaProgress;
    private LinearLayout linlaData;
    RelativeLayout emptyList;
    private TextView contactsFilter;
    private ContactsFilterDialog mContactsFilterDialog;

    private ContactsMultiSelectPreferenceAdapter listAdapter;

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

        linlaProgress = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        linlaData = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_data);
        emptyList = view.findViewById(R.id.contacts_multiselect_pref_dlg_empty);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView = view.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
        //noinspection DataFlowIssue
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ContactsMultiSelectPreferenceAdapter(preference);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contacts_multiselect_pref_dlg_unselect_all);
        //noinspection DataFlowIssue
        unselectAllButton.setOnClickListener(v -> {
//            PPApplicationStatic.logE("[CONTACTS_DIALOG] ContactsMultiSelectDialogPreferenceFragment.onClick", "unselectAllButton click");
            preference.value="";
            if (preference.contactList != null) {
                for (Contact contact : preference.contactList)
                    contact.checked = false;
            }
            refreshListView(true);
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
                    fragment.refreshListView(false);
            }, 200);
        }

        contactsFilter = view.findViewById(R.id.contacts_multiselect_pref_dlg_contacts_filter);
        if ((preference.contactsFilter == null) || preference.contactsFilter.displayName.isEmpty()) {
            //if (preference.value.isEmpty())
            //    cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
            //else
            //noinspection DataFlowIssue
            contactsFilter.setText(R.string.contacts_filter_dialog_item_show_all);
        }
        else
            //noinspection DataFlowIssue
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

        mContactsFilterDialog = new ContactsFilterDialog((AppCompatActivity) prefContext, preference.withoutNumbers, preference);
        contactsFilter.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mContactsFilterDialog.showDialog();
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

    void refreshListView(final boolean forUnselect) {
        asyncTask = new RefreshListViewAsyncTask(forUnselect, preference, this, prefContext);
        asyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        final boolean forUnselect;
        private final WeakReference<ContactsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<ContactsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean forUnselect,
                                        ContactsMultiSelectDialogPreference preference,
                                        ContactsMultiSelectDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.forUnselect = forUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ContactsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (!forUnselect) {
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
//                PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.getContactsCache()");
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null) {
                    // cache not created, create it
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactsCache()");
                    PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/, false);
                    /*contactsCache = PPApplicationStatic.getContactsCache();
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getCaching()");
                    if (!contactsCache.getCaching()) {
                        // caching not performed
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getList()");
                        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
                        if (contactList == null) {
                            // not cached, cache it
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactsCache()");
                            PPApplicationStatic.createContactsCache(prefContext.getApplicationContext(), false, false/*, true*/, false);
                            /*contactsCache = PPApplicationStatic.getContactsCache();
                            while (contactsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        } else
                            contactList.clear();
                    } else {
                        // wait for cache end
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "contactsCache.getCaching()");
                        while (contactsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
                }
                //must be seconds, this ads groups into contacts
//                PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.getContactGroupsCache()");
                ContactGroupsCache contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                if (contactGroupsCache == null) {
                    // cache not created, create it
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactGroupsCache()");
                    PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/, false);
                    /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                    while (contactGroupsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getCaching()");
                    if (!contactGroupsCache.getCaching()) {
                        // caching not performed
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getList()");
                        List<ContactGroup> contactGroupList = contactGroupsCache.getList(/*withoutNumbers*/);
                        if (contactGroupList == null) {
                            // not cached, cache it
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactMultiSelectDialogPreferenceFragment.doInBackground", "PPApplicationStatic.createContactGroupsCache()");
                            PPApplicationStatic.createContactGroupsCache(prefContext.getApplicationContext(), false/*, false*//*, true*/, false);
                            /*contactGroupsCache = PPApplicationStatic.getContactGroupsCache();
                            while (contactGroupsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        }
                    } else {
                        // wait for cache end
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactMultiSelectDialogPreferenceFragment.doInBackground", "contactGroupsCache.getCaching()");
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
            final boolean _forUnselect = forUnselect;
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.linlaData.setVisibility(View.VISIBLE);

                    if (!_forUnselect) {
                        if (preference.contactList.isEmpty()) {
                            fragment.listView.setVisibility(View.GONE);
                            fragment.emptyList.setVisibility(View.VISIBLE);
                        } else {
                            fragment.emptyList.setVisibility(View.GONE);
                            fragment.listView.setVisibility(View.VISIBLE);
                        }

                        fragment.linlaData.setVisibility(View.VISIBLE);
                    }

                    fragment.listAdapter.notifyDataSetChanged();
                });
            }
        }

    }
}
