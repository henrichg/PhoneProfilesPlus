package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

public class PPCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= 29) {
//            Log.e("PPCallScreeningService.onScreenCall", "Call screening service triggered");

            final Context appContext = getApplicationContext();

            Uri callHandle =  callDetails.getHandle();
            if (callHandle != null) //noinspection ExtractMethodRecommender
            {

                //Runnable runnable = () -> { // NOT WORKING BLOCK CALL WTH THIS !!!
                final String callingPhoneNumber = callHandle.getSchemeSpecificPart();
//                Log.e("PPCallScreeningService.onScreenCall", "callingPhoneNumber="+callingPhoneNumber);
                final int callDirection = callDetails.getCallDirection();

                // Required is direct call of EventsHandler, because must be tested,
                // event status (must be running to block call).
                // If this is longer then 5 seconds, then system unbind this service and
                // used is default call screening, call is ringing. Uff :-)
                //Runnable runnable = () -> {
                    EventsHandler eventsHandler = new EventsHandler(appContext);

//                    Log.e("PPCallScreeningService.onScreenCall", "call of EventsHandler - start");
                    Calendar now = Calendar.getInstance();
                    long time = now.getTimeInMillis();
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPCallScreeningService.onScreenCall", "SENSOR_TYPE_CALL_CONTROL");
                    eventsHandler.setEventCallControlParameters(callingPhoneNumber, time, callDirection);
                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_CALL_CONTROL});
//                    Log.e("PPCallScreeningService.onScreenCall", "call of EventsHandler - end");
                //};
                //PPApplicationStatic.createBasicExecutorPool();
                //PPApplication.basicExecutorPool.submit(runnable);

                //GlobalUtils.sleep(6000);

                CallResponse.Builder response = new CallResponse.Builder();

                if (callDirection ==  Call.Details.DIRECTION_INCOMING) //noinspection ExtractMethodRecommender
                {
                    // blok call only for incoming call

//                    Log.e("PPCallScreeningService.onScreenCall", "***** (1) *****");

                    //noinspection ExtractMethodRecommender
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] PPCallScreeningService.onScreenCall", "PPApplicationStatic.getContactsCache()");
                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                    List<Contact> contactList = null;
                    if (contactsCache != null) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.doHandleEvent", "PPApplication.contactsCacheMutex");
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] PPCallScreeningService.onScreenCall", "contactsCache.getList()");
                        contactList = contactsCache.getList(/*false*/);
                    }

                    boolean blockCallingPhoneNumber = false;
                    boolean sendSMS = false;
                    String smsText = "";

                    if (contactList != null) {
                        List<Event> eventList = DatabaseHandler.getInstance(appContext).getCallControlEvents();
//                        Log.e("PPCallScreeningService.onScreenCall", "eventList.size()="+eventList.size());
                        for (Event event : eventList) {
                            if (event._eventPreferencesCallControl._enabled &&
                                event._eventPreferencesCallControl.isRunnable(appContext) &&
                                event.getStatus() == Event.ESTATUS_RUNNING) {

                                // call event._eventPreferencesCallScreening.doHableEvent()
                                // to get sensor pass for event
                                //EventsHandler eventsHandler = new EventsHandler(appContext);
                                //event._eventPreferencesCallScreening.doHandleEventOnlyCheckPhoneNumberAndDirection(eventsHandler, callingPhoneNumber, callDirection);
                                //if ((!eventsHandler.notAllowedCallScreening) && eventsHandler.callScreeningPassed) {
                                    // snsor is passed block call
                                    //Log.e("PPCallScreeningService.onScreenCall", "semsor passed for event="+event._name);

                                    String contacts = event._eventPreferencesCallControl._contacts;
                                    String contactGroups = event._eventPreferencesCallControl._contactGroups;
                                    //int contactListType = event._eventPreferencesCallScreening._contactListType;
                                    boolean notInContacts = event._eventPreferencesCallControl._notInContacts;
                                    int direction = event._eventPreferencesCallControl._callDirection;
                                    boolean blockCalls = event._eventPreferencesCallControl._blockCalls;
                                    sendSMS = event._eventPreferencesCallControl._sendSMS;
                                    smsText = event._eventPreferencesCallControl._smsText;

                                    if (notInContacts) {
                                        if (direction != EventPreferencesCallControl.CALL_DIRECTION_OUTGOING)
                                            blockCallingPhoneNumber = !isPhoneNumberInContacts(contactList, callingPhoneNumber);
                                    } else {
                                        if ((
                                                /*(contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                                                ((contacts != null) && (!contacts.isEmpty())) ||
                                                        ((contactGroups != null) && (!contactGroups.isEmpty()))
                                        ) && (direction != EventPreferencesCallControl.CALL_DIRECTION_OUTGOING)
                                                && blockCalls) {
                                            blockCallingPhoneNumber = isPhoneNumberConfigured(contacts, contactGroups, /*contactListType,*/ contactList, callingPhoneNumber);
                                        }
                                    }
                                //}
                            }
                            if (blockCallingPhoneNumber)
                                break;
                        }

                        contactList.clear();
                    }

//                    Log.e("PPCallScreeningService.onScreenCall", "blockCallingPhoneNumber="+blockCallingPhoneNumber);

                    if (blockCallingPhoneNumber) {
                        //block call

                        // blocked call to Activity log
                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_CALL_CONTROL_BLOCKED_CALL, null, callingPhoneNumber, "");

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
//                        Log.e("PPCallScreeningService.onScreenCall", "***** (2) *****");
                        response.setDisallowCall(false);
                        response.setRejectCall(false);
                    }
                } else {
//                    Log.e("PPCallScreeningService.onScreenCall", "***** (3) *****");
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

//            Log.e("PPCallScreeningService.onScreenCall", "***** (4) *****");

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

    private boolean isPhoneNumberInContacts(List<Contact> contactList, String phoneNumber) {
//        Log.e("PPCallScreeningService.isPhoneNumberInContacts", "phoneNumber="+phoneNumber);

        boolean phoneNumberInContacts = false;

        synchronized (PPApplication.contactsCacheMutex) {
            if (contactList != null) {
                for (Contact contact : contactList) {
                    if (contact.phoneId != 0) {
                        String _phoneNumber = contact.phoneNumber;
//                        Log.e("PPCallScreeningService.isPhoneNumberInContacts", "_phoneNumber="+_phoneNumber);
                        if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                            phoneNumberInContacts = true;
                            break;
                        }
                    }
                }
            }
        }

        return phoneNumberInContacts;
    }

}