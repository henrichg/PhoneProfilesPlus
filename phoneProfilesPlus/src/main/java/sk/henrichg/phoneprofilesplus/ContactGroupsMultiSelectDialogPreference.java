package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

public class ContactGroupsMultiSelectDialogPreference extends DialogPreference
{

    private final Context _context;
    private String value = "";

    private AlertDialog mDialog;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ContactGroupsMultiSelectPreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    public ContactGroupsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactGroupsCache() == null)
            EditorProfilesActivity.createContactGroupsCache();

    }

    protected void showDialog(Bundle state) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist())
                {
                    // fill with strings of contact groups separated with |
                    value = "";
                    List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
                    if (contactGroupList != null)
                    {
                        for (ContactGroup contactGroup : contactGroupList)
                        {
                            if (contactGroup.checked)
                            {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + contactGroup.groupId;
                            }
                        }
                    }
                    persistString(value);

                    setSummaryCMSDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_contact_groups_multiselect_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ContactGroupsMultiSelectDialogPreference.this.onShow(/*dialog*/);
            }
        });

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        rellaData = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_rella_data);
        //noinspection ConstantConditions
        ListView listView = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        listAdapter = new ContactGroupsMultiSelectPreferenceAdapter(_context);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_unselect_all);
        //unselectAllButton.setAllCaps(false);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value="";
                refreshListView(false);
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)_context).isFinishing())
            mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean notForUnselect) {
        if ((mDialog != null) && mDialog.isShowing()) {

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
                    if (!EditorProfilesActivity.getContactGroupsCache().cached)
                        EditorProfilesActivity.getContactGroupsCache().getContactGroupList(_context);

                    getValueCMSDP(notForUnselect);

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    if (!EditorProfilesActivity.getContactGroupsCache().cached)
                        EditorProfilesActivity.getContactGroupsCache().clearCache(false);

                    listAdapter.notifyDataSetChanged();

                    if (notForUnselect) {
                        rellaData.setVisibility(View.VISIBLE);
                        linlaProgress.setVisibility(View.GONE);
                    }
                }

            }.execute();
        }
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantContactGroupsDialogPermissions(_context))
            refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        EditorProfilesActivity.getContactGroupsCache().cancelCaching();
        if (!EditorProfilesActivity.getContactGroupsCache().cached)
            EditorProfilesActivity.getContactGroupsCache().clearCache(false);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueCMSDP(true);
        }
        else {
            // set state
            value = "";
            persistString("");
        }
        setSummaryCMSDP();
    }

    private void getValueCMSDP(boolean notForUnselect)
    {
        if (notForUnselect)
            // Get the persistent value
            value = getPersistedString(value);

        // change checked state by value
        List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
        if (contactGroupList != null)
        {
            String[] splits = value.split("\\|");
            for (ContactGroup contactGroup : contactGroupList)
            {
                contactGroup.checked = false;
                for (String split : splits) {
                    try {
                        long groupId = Long.parseLong(split);
                        if (contactGroup.groupId == groupId)
                            contactGroup.checked = true;
                    } catch (Exception ignored) {
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < contactGroupList.size()) {
                ContactGroup contactGroup = contactGroupList.get(i);
                if (contactGroup.checked) {
                    contactGroupList.remove(i);
                    contactGroupList.add(ich, contactGroup);
                    ich++;
                }
                i++;
            }
        }
    }

    private void setSummaryCMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(_context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    String[] projection = new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE};
                    String selection = ContactsContract.Groups._ID + "=" + splits[0];
                    Cursor mCursor = _context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        //while (mCursor.moveToNext()) {
                        if (mCursor.moveToFirst()) {
                            found = true;
                            prefVolumeDataSummary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                            //break;
                        }
                        mCursor.close();
                    }
                    if (!found)
                        prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        setSummary(prefVolumeDataSummary);
    }

}
