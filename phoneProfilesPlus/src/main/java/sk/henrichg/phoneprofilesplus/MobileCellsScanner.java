package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.util.List;

class MobileCellsScanner {

    private final Context context;
    private TelephonyManager telephonyManagerDefault;
    private TelephonyManager telephonyManagerSIM1 = null;
    private TelephonyManager telephonyManagerSIM2 = null;

    MobileCellsListener mobileCellsListenerDefault = null;
    MobileCellsListener mobileCellsListenerSIM1 = null;
    MobileCellsListener mobileCellsListenerSIM2 = null;

    // must be this, because of Collections.synchronizedList
    //static final List<Long> autoRegistrationEventList = Collections.synchronizedList(new ArrayList<Long>());

    static final String ACTION_NEW_MOBILE_CELLS_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED";
    static final String ACTION_NEW_MOBILE_CELLS_NOTIFICATION_DISABLE = PPApplication.PACKAGE_NAME + ".MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION";

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE = "show_enable_location_notification_phone_state";

    //static MobileCellsRegistrationService autoRegistrationService = null;

    //static String ACTION_PHONE_STATE_CHANGED = PPApplication.PACKAGE_NAME + ".ACTION_PHONE_STATE_CHANGED";

    MobileCellsScanner(Context context) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner - constructor", "******** ### *******");

        this.context = context.getApplicationContext();

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int simCount = telephonyManager.getPhoneCount();
            if ((simCount > 1)) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(appContext);
                if (mSubscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        // Loop through the subscription list i.e. SIM list.
                        subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    } catch (SecurityException e) {
                        //PPApplicationStatic.recordException(e);
                    }
                    if (subscriptionList != null) {
                        int size = subscriptionList.size();
                        for (int i = 0; i < size; i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                            if (subscriptionInfo != null) {
                                int subscriptionId = subscriptionInfo.getSubscriptionId();
                                if (subscriptionInfo.getSimSlotIndex() == 0) {
                                    if (telephonyManagerSIM1 == null) {
                                        telephonyManagerSIM1 = telephonyManager.createForSubscriptionId(subscriptionId);
                                        mobileCellsListenerSIM1 = new MobileCellsListener(/*subscriptionInfo, */context, this, telephonyManagerSIM1);
                                    }
                                }
                                if (subscriptionInfo.getSimSlotIndex() == 1) {
                                    if (telephonyManagerSIM2 == null) {
                                        telephonyManagerSIM2 = telephonyManager.createForSubscriptionId(subscriptionId);
                                        mobileCellsListenerSIM2 = new MobileCellsListener(/*subscriptionInfo, */context, this, telephonyManagerSIM2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                telephonyManagerDefault = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                mobileCellsListenerDefault = new MobileCellsListener(/*null, */context, this, telephonyManagerDefault);
            }
        }

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    void connect() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.connect", "******** ### *******");
        boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
        if (/*PPApplication.*/isPowerSaveMode) {
            if (ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode.equals("2"))
                // start scanning in power save mode is not allowed
                return;
        }
        else {
            if (ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply.equals("2")) {
                if (GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo)) {
                    // not scan in configured time
                    return;
                }
            }
        }

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if ((telephonyManager != null) &&
                PPApplication.HAS_FEATURE_TELEPHONY &&
                Permissions.checkLocation(context.getApplicationContext())) {
            int simCount = telephonyManager.getPhoneCount();
            if ((simCount > 1)) {
                if ((telephonyManagerSIM1 != null) && (mobileCellsListenerSIM1 != null)) {
                    try {
                        //noinspection deprecation
                        telephonyManagerSIM1.listen(mobileCellsListenerSIM1,
                                PhoneStateListener.LISTEN_CELL_INFO
                                        | PhoneStateListener.LISTEN_CELL_LOCATION // is required for some devices, especially with AP level < 26
                                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        );
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if ((telephonyManagerSIM2 != null) && (mobileCellsListenerSIM2 != null)) {
                    try {
                        //noinspection deprecation
                        telephonyManagerSIM2.listen(mobileCellsListenerSIM2,
                                PhoneStateListener.LISTEN_CELL_INFO
                                        | PhoneStateListener.LISTEN_CELL_LOCATION  // is required for some devices, especially with AP level < 26
                                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        );
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
            else {
                try {
                    //noinspection deprecation
                    telephonyManagerDefault.listen(mobileCellsListenerDefault,
                            PhoneStateListener.LISTEN_CELL_INFO
                                    | PhoneStateListener.LISTEN_CELL_LOCATION  // is required for some devices, especially with AP level < 26
                                    | PhoneStateListener.LISTEN_SERVICE_STATE
                            );
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

        }
        startAutoRegistration(context, true);
    }

    void disconnect() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.disconnect", "******** ### *******");
        if (mobileCellsListenerSIM1 != null) {
            try {
                if (telephonyManagerSIM1 != null)
                    telephonyManagerSIM1.listen(mobileCellsListenerSIM1, PhoneStateListener.LISTEN_NONE);
                mobileCellsListenerSIM1 = null;
                telephonyManagerSIM1 = null;
            } catch (Exception ignored) {
            }
        }
        if (mobileCellsListenerSIM2 != null) {
            try {
                if (telephonyManagerSIM2 != null)
                    telephonyManagerSIM2.listen(mobileCellsListenerSIM2, PhoneStateListener.LISTEN_NONE);
                mobileCellsListenerSIM2 = null;
                telephonyManagerSIM2 = null;
            } catch (Exception ignored) {
            }
        }
        if (mobileCellsListenerDefault != null) {
            try {
                if (telephonyManagerDefault != null)
                    telephonyManagerDefault.listen(mobileCellsListenerDefault, PhoneStateListener.LISTEN_NONE);
                mobileCellsListenerDefault = null;
                telephonyManagerDefault = null;
            } catch (Exception ignored) {
            }
        }

        stopAutoRegistration(context, false);
    }

    void registerCell() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.registerCell", "******** ### *******");
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.registerCell();
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.registerCell();
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.registerCell();
    }

    void rescanMobileCells() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.rescanMobileCells", "******** ### *******");
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.rescanMobileCells();
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.rescanMobileCells();
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.rescanMobileCells();
    }

    void handleEvents(final Context appContext) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.handleEvents", "******** ### *******");
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.handleEvents(appContext);
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.handleEvents(appContext);
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.handleEvents(appContext);
    }

    int getRegisteredCell(int forSimCard) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.getRegisteredCell", "******** ### *******");
        if ((forSimCard == 0) && (mobileCellsListenerDefault != null))
            return mobileCellsListenerDefault.registeredCell;
        if ((forSimCard == 1) && (mobileCellsListenerSIM1 != null))
            return mobileCellsListenerSIM1.registeredCell;
        if ((forSimCard == 2) && (mobileCellsListenerSIM2 != null))
            return mobileCellsListenerSIM2.registeredCell;
        return 0;
    }

    long getLastConnectedTime(int forSimCard) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.getLastConnectedTime", "******** ### *******");
        if ((forSimCard == 0) && (mobileCellsListenerDefault != null))
            return mobileCellsListenerDefault.lastConnectedTime;
        if ((forSimCard == 1) && (mobileCellsListenerSIM1 != null))
            return mobileCellsListenerSIM1.lastConnectedTime;
        if ((forSimCard == 2) && (mobileCellsListenerSIM2 != null))
            return mobileCellsListenerSIM2.lastConnectedTime;
        return 0;
    }

    boolean isNotUsedCellsNotificationEnabled() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.isNotUsedCellsNotificationEnabled", "******** ### *******");
        /*
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
            return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        */
            return ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    }

    static boolean isValidCellId(int cid) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.isValidCellId", "******** ### *******");
        return (cid != -1) && (cid != 0) /*&& (cid != 1)*/ && (cid != Integer.MAX_VALUE);
    }

    static void startAutoRegistration(Context context, boolean forConnect) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.startAutoRegistration", "******** ### *******");
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (!forConnect) {
            PPApplication.mobileCellsScannerEnabledAutoRegistration = true;
            // save to shared preferences
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
        }
        else
            // read from shared preferences
            MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        if (PPApplication.mobileCellsScannerEnabledAutoRegistration) {
//            PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.startAutoRegistration", "******** ### *******  start registration service");
            try {
                // start registration service
                Intent serviceIntent = new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class);
                PPApplicationStatic.startPPService(context, serviceIntent, false);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
    }

    static void stopAutoRegistration(Context context, boolean clearRegistration) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.stopAutoRegistration", "******** ### *******");
        // stop registration service
        context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
        //MobileCellsRegistrationService.stop(context);

        if (clearRegistration) {
            //clearEventList();
            // set enabledAutoRegistration=false
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, true);
        }
    }

/*
    static boolean isEventAdded(long event_id) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.isEventAdded", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.contains(event_id);
        }
    }

    static void addEvent(long event_id) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.addEvent", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.add(event_id);
        }
    }

    static void removeEvent(long event_id) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.removeEvent", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.remove(event_id);
        }
    }

    static void clearEventList() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.clearEventList", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.clear();
        }
    }

    static int getEventCount() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.getEventCount", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.size();
        }
    }

    static void getAllEvents(SharedPreferences sharedPreferences,
                             String key) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.getAllEvents", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            Gson gson = new Gson();
            String json =sharedPreferences.getString(key, null);
            Type type = new TypeToken<ArrayList<Long>>() {}.getType();
            autoRegistrationEventList.clear();
            ArrayList<Long> list = gson.fromJson(json, type);
            if (list != null)
                autoRegistrationEventList.addAll(list);
        }
    }

    static void saveAllEvents(SharedPreferences.Editor editor,
                              String key) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.saveAllEvents", "******** ### *******");
        synchronized (autoRegistrationEventList) {
            Gson gson = new Gson();
            String json = gson.toJson(autoRegistrationEventList);
            editor.putString(key, json);
        }
    }
*/
/*
    static String addCellId(String cells, int cellId) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsScanner.addCellId", "******** ### *******");

        String[] splits = cells.split(StringConstants.STR_SPLIT_REGEX);
        String sCellId = Integer.toString(cellId);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(sCellId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!cells.isEmpty())
                cells = cells + "|";
            cells = cells + sCellId;
        }
        return cells;
    }
*/
    /*
    private static boolean getShowEnableLocationNotification(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE, true);
    }

    static void setShowEnableLocationNotification(Context context, boolean show)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(MobileCellsScanner.PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE, show);
        editor.apply();
    }
    */

}
