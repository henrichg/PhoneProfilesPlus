package sk.henrichg.phoneprofilesplus;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.List;

public class PPCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= 29) {
            CallResponse.Builder response = new CallResponse.Builder();
            Log.e("CallScreeningService.onScreenCall", "Call screening service triggered");

            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
            dataWrapper.fillEventList();

            boolean eventFound = false;
            for (Event _event : dataWrapper.eventList) {
                if ((_event.getStatus() != Event.ESTATUS_STOP) &&
                        (_event._eventPreferencesCall._enabled)) {
                    Log.e("CallScreeningService.onScreenCall", "schema specific part="+callDetails.getHandle().getSchemeSpecificPart());

                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                    if (contactsCache != null) {
                        List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                        synchronized (PPApplication.contactsCacheMutex) {
                            contactList = contactsCache.getList(/*false*/);
                        }
                        eventFound = _event._eventPreferencesCall.isPhoneNumberConfigured(contactList, callDetails.getHandle().getSchemeSpecificPart());
                        if (contactList != null)
                            contactList.clear();
                    }

                    if (eventFound)
                        break;
                }
            }

            if (eventFound) {
                response.setDisallowCall(true);
                response.setRejectCall(true);
                response.setSilenceCall(false);
                response.setSkipCallLog(false);
                response.setSkipNotification(false);
            } else {
                response.setDisallowCall(false);
                response.setRejectCall(false);
                response.setSilenceCall(false);
                response.setSkipCallLog(false);
                response.setSkipNotification(false);
            }
            respondToCall(callDetails, response.build());

            dataWrapper.invalidateDataWrapper();
        }
    }

}
