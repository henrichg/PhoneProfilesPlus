package sk.henrichg.phoneprofilesplus;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;

import java.util.List;

public class PPCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= 29) {
            CallResponse.Builder response = new CallResponse.Builder();
            //Log.e("PPCallScreeningService.onScreenCall", "Call screening service triggered");

            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
            Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);

            boolean profileFound = false;
            boolean phoneNumberFound = false;
            if (
                 (((activatedProfile._phoneCallsContacts != null) && (!activatedProfile._phoneCallsContacts.isEmpty())) ||
                  ((activatedProfile._phoneCallsContactGroups != null) && (!activatedProfile._phoneCallsContactGroups.isEmpty()))) &&
                 activatedProfile._phoneCallsBlockCalls
            ) {
                profileFound = true;

                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache != null) {
                    List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                    synchronized (PPApplication.contactsCacheMutex) {
                        contactList = contactsCache.getList(/*false*/);
                    }
                    phoneNumberFound = isPhoneNumberConfigured(activatedProfile, contactList,
                            callDetails.getHandle().getSchemeSpecificPart());
                    if (contactList != null)
                        contactList.clear();
                }
            }

            if (profileFound && phoneNumberFound) {
                response.setDisallowCall(true);
                response.setRejectCall(true);
                // TODO sem daj vygenerovanie notifikacie, na ktoru ked kliknem posle smsku.
                //  Cize pridaj dalsie dva parametre: checkbox "Send SMS" a EeditText "SMS text".
                //  Vygeneruj notifikaciu len ak ma zakskrtnuty checkbox "Send SMS".
                //  Potom mozem posat tak, ze vygenerujem Intent s parametrami, co naplni default
                //  sms aplikaciu: poslanie sms blokovanemu tel. cislu + text a uzivatel len stlaci "Poslat"?
                //  Uvazuj nad tym, ze by som pouzival texte wildcard na volajuce tel. cislo, nazov kontaktu
                //  a uzivatel by ho mohol pouzit v texte.
            } else {
                response.setDisallowCall(false);
                response.setRejectCall(false);
            }
            response.setSilenceCall(false);
            response.setSkipCallLog(false);
            response.setSkipNotification(false);
            respondToCall(callDetails, response.build());

            dataWrapper.invalidateDataWrapper();
        }
    }

    private boolean isPhoneNumberConfigured(Profile profile, List<Contact> contactList, String phoneNumber) {
        boolean phoneNumberFound = false;

        //if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {

            // find phone number in groups
            String[] splits = profile._phoneCallsContactGroups.split(StringConstants.STR_SPLIT_REGEX);
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
                splits = profile._phoneCallsContacts.split(StringConstants.STR_SPLIT_REGEX);
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

            if (profile._phoneCallsContactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                phoneNumberFound = !phoneNumberFound;
        //} else
        //   phoneNumberFound = true;

        return phoneNumberFound;
    }

}