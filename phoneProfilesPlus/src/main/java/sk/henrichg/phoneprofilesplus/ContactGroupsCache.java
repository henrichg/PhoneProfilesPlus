package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Comparator;
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

    /*
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
            List<Contact> contacts = contactsCache.getList();
            if (contacts != null)
                _contactList.addAll(contacts);
//            contacts = contactsCache.getList(true);
//            if (contacts != null)
//                _contactListWithoutNumber.addAll(contacts);
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
                //String order = ContactsContract.Groups.TITLE + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactGroupId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID));

                        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
                        if (name.equals("My Contacts")) name = context.getString(R.string.contact_group_name_myContacts);
                        if (name.equals("Family")) name = context.getString(R.string.contact_group_name_family);
                        if (name.equals("Friends")) name = context.getString(R.string.contact_group_name_friends);
                        if (name.equals("Coworkers")) name = context.getString(R.string.contact_group_name_coworkers);
                        if (name.equals("Starred in Android")) name = context.getString(R.string.contact_group_name_starred);
                        if (name.equals("Starred")) name = context.getString(R.string.contact_group_name_starred);

                        int count = mCursor.getInt(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.SUMMARY_COUNT));

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
                                long contactId = mCursorGroup.getLong(mCursorGroup.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
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

                Collections.sort(_contactGroupList, new ContactGroupsCache.ContactGroupsComparator());

                cached = true;
            }
        } catch (SecurityException e) {
            //Log.e("ContactGroupsCache.getContactGroupList", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        } catch (Exception e) {
            //Log.e("ContactGroupsCache.getContactGroupList", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            cached = false;
        }

        //if (cached) {
            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                contactGroupList.clear();
                contactGroupList.addAll(_contactGroupList);
            }
        //}

        caching = false;
    }
*/

    static String translateContactGroup(String name, Context context) {
        if (name.equals("My Contacts"))
            name = context.getString(R.string.contact_group_name_myContacts);
        if (name.equals("Family"))
            name = context.getString(R.string.contact_group_name_family);
        if (name.equals("Friends"))
            name = context.getString(R.string.contact_group_name_friends);
        if (name.equals("Coworkers"))
            name = context.getString(R.string.contact_group_name_coworkers);
        if (name.equals("Starred in Android"))
            name = context.getString(R.string.contact_group_name_starred);
        if (name.equals("Starred"))
            name = context.getString(R.string.contact_group_name_starred);
        return name;
    }

    void getContactGroupListX(Context context) {
        if (cached || caching) return;

        caching = true;
        //cancelled = false;

        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
        if (contactsCache == null) {
            caching = false;
            return;
        }

        ArrayList<ContactGroup> _contactGroupList = new ArrayList<>();
        ArrayList<ContactGroupsInEvent> _contactGroupInEventsCall = new ArrayList<>();
        ArrayList<ContactGroupsInEvent> _contactGroupInEventsSMS = new ArrayList<>();
        ArrayList<ContactGroupsInEvent> _contactGroupInEventsNotification = new ArrayList<>();
        ArrayList<ContactGroup> _oldContactGroupList = new ArrayList<>();

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

//        long kolegoviaGroupId = 0;

        DataWrapper dataWrapper = null;

        try {
            if (Permissions.checkContacts(context)) {

                if (contactGroupList.size() != 0) {
                    dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                    dataWrapper.fillEventList();

                    // fill array with events, which uses group cache
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesCall._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesCall._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_PHONE_CALL;
                            _contactGroupInEventsCall.add(contactGroupsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesSMS._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesSMS._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_SMS;
                            _contactGroupInEventsSMS.add(contactGroupsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesNotification._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesNotification._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_NOTIFICATION;
                            _contactGroupInEventsNotification.add(contactGroupsInEvent);
                        }
                    }
                }

                clearGroups(_contactList);
                //contactsCache.clearGroups(_contactListWithoutNumber);

                List<Long> contactGroupIds = new ArrayList<>();

                String[] projection = new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.ACCOUNT_TYPE
                };
                String selection = ContactsContract.Groups.DELETED + "=0"; //' + " AND " +
                //ContactsContract.Groups.GROUP_VISIBLE+"=1 ";
                //ContactsContract.Groups.ACCOUNT_TYPE + "<>'vnd.sec.contact.phone'";
                //String order = ContactsContract.Groups.TITLE + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactGroupId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID));

                        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
                        if (name != null) {
                            name = translateContactGroup(name, context);

                            String accountType = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_TYPE));

                            int count = mCursor.getInt(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.SUMMARY_COUNT));

                            //if (count > 0) {
                            contactGroupIds.add(contactGroupId);

                            ContactGroup aContactGroup = new ContactGroup();
                            aContactGroup.groupId = contactGroupId;
                            aContactGroup.name = name;
                            aContactGroup.count = count;
                            aContactGroup.accountType = accountType;

                            _contactGroupList.add(aContactGroup);

                        }

                        //if (cancelled)
                        //    break;

                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                String[] projectionGroup = new String[]{
                        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID//,
                        //ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME
                };
                String selectionGroup = ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

                Cursor mCursorGroup = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projectionGroup, selectionGroup, null, null);
                if (mCursorGroup != null) {
                    while (mCursorGroup.moveToNext()) {
                        long groupRowId = mCursorGroup.getLong(mCursorGroup.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                        for (long contactGroupId : contactGroupIds) {
                            if (groupRowId == contactGroupId) {
                                // contact is in contactGroupId group

                                long contactId = mCursorGroup.getLong(mCursorGroup.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                                addGroup(contactId, contactGroupId, _contactList);
                                //contactsCache.addGroup(contactId, contactGroupId, _contactListWithoutNumber);
                            }
                        }
                    }
                    mCursorGroup.close();
                }

                _contactGroupList.sort(new ContactGroupsComparator());

                synchronized (PPApplication.contactsCacheMutex) {
                    contactsCache.updateContacts(_contactList/*, false*/);
                    //contactsCache.updateContacts(_contactListWithoutNumber, true);

                    if (contactGroupList.size() != 0) {
                        // do copy of old contactGroupList
                        for (ContactGroup _contactGroup : contactGroupList) {
                            ContactGroup dGroup = new ContactGroup();
                            dGroup.groupId = _contactGroup.groupId;
                            dGroup.name = _contactGroup.name;
                            dGroup.accountType = _contactGroup.accountType;
                            _oldContactGroupList.add(dGroup);
                        }
                    }

                    updateContactGroups(_contactGroupList);

                    if (_oldContactGroupList.size() != 0) {
                        for (ContactGroupsInEvent contactsInEvent : _contactGroupInEventsCall) {
                            // for each contactsInEvent for call sensor
                            contactsInEvent.event._eventPreferencesCall._contacts =
                                    covertOldGroupToNewGroup(contactsInEvent, _oldContactGroupList);
                        }
                        for (ContactGroupsInEvent contactsInEvent : _contactGroupInEventsSMS) {
                            // for each contactsInEvent for sms sensor
                            contactsInEvent.event._eventPreferencesSMS._contacts =
                                    covertOldGroupToNewGroup(contactsInEvent, _oldContactGroupList);
                        }
                        for (ContactGroupsInEvent contactsInEvent : _contactGroupInEventsNotification) {
                            // for each contactsInEvent for notification sensor
                            contactsInEvent.event._eventPreferencesNotification._contacts =
                                    covertOldGroupToNewGroup(contactsInEvent, _oldContactGroupList);
                        }
                    }

                }

                cached = true;
            }
        } catch (SecurityException e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList/*, false*/);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                updateContactGroups(_contactGroupList);
            }

            cached = false;
        } catch (Exception e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList/*, false*/);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                updateContactGroups(_contactGroupList);
            }

            cached = false;
        }

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();

        caching = false;
    }

    int getLength()
    {
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached)
                return contactGroupList.size();
            else
                return 0;
        }
    }

    void updateContactGroups(List<ContactGroup> _contactGroupList) {
        contactGroupList.clear();
        contactGroupList.addAll(_contactGroupList);
    }

    List<ContactGroup> getList()
    {
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached)
                return contactGroupList;
            else
                return null;
        }
    }

    ContactGroup getContactGroup(int position)
    {
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached)
                return contactGroupList.get(position);
            else
                return null;
        }
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

        for (Contact contact : _contactList) {
            boolean contactFound = false;

            if (contact.contactId == contactId) {
                contactFound = true;

                synchronized (PPApplication.contactsCacheMutex) {
                    if (contact.groups == null)
                        contact.groups = new ArrayList<>();

                    // search group in contact
                    boolean groupFound = false;
                    for (long groupId : contact.groups) {
                        if (groupId == contactGroupId) {
                            groupFound = true;
                            break;
                        }
                    }
                    if (!groupFound) {
                        // group not found, add it
                        contact.groups.add(contactGroupId);
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

    private String covertOldGroupToNewGroup(ContactGroupsInEvent groupsInEvent, List<ContactGroup> _oldContactGroupList) {
        StringBuilder newGroups = new StringBuilder();

        String[] splits = groupsInEvent.groups.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            // for each group in groupsInEvent.groups
            long _groupId = Long.parseLong(split);

            boolean foundInNew = false;
            // search one group from groupsInEvent.groups
            for (ContactGroup oldGroup : _oldContactGroupList) {
                boolean foundInOld = false;
                if (oldGroup.groupId == _groupId)
                    foundInOld = true;
                if (foundInOld) {
                    // found contact in old list

                    // search it in new list
                    for (ContactGroup newGroup : contactGroupList) {
                        // search these fields in new contactGroupList
                        if (newGroup.name.equals(oldGroup.name) &&
                                newGroup.accountType.equals(oldGroup.accountType)) {
                            foundInNew = true;
                            // update group to new group in event
                            if (newGroups.length() > 0)
                                newGroups.append("|");
                            newGroups.append(newGroup.groupId);
                            break;
                        }
                    }
                    break;
                }
            }
            if (!foundInNew) {
                // get back old contact
                if (newGroups.length() > 0)
                    newGroups.append("|");
                newGroups.append(split);
            }
        }

        return newGroups.toString();
    }

    private static class ContactGroupsComparator implements Comparator<ContactGroup> {

        public int compare(ContactGroup lhs, ContactGroup rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }

    private static class ContactGroupsInEvent {
        Event event = null;
        String groups = null;
        //int sensorType = -1;
    }

}
