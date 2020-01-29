package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class ContactGroupsMultiSelectDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ContactGroupsMultiSelectDialogPreferenceX preference;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ContactGroupsMultiSelectPreferenceAdapterX listAdapter;

    private AsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (ContactGroupsMultiSelectDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_contact_groups_multiselect_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_rella_data);
        ListView listView = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapterX(prefContext);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = view.findViewById(R.id.contact_groups_multiselect_pref_dlg_unselect_all);
        //unselectAllButton.setAllCaps(false);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preference.value="";
                refreshListView(false);
            }
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

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //if (!EditorProfilesActivity.getContactGroupsCache().cached)
                //    EditorProfilesActivity.getContactGroupsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();

                if (notForUnselect) {
                    rellaData.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

}
