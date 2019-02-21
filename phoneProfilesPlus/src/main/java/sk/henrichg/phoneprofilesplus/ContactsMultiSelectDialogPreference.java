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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class ContactsMultiSelectDialogPreference extends DialogPreference
{

    private final Context _context;
    private String value = "";

    private AlertDialog mDialog;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ContactsMultiSelectPreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    List<Contact> contactList;

    public ContactsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactsCache() == null)
            EditorProfilesActivity.createContactsCache();

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
                    // fill with strings of contacts separated with |
                    value = "";
                    if (contactList != null)
                    {
                        for (Contact contact : contactList)
                        {
                            if (contact.checked)
                            {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + contact.contactId + "#" + contact.phoneId;
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
        View layout = inflater.inflate(R.layout.activity_contacts_multiselect_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ContactsMultiSelectDialogPreference.this.onShow(/*dialog*/);
            }
        });

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        rellaData = layout.findViewById(R.id.contacts_multiselect_pref_dlg_rella_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        //noinspection ConstantConditions
        FastScrollRecyclerView listView = layout.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ContactsMultiSelectPreferenceAdapter(this);
        listView.setAdapter(listAdapter);

        final Button unselectAllButton = layout.findViewById(R.id.contacts_multiselect_pref_dlg_unselect_all);
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
                    if (!EditorProfilesActivity.getContactsCache().cached)
                        EditorProfilesActivity.getContactsCache().getContactList(_context);

                    getValueCMSDP(notForUnselect);

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    if (!EditorProfilesActivity.getContactsCache().cached)
                        EditorProfilesActivity.getContactsCache().clearCache(false);

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
        if (Permissions.grantContactsDialogPermissions(_context))
            refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        EditorProfilesActivity.getContactsCache().cancelCaching();
        if (!EditorProfilesActivity.getContactsCache().cached)
            EditorProfilesActivity.getContactsCache().clearCache(false);
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
        contactList = EditorProfilesActivity.getContactsCache().getList();
        if (contactList != null)
        {
            String[] splits = value.split("\\|");
            for (Contact contact : contactList)
            {
                contact.checked = false;
                for (String split : splits) {
                    try {
                        String[] splits2 = split.split("#");
                        long contactId = Long.parseLong(splits2[0]);
                        long phoneId = Long.parseLong(splits2[1]);
                        if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                            contact.checked = true;
                    } catch (Exception ignored) {
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < contactList.size()) {
                Contact contact = contactList.get(i);
                if (contact.checked) {
                    contactList.remove(i);
                    contactList.add(ich, contact);
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
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_ID};
                    String[] splits2 = splits[0].split("#");
                    String selection = ContactsContract.Contacts._ID + "=" + splits2[0];
                    Cursor mCursor = _context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + splits2[0] + " AND " +
                                    ContactsContract.CommonDataKinds.Phone._ID + "=" + splits2[1];
                            Cursor phones = _context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, null);
                            if (phones != null) {
                                //while (phones.moveToNext()) {
                                if (phones.moveToFirst()) {
                                    found = true;
                                    prefVolumeDataSummary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + '\n' +
                                            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    //break;
                                }
                                phones.close();
                            }
                            if (found)
                                break;
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
