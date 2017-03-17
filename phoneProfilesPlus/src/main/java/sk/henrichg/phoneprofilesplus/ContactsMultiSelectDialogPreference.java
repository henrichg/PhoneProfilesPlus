package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andraskindler.quickscroll.QuickScroll;

import java.util.List;

public class ContactsMultiSelectDialogPreference extends DialogPreference
{

    Context _context = null;
    String value = "";

    MaterialDialog mDialog;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

    private ContactsMultiselectPreferenceAdapter listAdapter;

    public ContactsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactsCache() == null)
            EditorProfilesActivity.createContactsCache();

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
                .customView(R.layout.activity_contacts_multiselect_pref_dialog, false)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // sem narvi stringy kontatkov oddelenych |
                            value = "";
                            List<Contact> contactList = EditorProfilesActivity.getContactsCache().getList();
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

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        linlaProgress = (LinearLayout)layout.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
        linlaListView = (LinearLayout)layout.findViewById(R.id.contacts_multiselect_pref_dlg_linla_listview);
        ListView listView = (ListView)layout.findViewById(R.id.contacts_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Contact contact = (Contact)listAdapter.getItem(position);
                contact.toggleChecked();
                ContactViewHolder viewHolder = (ContactViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contact.checked);
            }
        });

        listAdapter = new ContactsMultiselectPreferenceAdapter(_context);
        listView.setAdapter(listAdapter);

        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorQSScrollbar, tv, true);
        int colorQSScrollbar = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarInactive, tv, true);
        int colorQSHandlebarInactive = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarActive, tv, true);
        int colorQSHandlebarActive = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarStroke, tv, true);
        int colorQSHandlebarStroke = tv.data;

        final QuickScroll quickscroll = (QuickScroll)layout.findViewById(R.id.contacts_multiselect_pref_dlg_quickscroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, listView, listAdapter, QuickScroll.STYLE_HOLO, colorQSScrollbar);
        quickscroll.setHandlebarColor(colorQSHandlebarInactive, colorQSHandlebarActive, colorQSHandlebarStroke);
        quickscroll.setIndicatorColor(colorQSHandlebarActive, colorQSHandlebarActive, Color.WHITE);
        quickscroll.setFixedSize(1);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ContactsMultiSelectDialogPreference.this.onShow(dialog);
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public void refreshListView(final boolean notForUnselect) {

        new AsyncTask<Void, Integer, Void>() {

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
                if (!EditorProfilesActivity.getContactsCache().isCached())
                    EditorProfilesActivity.getContactsCache().getContactList(_context);

                getValueCMSDP(notForUnselect);

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getContactsCache().isCached())
                    EditorProfilesActivity.getContactsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    linlaListView.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    public void onShow(DialogInterface dialog) {
        if (Permissions.grantContactsDialogPermissions(_context, this))
            refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);
        EditorProfilesActivity.getContactsCache().cancelCaching();
        if (!EditorProfilesActivity.getContactsCache().isCached())
            EditorProfilesActivity.getContactsCache().clearCache(false);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
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
            // sem narvi default string kontaktov oddeleny |
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
        List<Contact> contactList = EditorProfilesActivity.getContactsCache().getList();
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
