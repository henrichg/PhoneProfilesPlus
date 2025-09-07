package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class PPExtenderBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_PACKAGE_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".package_name";
    private static final String EXTRA_CLASS_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".class_name";

    private static final String EXTRA_ORIGIN = PPApplication.PACKAGE_NAME_EXTENDER + ".origin";
    private static final String EXTRA_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".time";
    private static final String EXTRA_SUBSCRIPTION_ID = PPApplication.PACKAGE_NAME_EXTENDER + ".subscription_id";

    //private static final String EXTRA_SERVICE_PHONE_EVENT = PPApplication.PACKAGE_NAME_EXTENDER + ".service_phone_event";
    private static final String EXTRA_CALL_EVENT_TYPE = PPApplication.PACKAGE_NAME_EXTENDER + ".call_event_type";
    private static final String EXTRA_PHONE_NUMBER = PPApplication.PACKAGE_NAME_EXTENDER + ".phone_number";
    private static final String EXTRA_EVENT_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".event_time";
    private static final String EXTRA_SIM_SLOT = PPApplication.PACKAGE_NAME_EXTENDER + ".sim_slot";

    static final String EXTRA_DISPLAY_NOTIFICATION = "EXTRA_DISPLAY_NOTIFICATION";


    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";

    private static final int ACCESSIBILITY_SERVICE_CONNECTED_DELAY = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] PPExtenderBroadcastReceiver.onReceive", "xxx");

//        Log.e("PPExtenderBroadcastReceiver.onReceive", "**********");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

//        Log.e("PPExtenderBroadcastReceiver.onReceive", "xxxxxxxxxxxxxxx");

        if ((intent == null) || (intent.getAction() == null))
            return;

//        PPApplicationStatic.logE("[IN_BROADCAST] PPExtenderBroadcastReceiver.onReceive", intent.getAction());
//        Log.e("PPExtenderBroadcastReceiver.onReceive", intent.getAction());

        final Context appContext = context.getApplicationContext();

        switch (intent.getAction()) {
            case PPApplication.ACTION_PPPEXTENDER_STARTED:
                isAccessibilityServiceEnabled(appContext, true, true
                        /*, "PPExtenderBroadcastReceiver.onReceive (ACTION_PPPEXTENDER_STARTED)"*/);
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED:
                if (PPApplication.accessibilityServiceForPPPExtenderConnected != 1) {
                    // cancel ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG
                    PPApplicationStatic._cancelWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, false);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);
                    notificationManager.cancel(
                            PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_TAG,
                            PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_ID);

                    PPApplication.accessibilityServiceForPPPExtenderConnected = 1;
                    Runnable runnable = () -> {
                        synchronized (PPApplication.handleEventsMutex) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");

                            //Context appContext= appContextWeakRef.get();
                            //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if (PhoneProfilesService.getInstance() != null) {
                                    DataWrapper dataWrapper2 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                    dataWrapper2.fillEventList();
                                    //dataWrapper2.fillProfileList(false, false);
                                    PhoneProfilesServiceStatic.registerPPPExtenderReceiver(true, dataWrapper2, appContext);
//                                PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PPExtenderBroadcastReceiver.onReceive", "ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                                    PPApplicationStatic.restartAllScanners(appContext, false);

                                    PPApplicationStatic.addActivityLog(dataWrapper2.context, PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_ENABLED,
                                            null, null, "");

                                    PPApplicationStatic.logE("[DELAYED_EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "dataWrapper.restartEventsWithDelay");
                                    dataWrapper2.restartEventsWithDelay(/*false,*/ true, false, true, PPApplication.ALTYPE_UNDEFINED);
                                }

                            } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            //}
                        }
                    };
                    PPApplicationStatic.logE("[EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "(1) xxx");
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable);
                }
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND:
                // cancel ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG
                PPApplicationStatic._cancelWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, false);

                PPApplication.accessibilityServiceForPPPExtenderConnected = 2;

                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_UNBIND,
                        null, null, "");

                Runnable runnable2 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");

                    synchronized (PPApplication.handleEventsMutex) {

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            setApplicationInForeground(appContext, "");

                            if (EventStatic.getGlobalEventsRunning(appContext)) {
//                              PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "ACTION_ACCESSIBILITY_SERVICE_UNBIND -> SENSOR_TYPE_APPLICATION,SENSOR_TYPE_DEVICE_ORIENTATION");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.setEventApplicationParameters("", 0);
                                eventsHandler.handleEvents(new int[]{
                                        EventsHandler.SENSOR_TYPE_APPLICATION,
                                        EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION});
                            }

                            /*
                            boolean applicationsAllowed = false;
                            boolean orientationAllowed = false;

                            DataWrapper dataWrapper4 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            dataWrapper4.fillEventList();
                            boolean applicationExists = dataWrapper4.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION);
                            if (applicationExists)
                                applicationsAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                            boolean orientationExists = dataWrapper4.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION);
                            if (orientationExists)
                                orientationAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                            dataWrapper4.invalidateDataWrapper();

                            if ((applicationsAllowed) || (orientationAllowed)) {
                                setApplicationInForeground(appContext, "");

                                if (EventStatic.getGlobalEventsRunning(appContext)) {
                                    //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);

                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    if (applicationExists) {
        //                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (2)");
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                    }
                                    if (orientationExists) {
        //                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (2)");
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                    }
                                }
                            }
                            */

                        } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        //}

                    }
                };
                PPApplicationStatic.logE("[EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "(2) xxx");
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable2);

                break;
            case PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED:
                try {
                    final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                    final String className = intent.getStringExtra(EXTRA_CLASS_NAME);
                    //Log.e("PPExtenderBroadcastReceiver.onReceive", "(1) ACTION_FOREGROUND_APPLICATION_CHANGED packageName="+packageName);
                    //Log.e("PPExtenderBroadcastReceiver.onReceive", "(1) ACTION_FOREGROUND_APPLICATION_CHANGED className="+className);

                    if ((packageName != null) && (className != null)) {
                        ComponentName componentName = new ComponentName(packageName, className);

                        ActivityInfo activityInfo = tryGetActivity(appContext, componentName);
                        boolean isActivity = activityInfo != null;
                        if (isActivity) {
                            if (EventStatic.getGlobalEventsRunning(appContext)) {
                                Runnable runnable3 = () -> {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");

                                    setApplicationInForeground(appContext, packageName);

                                    Calendar now = Calendar.getInstance();
                                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                    final long _time = now.getTimeInMillis() + gmtOffset;

                                    synchronized (PPApplication.handleEventsMutex) {

                                        //Context appContext= appContextWeakRef.get();
                                        //if (appContext != null) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_FOREGROUND_APPLICATION_CHANGED);
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            //Log.e("PPExtenderBroadcastReceiver.onReceive", "(2) ACTION_FOREGROUND_APPLICATION_CHANGED");
//                                          PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "ACTION_FOREGROUND_APPLICATION_CHANGED -> SENSOR_TYPE_APPLICATION,SENSOR_TYPE_DEVICE_ORIENTATION");
//                                          PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "ACTION_FOREGROUND_APPLICATION_CHANGED -> packageName="+packageName);
                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            eventsHandler.setEventApplicationParameters(packageName, _time);
                                            eventsHandler.handleEvents(new int[]{
                                                    EventsHandler.SENSOR_TYPE_APPLICATION,
                                                    EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION});

                                            /*
                                            DataWrapper dataWrapper3 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                            dataWrapper3.fillEventList();
                                            //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            if (dataWrapper3.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION)) {
    //                                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (1)");
                                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                            }
                                            if (dataWrapper3.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION)) {
    //                                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (1)");
                                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                            }
                                            dataWrapper3.invalidateDataWrapper();
                                            */

                                        } catch (Exception e) {
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                            PPApplicationStatic.recordException(e);
                                        } finally {
                                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                                try {
                                                    wakeLock.release();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                        //}
                                    }
                                };
                                PPApplicationStatic.logE("[EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "(3) xxx");
                                PPApplicationStatic.createEventsHandlerExecutor();
                                PPApplication.eventsHandlerExecutor.submit(runnable3);
                            }
                        }
                    }
                } catch (Exception e) {
                    //Log.e("PPExtenderBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0) {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_FORCE_STOP_APPLICATIONS_END);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId, false);
                            if (profile != null) {
                                SharedPreferences sharedPreferences = appContext.getSharedPreferences(PPApplication.TMP_SHARED_PREFS_PPP_EXTENDER_BROADCAST_RECEIVER, Context.MODE_PRIVATE);
                                profile.saveProfileToSharedPreferences(sharedPreferences);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "call of ActivateProfileHelper.executeForInteractivePreferences");
                                ActivateProfileHelper.executeForInteractivePreferences(profile, appContext, sharedPreferences);
                            }

                        } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    };
                    PPApplicationStatic.createProfileIteractivePreferencesExecutorPool();
                    PPApplication.profileIteractivePreferencesExecutorPool.submit(runnable3);
                }
                break;
            case PPApplication.ACTION_SMS_MMS_RECEIVED:
                final String origin = intent.getStringExtra(EXTRA_ORIGIN); // phone number
                final long time = intent.getLongExtra(EXTRA_TIME, 0);
                final int subscriptionId = intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1);
//                Log.e("PPExtenderBroadcastReceiver.onReceive", "origin="+origin);

//                PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionId="+subscriptionId);

                int _simSlot = 0;

                if (subscriptionId != -1) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
//                        PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList=" + subscriptionList);
                        } catch (SecurityException e) {
                            PPApplicationStatic.recordException(e);
                        }
                        if (subscriptionList != null) {
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList.size()=" + subscriptionList.size());
                            int size = subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/
                            for (int i = 0; i < size; i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionInfo=" + subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    int _subscriptionId = subscriptionInfo.getSubscriptionId();
//                                    PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionId=" + subscriptionId);
                                    if (subscriptionId == _subscriptionId) {
                                        _simSlot = slotIndex + 1;
                                        break;
                                    }
                                }
//                                else
//                                    PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionInfo == null");
                            }
                        }
//                        else
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList == null");
                    }
//                    else
//                        PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager == null");
                }

                final int simSlot = _simSlot;

                if (EventStatic.getGlobalEventsRunning(appContext)) {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");

                        synchronized (PPApplication.handleEventsMutex) {

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_SMS_MMS_RECEIVED);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false) > 0) {
//                                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "ACTION_SMS_MMS_RECEIVED -> SENSOR_TYPE_SMS");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.setEventSMSParameters(origin, time, simSlot);
                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_SMS});
                                //}

//                                Log.e("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "(1) PPApplicationStatic.getContactsCache()");
                                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                                List<Contact> contactList = null;
                                if (contactsCache != null) {
//                                    PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.doHandleEvent", "PPApplication.contactsCacheMutex");
//                                    PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "contactsCache.getList()");
                                    contactList = contactsCache.getList(/*false*/);
                                }

                                boolean smsFromPhoneNumber = false;
                                boolean sendSMSFromEvent = false;
                                String smsTextFromEvent = "";

                                if (contactList != null) {
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (1) *****");

                                    List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();
                                    for (Event event : eventList) {

                                        if (event._eventPreferencesSMS._enabled &&
                                                event._eventPreferencesSMS.isRunnable(appContext)) {

                                            String contactsFromEvent = event._eventPreferencesSMS._contacts;
                                            String contactGroupsFromEvent = event._eventPreferencesSMS._contactGroups;
                                            int contactListTypeFromEvent = event._eventPreferencesSMS._contactListType;
                                            int forSIMCardFromEvent = event._eventPreferencesSMS._forSIMCard;
                                            sendSMSFromEvent = event._eventPreferencesSMS._sendSMS;
                                            smsTextFromEvent = event._eventPreferencesSMS._smsText;

                                            if ((
                                                    /*(contactListTypeFromEvent == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                                                    ((contactsFromEvent != null) && (!contactsFromEvent.isEmpty())) ||
                                                            ((contactGroupsFromEvent != null) && (!contactGroupsFromEvent.isEmpty()))
                                            )
                                                    && (contactListTypeFromEvent == EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST) // only white list is allowed for send sms
                                            ) {
//                                            Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (2) *****");

                                                boolean simSlotOK = true;
                                                if (forSIMCardFromEvent != 0) {
                                                    boolean hasFeature = false;
                                                    boolean hasSIMCard = false;
                                                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                                    if (telephonyManager != null) {
                                                        int phoneCount = telephonyManager.getPhoneCount();
                                                        if (phoneCount > 1) {
                                                            hasFeature = true;
                                                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                                                            hasSIMCard = hasSIMCardData.simCount >= 1;
                                                        }
                                                    }
                                                    if (hasFeature && hasSIMCard)
                                                        simSlotOK = ((simSlot == 1) && (forSIMCardFromEvent == 1)) ||
                                                                ((simSlot == 2) && (forSIMCardFromEvent == 2));
                                                }

//                                            Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (3) *****");

                                                if (simSlotOK)
                                                    smsFromPhoneNumber = isPhoneNumberConfigured(contactsFromEvent, contactGroupsFromEvent, /*contactListType,*/ contactList, origin);
                                            }
                                        }
                                        if (smsFromPhoneNumber)
                                            break;
                                    }

                                    contactList.clear();
                                }

//                            Log.e("PPExtenderProadcastReceover.onReceive", "smsFromPhoneNumber="+smsFromPhoneNumber);

                                if ((smsFromPhoneNumber) && (Build.VERSION.SDK_INT >= 29)) {
//                                    Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (6) *****");
                                    if (Permissions.checkSendSMS(appContext)) {
                                        // send sms
                                        if (sendSMSFromEvent && ((origin != null) && (!origin.isEmpty())) &&
                                                (smsTextFromEvent != null) && (!smsTextFromEvent.isEmpty())) {
                                            try {
//                                            Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (4) *****");

                                                SmsManager smsManager = SmsManager.getDefault();
                                                smsManager.sendTextMessage(origin, null, smsTextFromEvent, null, null);
                                            } catch (Exception e) {
                                                PPApplicationStatic.recordException(e);
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                        }
                    };
                    PPApplicationStatic.logE("[EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "(4) xxx");
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable3);
                }
                break;
            case PPApplication.ACTION_CALL_RECEIVED:
                //final int servicePhoneEvent = intent.getIntExtra(EXTRA_SERVICE_PHONE_EVENT, 0);
                final int callEventType = intent.getIntExtra(EXTRA_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                final String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final long eventTime = intent.getLongExtra(EXTRA_EVENT_TIME, 0);
                final int slotIndex = intent.getIntExtra(EXTRA_SIM_SLOT, 0);
//                Log.e("PPExtenderBroadcastReceiver.onReceive", "callEventType="+callEventType);
//                Log.e("PPExtenderBroadcastReceiver.onReceive", "phoneNumber="+phoneNumber);

//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "callEventType="+callEventType);
//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "phoneNumber="+phoneNumber);
//                @SuppressLint("SimpleDateFormat")
//                SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
//                String _time = sdf.format(Calendar.getInstance().getTimeInMillis());
//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "eventTime="+_time);
//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "slotIndex="+slotIndex);

//                Log.e("PPExtenderBroadcastReceiver.onReceive", "callEventType="+callEventType);

                if (EventStatic.getGlobalEventsRunning(appContext)) {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");

                        synchronized (PPApplication.handleEventsMutex) {

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_CALL_RECEIVED);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

//                            Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (0) *****");

                                //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false) > 0) {
//                                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "ACTION_CALL_RECEIVED -> SENSOR_TYPE_PHONE_CALL");
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "call of events handler SENSOR_TYPE_PHONE_CALL - callEventType="+callEventType);
                                EventsHandler eventsHandler = new EventsHandler(appContext);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "callEventType="+callEventType);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "phoneNumber="+phoneNumber);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "eventTime="+eventTime);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "slotIndex="+slotIndex);
                                eventsHandler.setEventCallParameters(/*servicePhoneEvent, */callEventType, phoneNumber, eventTime, slotIndex);
                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_PHONE_CALL});
                                //}

                                if ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) ||
                                        (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) ||
                                        (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED)) {
//                                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "PHONE_CALL_EVENT_MISSED_CALL");

//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (1) *****");

                                    //noinspection ExtractMethodRecommender
//                                PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "PPApplicationStatic.getContactsCache()");
//                                    Log.e("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "(2) PPApplicationStatic.getContactsCache()");
                                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                                    List<Contact> contactList = null;
                                    if (contactsCache != null) {
//                                    PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.doHandleEvent", "PPApplication.contactsCacheMutex");
//                                    PPApplicationStatic.logE("[CONTACTS_CACHE] PPExtenderBroadcastReceiver.onReceive", "contactsCache.getList()");
                                        contactList = contactsCache.getList(/*false*/);
                                    }

                                    boolean callingPhoneNumber = false;
                                    boolean sendSMSFromEvent = false;
                                    String smsTextFromEvent = "";

                                    if (contactList != null) {
//                                    Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (2) *****");

                                        List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();
                                        for (Event event : eventList) {
                                            boolean canSendSMS = callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL;

                                            if (event._eventPreferencesCall._enabled &&
                                                    event._eventPreferencesCall.isRunnable(appContext)) {

                                                if ((!canSendSMS) && (Build.VERSION.SDK_INT >= 29) && (event._eventPreferencesCall._endCall)) {
                                                    canSendSMS = true;
                                                }

//                                            Log.e("PPExtenderBroadcastReceiver.onReceive", "canSendSMS="+canSendSMS);

                                                if (canSendSMS) {
//                                                Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (3) *****");

                                                    String contactsFromEvent = event._eventPreferencesCall._contacts;
                                                    String contactGroupsFromEvent = event._eventPreferencesCall._contactGroups;
                                                    int contactListTypeFromEvent = event._eventPreferencesCall._contactListType;
                                                    int callEventFromEvent = event._eventPreferencesCall._callEvent;
                                                    int forSIMCardFromEvent = event._eventPreferencesCall._forSIMCard;
                                                    sendSMSFromEvent = event._eventPreferencesCall._sendSMS;
                                                    smsTextFromEvent = event._eventPreferencesCall._smsText;

                                                    if ((
                                                            /*(contactListTypeFromEvent == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                                                            ((contactsFromEvent != null) && (!contactsFromEvent.isEmpty())) ||
                                                                    ((contactGroupsFromEvent != null) && (!contactGroupsFromEvent.isEmpty()))
                                                    )
                                                            &&
                                                            (
                                                                    (callEventFromEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                                                                            (callEventFromEvent == EventPreferencesCall.CALL_EVENT_RINGING) ||
                                                                            (callEventFromEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED) ||
                                                                            (callEventFromEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED)
                                                            )
                                                            && (contactListTypeFromEvent == EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST) // only white list is allowed for send sms
                                                    ) {
//                                                    Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (4) *****");

                                                        boolean simSlotOK = true;
                                                        if (forSIMCardFromEvent != 0) {
                                                            boolean hasFeature = false;
                                                            boolean hasSIMCard = false;
                                                            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                                            if (telephonyManager != null) {
                                                                int phoneCount = telephonyManager.getPhoneCount();
                                                                if (phoneCount > 1) {
                                                                    hasFeature = true;
                                                                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                                                                    hasSIMCard = hasSIMCardData.simCount >= 1;
                                                                }
                                                            }
                                                            if (hasFeature && hasSIMCard)
                                                                simSlotOK = ((slotIndex == 1) && (forSIMCardFromEvent == 1)) ||
                                                                        ((slotIndex == 2) && (forSIMCardFromEvent == 2));
                                                        }

//                                                    Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (5) *****");

                                                        if (simSlotOK)
                                                            callingPhoneNumber = isPhoneNumberConfigured(contactsFromEvent, contactGroupsFromEvent, /*contactListType,*/ contactList, phoneNumber);
                                                    }
                                                }
                                            }
                                            if (callingPhoneNumber)
                                                break;
                                        }

                                        contactList.clear();
                                    }

//                                Log.e("PPExtenderProadcastReceover.onReceive", "callingPhoneNumber="+callingPhoneNumber);

                                    if (callingPhoneNumber) {
//                                    Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (6) *****");
                                        if (Permissions.checkSendSMS(appContext)) {
                                            // send sms
                                            if (sendSMSFromEvent && ((phoneNumber != null) && (!phoneNumber.isEmpty())) &&
                                                    (smsTextFromEvent != null) && (!smsTextFromEvent.isEmpty())) {
                                                try {
//                                                Log.e("PPExtenderBroadcastReceiver.onReceive", "***** (7) *****");

                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    smsManager.sendTextMessage(phoneNumber, null, smsTextFromEvent, null, null);
                                                } catch (Exception e) {
                                                    PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }

                                }

                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                        }
                    };
                    PPApplicationStatic.logE("[EXECUTOR_CALL] PPExtenderBroadcastReceiver.onReceive", "(5) xxx");
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable3);
                }
                break;
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

    private ActivityInfo tryGetActivity(Context context, ComponentName componentName) {
        try {
            return context.getPackageManager().getActivityInfo(componentName, 0);
        } catch (Exception e) {
            return null;
        }
    }


    static boolean isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay, boolean displayNotification
                                                 /*, String calledFrom*/) {
        boolean enabled = false;

        //int accessibilityEnabled = 0;
        final String service = PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME + "/" + PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME + ".PPPEAccessibilityService";

        // Do not use: it returns always 0 :-(
        /*try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }*/

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        //if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        PPApplication.accessibilityServiceForPPPExtenderConnected = 1;
                        return true;
                    }
                }
            }

        // ------ not enabled --------------------

        if (PPApplication.accessibilityServiceForPPPExtenderConnected != 0) {
            // not started delayed check
            PPApplication.accessibilityServiceForPPPExtenderConnected = 2;
        }

        if (againCheckInDelay) {

            if (PPApplication.accessibilityServiceForPPPExtenderConnected == 2) {
                // not started delayed check, start it

                PPApplication.accessibilityServiceForPPPExtenderConnected = 0;

                // send broadcast to Extender to get if Extender is connected
                //Intent _intent = new Intent(PPApplication.ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED);
                //context.sendBroadcast(_intent, PPApplication.PPP_EXTENDER_PERMISSION);

                Data workData = new Data.Builder()
                        .putBoolean(EXTRA_DISPLAY_NOTIFICATION, displayNotification)
                        .build();

                boolean enqueuedWork = false;

//                PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "xxxxxxxxxxxxxxxxxxxx");

                // work for check accessibility, when Extender do not send ACTION_ACCESSIBILITY_SERVICE_CONNECTED
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG)
                                .setInputData(workData)
                                .setInitialDelay(ACCESSIBILITY_SERVICE_CONNECTED_DELAY, TimeUnit.MINUTES)
                                .build();
                try {
                    if (PPApplicationStatic.getApplicationStarted(true, false)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
//                            PPApplicationStatic.logE("[WORKER_CALL] PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            enqueuedWork = true;
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

                if (!enqueuedWork)
                    PPApplication.accessibilityServiceForPPPExtenderConnected = 2;
            }

            enabled = PPApplication.accessibilityServiceForPPPExtenderConnected == 0;

/*
                if (PPApplication.accessibilityServiceForPPPExtenderConnected > 0)
                    enabled = PPApplication.accessibilityServiceForPPPExtenderConnected == 1;
                else
                    enabled = true;
 */
        }

        return enabled;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, PackageManager.MATCH_ALL);
            //boolean installed = appInfo.enabled;
            //   !!! Do not use this, because in Samsung may be disabled, when is set to deep sleep automatically
            //if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return PPApplicationStatic.getVersionCode(pInfo);
            //}
            //else {
            //    return 0;
            //}
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return 0;
        }
    }

    static String getExtenderVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, PackageManager.MATCH_ALL);
            //boolean installed = appInfo.enabled;
            //   !!! Do not use this, because in Samsung may be disabled, when is set to deep sleep automatically
            //if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return pInfo.versionName;
            //}
            //else {
            //    return "";
            //}
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.getExtenderVersionName", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return "";
        }
    }

    /** @noinspection SameParameterValue*/
    static boolean isEnabled(Context context, int version, boolean displayNotification, boolean againCheckInDelay
                             /*, String calledFrom*/) {

        int extenderVersion = isExtenderInstalled(context);
        boolean enabled = false;
        //if ((version == -1) || (extenderVersion >= version)) // -1 => do not check version
        if (extenderVersion >= version)
            enabled = isAccessibilityServiceEnabled(context, againCheckInDelay, displayNotification
                    /*, "PPExtenderBroadcastReceiver.isEnabled"*/);
        //return  (extenderVersion >= version) && enabled;
        return  (extenderVersion >= version) && enabled;
    }

    static void getApplicationInForeground(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.getApplicationInForeground", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            ApplicationPreferences.prefApplicationInForeground = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_APPLICATION_IN_FOREGROUND, "");
            //return prefApplicationInForeground;
        }
    }
    static void setApplicationInForeground(Context context, String application)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.setApplicationInForeground", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_APPLICATION_IN_FOREGROUND, application);
            editor.apply();
            ApplicationPreferences.prefApplicationInForeground = application;
        }
    }

}
