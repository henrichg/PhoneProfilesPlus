package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class PPCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= 29) {
            //Log.e("PPCallScreeningService.onScreenCall", "Call screening service triggered");

            final Context appContext = getApplicationContext();

            if (callDetails.getCallDirection() ==  Call.Details.DIRECTION_INCOMING) {
//                Log.e("PPCallScreeningService.onScreenCall", "incomming call");

                Uri callHandle =  callDetails.getHandle();
                if (callHandle != null) {

                    //Runnable runnable = () -> { // NOT WORKING BLOCK CALL WTH THIS !!!
                        boolean phoneNumberFound = false;

                        String callingPhoneNumber = callHandle.getSchemeSpecificPart();
                        //Log.e("PPCallScreeningService.onScreenCall", "callingPhoneNumber="+callingPhoneNumber);

                        Runnable runnable = () -> {
                            EventsHandler eventsHandler = new EventsHandler(appContext);

                            Log.e("PPCallScreeningService.onScreenCall", "call of EventsHandler");
                            Calendar now = Calendar.getInstance();
                            long time = now.getTimeInMillis();
                            eventsHandler.setEventCallScreeningParameters(callingPhoneNumber, time);
                            eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_CALL_SCREENING});
                        };
                        PPApplicationStatic.createBasicExecutorPool();
                        PPApplication.basicExecutorPool.submit(runnable);

                        boolean sendSMS = false;
                        String smsText = "";

                        List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();
                        for (Event event : eventList) {
                            if (event._eventPreferencesCallScreening._enabled &&
                                    event._eventPreferencesCallScreening.isRunnable(appContext)) {
                                String contacts = event._eventPreferencesCallScreening._contacts;
                                String contactGroups = event._eventPreferencesCallScreening._contactGroups;
                                //int contactListType = event._eventPreferencesCallScreening._contactListType;
                                boolean blockCalls = event._eventPreferencesCallScreening._blockCalls;
                                sendSMS = event._eventPreferencesCallScreening._sendSMS;
                                smsText = event._eventPreferencesCallScreening._smsText;

                                if (
                                        (
                                            /*(contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                                            ((contacts != null) && (!contacts.isEmpty())) ||
                                                    ((contactGroups != null) && (!contactGroups.isEmpty()))
                                        ) && blockCalls
                                ) {
                                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                                    if (contactsCache != null) {
                                        List<Contact> contactList;
//                                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                                        synchronized (PPApplication.contactsCacheMutex) {
                                            contactList = contactsCache.getList(/*false*/);
                                        }
                                        phoneNumberFound = isPhoneNumberConfigured(contacts, contactGroups, /*contactListType,*/ contactList, callingPhoneNumber);
                                        if (contactList != null)
                                            contactList.clear();
                                    }
                                }
                            }
                            if (phoneNumberFound)
                                break;
                        }

                        //Log.e("PPCallScreeningService.onScreenCall", "phoneNumberFound="+phoneNumberFound);

                        CallResponse.Builder response = new CallResponse.Builder();

                        if (phoneNumberFound) {
                            //block call
                            response.setDisallowCall(true);
                            response.setRejectCall(true);

                            if (Permissions.checkSendSMS(appContext)) {
                                // send sms
                                if (sendSMS && (!callingPhoneNumber.isEmpty()) &&
                                        (smsText != null) && (!smsText.isEmpty())) {
                                    try {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(callingPhoneNumber, null, smsText, null, null);
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
                        return;
                    //};
                    //PPApplicationStatic.createEventsHandlerExecutor();
                    //PPApplication.eventsHandlerExecutor.submit(runnable);
                }
            }

            CallResponse.Builder response = new CallResponse.Builder();
            response.setDisallowCall(false);
            response.setRejectCall(false);

            response.setSilenceCall(false);
            response.setSkipCallLog(false);
            response.setSkipNotification(false);
            respondToCall(callDetails, response.build());
        }
    }

    private boolean isPhoneNumberConfigured(String contacts, String contactGroups, /*int contactListType,*/ List<Contact> contactList, String phoneNumber) {
        boolean phoneNumberFound = false;

        //if (contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {

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

            //if (contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
            //    phoneNumberFound = !phoneNumberFound;
        //} else
        //   phoneNumberFound = true;

        return phoneNumberFound;
    }

}