package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

class ContactGroupsCache {

    private final ArrayList<ContactGroup> contactGroupList;
    private boolean cached;
    private boolean caching;
    //private boolean cancelled;

    ContactGroupsCache()
    {
        contactGroupList = new ArrayList<>();
        cached = false;
        caching = false;
    }

    void getContactGroupList(Context context) {
        if (cached || caching) return;

        caching = true;
        //cancelled = false;

        ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
        if (contactsCache == null) {
            caching = false;
            return;
        }

        ArrayList<ContactGroup> _contactGroupList = new ArrayList<>();

        ArrayList<Contact> _contactList = new ArrayList<>();
        //ArrayList<Contact> _contactListWithoutNumber = new ArrayList<>();
        synchronized (PPApplication.contactsCacheMutex) {
            List<Contact> contacts = contactsCache.getList(/*false*/);
            if (contacts != null)
                _contactList.addAll(contacts);
            /*contacts = contactsCache.getList(true);
            if (contacts != null)
                _contactListWithoutNumber.addAll(contacts);*/
        }

        try {
            if (Permissions.checkContacts(context)) {
                contactsCache.clearGroups(_contactList);
                //contactsCache.clearGroups(_contactListWithoutNumber);

                String[] projection = new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.SUMMARY_COUNT};
                String selection = ContactsContract.Groups.DELETED + "!='1'";// + " AND "+
                //ContactsContract.Groups.GROUP_VISIBLE+"!='0' ";
                String order = ContactsContract.Groups.TITLE + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, order);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactGroupId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Groups._ID));
                        String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                        int count = mCursor.getInt(mCursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));

                        if (count > 0) {
                            ContactGroup aContactGroup = new ContactGroup();
                            aContactGroup.groupId = contactGroupId;
                            aContactGroup.name = name;
                            aContactGroup.count = count;

                            _contactGroupList.add(aContactGroup);
                        }

                        //if (cancelled)
                        //    break;

                        String[] projectionGroup = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                        String selectionGroup = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                        String[] selectionGroupArgs = new String[]{String.valueOf(contactGroupId)};
                        Cursor mCursorGroup = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projectionGroup, selectionGroup, selectionGroupArgs, null);
                        if (mCursorGroup != null) {
                            while (mCursorGroup.moveToNext()) {
                                long contactId = mCursorGroup.getLong(mCursorGroup.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                                /*if (name.equals("Family")) {
                                    Log.e("ContactGroupsCache.getContactGroupList", "contactGroupId=" + contactGroupId);
                                    Log.e("ContactGroupsCache.getContactGroupList", "contactId=" + contactId);
                                }*/
                                contactsCache.addGroup(contactId, contactGroupId, _contactList);
                                //contactsCache.addGroup(contactId, contactGroupId, _contactListWithoutNumber);
                            }
                            mCursorGroup.close();
                        }
                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                cached = true;
            }
        } catch (SecurityException e) {
            Log.e("ContactGroupsCache.getContactList", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);

            _contactGroupList.clear();
            contactsCache.clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        } catch (Exception e) {
            Log.e("ContactGroupsCache.getContactList", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);

            _contactGroupList.clear();
            contactsCache.clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        }

        synchronized (PPApplication.contactGroupsCacheMutex) {
            contactsCache.updateContacts(_contactList/*, false*/);
            //contactsCache.updateContacts(_contactListWithoutNumber, true);

            contactGroupList.clear();
            contactGroupList.addAll(_contactGroupList);
        }

        caching = false;
    }

    public int getLength()
    {
        if (cached)
            synchronized (PPApplication.contactGroupsCacheMutex) {
                return contactGroupList.size();
            }
        else
            return 0;
    }

    public List<ContactGroup> getList()
    {
        if (cached)
            return contactGroupList;
        else
            return null;
    }

    ContactGroup getContactGroup(int position)
    {
        if (cached)
            synchronized (PPApplication.contactGroupsCacheMutex) {
                return contactGroupList.get(position);
            }
        else
            return null;
    }

    void clearCache()
    {
        synchronized (PPApplication.contactGroupsCacheMutex) {
            contactGroupList.clear();
            cached = false;
            caching = false;
        }
    }

    boolean getCaching() {
        return caching;
    }

}
