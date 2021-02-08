package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

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

    @SuppressWarnings("unused")
    void getContactGroupList(Context context) {
        if (cached || caching) return;

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactList", "---- START");

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
                clearGroups(_contactList);
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
                                addGroup(contactId, contactGroupId, _contactList);
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
            //Log.e("ContactGroupsCache.getContactList", Log.getStackTraceString(e));
            //PPApplication.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactGroupsCache.getContactList", Log.getStackTraceString(e));
            PPApplication.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        }

        //if (cached) {
            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList/*, false*/);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                contactGroupList.clear();
                contactGroupList.addAll(_contactGroupList);
            }
        //}

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactList", "---- END");

        caching = false;
    }

    void getContactGroupListX(Context context) {
        if (cached || caching) return;

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "---- START");

        caching = true;
        //cancelled = false;

        ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
        if (contactsCache == null) {
            caching = false;
            return;
        }

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(1)");

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

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(2)");

//        long kolegoviaGroupId = 0;

        try {
            if (Permissions.checkContacts(context)) {
                clearGroups(_contactList);
                //contactsCache.clearGroups(_contactListWithoutNumber);

                List<Long> contactGroupIds = new ArrayList<>();

                String[] projection = new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.ACCOUNT_TYPE
                };
                String selection = ContactsContract.Groups.DELETED + "=0" + " AND " +
                //ContactsContract.Groups.GROUP_VISIBLE+"=1 ";
                ContactsContract.Groups.ACCOUNT_TYPE + "<>'vnd.sec.contact.phone'";
                String order = ContactsContract.Groups.TITLE + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, order);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
//                        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(3)");

//                        if (mCursor.getInt(mCursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)) > 0) {
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.groupId=" + mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Groups._ID)));
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.name=" + mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE)));
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.count=" + mCursor.getInt(mCursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)));
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.accountType=" + mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)));
//                        }

                        long contactGroupId = mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Groups._ID));
                        String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Groups.TITLE));
                        int count = mCursor.getInt(mCursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));

                        if (count > 0) {
                            contactGroupIds.add(contactGroupId);

                            ContactGroup aContactGroup = new ContactGroup();
                            aContactGroup.groupId = contactGroupId;
                            aContactGroup.name = name;
                            aContactGroup.count = count;

                            _contactGroupList.add(aContactGroup);

//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.groupId="+aContactGroup.groupId);
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.name="+aContactGroup.name);
//                            PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.count="+aContactGroup.count);

//                            if (aContactGroup.name.equals("Coworkers")) {
//                                PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "skupina Kolegovia");
//                                PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "aContactGroup.groupId="+aContactGroup.groupId);
//                                kolegoviaGroupId = contactGroupId;
//                            }
                        }

                        //if (cancelled)
                        //    break;

                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

//                PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(4)");

                String[] projectionGroup = new String[]{
                        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME
                };
                String selectionGroup = ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

                Cursor mCursorGroup = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projectionGroup, selectionGroup, null, null);
                if (mCursorGroup != null) {
                    while (mCursorGroup.moveToNext()) {
//                        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(5)");

                        long groupRowId = mCursorGroup.getLong(mCursorGroup.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
//                        PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "groupRowId=" + groupRowId);

                        for (long contactGroupId : contactGroupIds) {
                            if (groupRowId == contactGroupId) {
                                // contact is in contactGroupId group

                                long contactId = mCursorGroup.getLong(mCursorGroup.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
//                                String contactName = mCursorGroup.getString(mCursorGroup.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME));
//                                if (/*(contactGroupId == kolegoviaGroupId) &&*/ (contactName.equals("Marek Bobak"))) {
//                                    PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "contactGroupId=" + contactGroupId);
//                                    PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "contactId=" + contactId);
//                                    PPApplication.logE("------- ContactGroupsCache.getContactGroupListX", "contactName=" + contactName);
//                                }
                                addGroup(contactId, contactGroupId, _contactList);
                                //contactsCache.addGroup(contactId, contactGroupId, _contactListWithoutNumber);
                            }
                        }
                    }
                    mCursorGroup.close();
                }

//                PPApplication.logE("ContactGroupsCache.getContactGroupListX", "_contactGroupList.size()="+_contactGroupList.size());
//                PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "(6)");

                cached = true;
            }
//            else
//                PPApplication.logE("ContactGroupsCache.getContactGroupListX", "not granted permission");
        } catch (SecurityException e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            //PPApplication.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        } catch (Exception e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            PPApplication.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        }

        //if (cached) {
        synchronized (PPApplication.contactsCacheMutex) {
            contactsCache.updateContacts(_contactList/*, false*/);
            //contactsCache.updateContacts(_contactListWithoutNumber, true);

            contactGroupList.clear();
            contactGroupList.addAll(_contactGroupList);
        }
        //}

//        PPApplication.logE("[TEST BATTERY] ContactGroupsCache.getContactGroupListX", "---- END");

        caching = false;
    }

    public int getLength()
    {
        if (cached)
            synchronized (PPApplication.contactsCacheMutex) {
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
            synchronized (PPApplication.contactsCacheMutex) {
                return contactGroupList.get(position);
            }
        else
            return null;
    }

    // called only from ContactGroupsCache
    void clearGroups(List<Contact> _contactList) {
        if (_contactList == null)
            return;

        for (Contact contact : _contactList) {
            if (contact.groups != null) {
                //synchronized (PPApplication.contactsCacheMutex) {
                    contact.groups.clear();
                    contact.groups = null;
                //}
            }
        }
    }

    // called only from ContactGroupsCache
    void addGroup(long contactId, long contactGroupId, List<Contact> _contactList) {
        if (_contactList == null)
            return;

        /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
            Log.e("ContactsCache.addGroup", "contactGroupId=" + contactGroupId);
            Log.e("ContactsCache.addGroup", "contactId=" + contactId);
        }*/
        for (Contact contact : _contactList) {
            boolean contactFound = false;

            if (contact.contactId == contactId) {
                contactFound = true;
                /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                    Log.e("ContactsCache.addGroup", "contact found");
                    Log.e("ContactsCache.addGroup", "contact.phoneNumber="+contact.phoneNumber);
                }*/

                synchronized (PPApplication.contactsCacheMutex) {
                    if (contact.groups == null)
                        contact.groups = new ArrayList<>();

                    // search group in contact
                    boolean groupFound = false;
                    for (long groupId : contact.groups) {
                        if (groupId == contactGroupId) {
                            groupFound = true;
                            /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                                Log.e("ContactsCache.addGroup", "group found");
                            }*/
                            break;
                        }
                    }
                    if (!groupFound) {
                        // group not found, add it
                        contact.groups.add(contactGroupId);
                        /*if ((contactGroupId == 1) || (contactGroupId == 15) || (contactGroupId == 20)) {
                            Log.e("ContactsCache.addGroup", "group added");
                            Log.e("ContactsCache.addGroup", "contact.groups.size()="+contact.groups.size());
                        }*/
                    }
                }
            }

            if (contactFound)
                break;
        }
    }

    void clearCache()
    {
        synchronized (PPApplication.contactsCacheMutex) {
            contactGroupList.clear();
            cached = false;
            caching = false;
        }
    }

    boolean getCaching() {
        return caching;
    }

}
