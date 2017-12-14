package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class ContactGroupsMultiSelectDialogPreference extends DialogPreference
{

    private Context _context = null;
    private String value = "";

    private MaterialDialog mDialog;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

    private ContactGroupsMultiSelectPreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    public ContactGroupsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactGroupsCache() == null)
            EditorProfilesActivity.createContactGroupsCache();

    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.pref_dlg_change_selection_button_unselect_all)
                .content(getDialogMessage())
                .customView(R.layout.activity_contact_groups_multiselect_pref_dialog, false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @SuppressWarnings("StringConcatenationInLoop")
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
                            mDialog.dismiss();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        value="";
                        refreshListView(false);
                    }
                });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ContactGroupsMultiSelectDialogPreference.this.onShow(/*dialog*/);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        linlaProgress = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        linlaListView = layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_listview);
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

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean notForUnselect) {

        asyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                if (notForUnselect) {
                    linlaListView.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getContactGroupsCache().isCached())
                    EditorProfilesActivity.getContactGroupsCache().getContactGroupList(_context);

                getValueCMSDP(notForUnselect);

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getContactGroupsCache().isCached())
                    EditorProfilesActivity.getContactGroupsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();

                if (notForUnselect) {
                    linlaListView.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantContactGroupsDialogPermissions(_context, this))
            refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        EditorProfilesActivity.getContactGroupsCache().cancelCaching();
        if (!EditorProfilesActivity.getContactGroupsCache().isCached())
            EditorProfilesActivity.getContactGroupsCache().clearCache(false);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
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
