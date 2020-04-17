package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("WeakerAccess")
public class ContactsMultiSelectDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactsMultiSelectDialogPreferenceX preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ContactsMultiSelectPreferenceAdapterX listAdapter;

    private AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ContactsMultiSelectDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_contacts_multiselect_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.contacts_multiselect_pref_dlg_rella_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        FastScrollRecyclerView listView = view.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ContactsMultiSelectPreferenceAdapterX(preference);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contacts_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preference.value="";
                refreshListView(false);
            }
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

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        //PhoneProfilesService.getContactsCache().cancelCaching();
        //if (!PhoneProfilesService.getContactsCache().cached)
        //    PhoneProfilesService.getContactsCache().clearCache(false);

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(final boolean notForUnselect) {
        asyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (notForUnselect) {
                    rellaData.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                //if (!PhoneProfilesService.getContactsCache().cached)
                //    PhoneProfilesService.getContactsCache().getContactList(prefContext);

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

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //if (!EditorProfilesActivity.getContactsCache().cached)
                //    EditorProfilesActivity.getContactsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    rellaData.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

}
