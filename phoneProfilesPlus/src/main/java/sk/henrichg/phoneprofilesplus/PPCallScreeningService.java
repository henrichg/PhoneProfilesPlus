package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import java.util.List;

public class PPCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= 29) {
            CallResponse.Builder response = new CallResponse.Builder();
            //Log.e("PPCallScreeningService.onScreenCall", "Call screening service triggered");

            //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
            //Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);

            // get profile from shared preferences, saved in Profile.execute()
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(PPApplication.TMP_SHARED_PREFS_ACTIVATE_PROFILE_HELPER_EXECUTE, Context.MODE_PRIVATE);

            String contacts = sharedPreferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_CONTACTS, Profile.defaultValuesString.get(Profile.PREF_PROFILE_PHONE_CALLS_CONTACTS));
            String contactGroups = sharedPreferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_GROUPS, Profile.defaultValuesString.get(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_GROUPS));
            int contactListType = Integer.parseInt(sharedPreferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE)));
            boolean blockCalls = sharedPreferences.getBoolean(Profile.PREF_PROFILE_PHONE_CALLS_BLOCK_CALLS, Boolean.TRUE.equals(Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_PHONE_CALLS_BLOCK_CALLS)));
            boolean sendSMS = sharedPreferences.getBoolean(Profile.PREF_PROFILE_PHONE_CALLS_SEND_SMS, Boolean.TRUE.equals(Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_PHONE_CALLS_SEND_SMS)));
            String smsText = sharedPreferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_SMS_TEXT, Profile.defaultValuesString.get(Profile.PREF_PROFILE_PHONE_CALLS_SMS_TEXT));

            boolean profileFound = false;
            boolean phoneNumberFound = false;
            String calledPhoneNumber = callDetails.getHandle().getSchemeSpecificPart();
            if (
                 (
                  (contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||
                  ((contacts != null) && (!contacts.isEmpty())) ||
                  ((contactGroups != null) && (!contactGroups.isEmpty()))
                 ) && blockCalls
            ) {

                profileFound = true;

                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache != null) {
                    List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                    synchronized (PPApplication.contactsCacheMutex) {
                        contactList = contactsCache.getList(/*false*/);
                    }
                    phoneNumberFound = isPhoneNumberConfigured(contacts, contactGroups, contactListType, contactList, calledPhoneNumber);
                    if (contactList != null)
                        contactList.clear();
                }
            }

            if (profileFound && phoneNumberFound) {
                response.setDisallowCall(true);
                response.setRejectCall(true);
                if (Permissions.checkSendSMS(getApplicationContext())) {
                    if (sendSMS &&
                            (smsText != null) && (!smsText.isEmpty())) {
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(calledPhoneNumber, null, smsText, null, null);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            } else {
                response.setDisallowCall(false);
                response.setRejectCall(false);
            }
            response.setSilenceCall(false);
            response.setSkipCallLog(false);
            response.setSkipNotification(false);
            respondToCall(callDetails, response.build());

            //dataWrapper.invalidateDataWrapper();
        }
    }

    private boolean isPhoneNumberConfigured(String contacts, String contactGroups, int contactListType, List<Contact> contactList, String phoneNumber) {
        boolean phoneNumberFound = false;

        if (contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {

            // find phone number in groups
            String[] splits = contactGroups.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                if (!split.isEmpty()) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.isPhoneNumberConfigured", "(2) PPApplication.contactsCacheMutex");
                    synchronized (PPApplication.contactsCacheMutex) {
                        if (contactList != null) {
                            for (Contact contact : contactList) {
                                if (contact.groups != null) {
                                    long groupId = contact.groups.indexOf(Long.valueOf(split));
                                    if (groupId != -1) {
                                        // group found in contact
                                        if (contact.phoneId != 0) {
                                            String _phoneNumber = contact.phoneNumber;
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFound = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (phoneNumberFound)
                    break;
            }

            if (!phoneNumberFound) {
                // find phone number in contacts
                // contactId#phoneId|...
                splits = contacts.split(StringConstants.STR_SPLIT_REGEX);
                for (String split : splits) {
                    String[] splits2 = split.split(StringConstants.STR_SPLIT_CONTACTS_REGEX);

                    if ((!split.isEmpty()) &&
                            (splits2.length == 3) &&
                            (!splits2[0].isEmpty()) &&
                            (!splits2[1].isEmpty()) &&
                            (!splits2[2].isEmpty())) {
                        String contactPhoneNumber = splits2[1];
                        if (PhoneNumberUtils.compare(contactPhoneNumber, phoneNumber)) {
                            // phone number is in sensor configured
                            phoneNumberFound = true;
                            break;
                        }
                    }
                }
            }

            if (contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                phoneNumberFound = !phoneNumberFound;
        } else
           phoneNumberFound = true;

        return phoneNumberFound;
    }

}