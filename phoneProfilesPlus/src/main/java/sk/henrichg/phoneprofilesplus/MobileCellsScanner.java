package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MobileCellsScanner {

    private final Context context;
    private TelephonyManager telephonyManagerDefault;
    private TelephonyManager telephonyManagerSIM1 = null;
    private TelephonyManager telephonyManagerSIM2 = null;

    MobileCellsListener mobileCellsListenerDefault = null;
    MobileCellsListener mobileCellsListenerSIM1 = null;
    MobileCellsListener mobileCellsListenerSIM2 = null;

    static String lastRunningEventsNotOutside = "";
    static String lastPausedEventsOutside = "";

    //static boolean forceStart = false;

    static boolean enabledAutoRegistration = false;
    static int durationForAutoRegistration = 0;
    static String cellsNameForAutoRegistration = "";
    @SuppressWarnings("Convert2Diamond")
    static final List<Long> autoRegistrationEventList = Collections.synchronizedList(new ArrayList<Long>());

    static final String NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED";
    static final String NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION = PPApplication.PACKAGE_NAME + ".MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION";

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE = "show_enable_location_notification_phone_state";

    //static MobileCellsRegistrationService autoRegistrationService = null;

    //static String ACTION_PHONE_STATE_CHANGED = PPApplication.PACKAGE_NAME + ".ACTION_PHONE_STATE_CHANGED";

    MobileCellsScanner(Context context) {
        //PPApplication.logE("MobileCellsScanner.constructor", "xxx");
        this.context = context;

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int simCount = telephonyManager.getPhoneCount();
            if ((Build.VERSION.SDK_INT >= 26) && (simCount > 1)) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(appContext);
                if (mSubscriptionManager != null) {
//                    PPApplication.logE("MobileCellsScanner.constructor", "mSubscriptionManager != null");
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        // Loop through the subscription list i.e. SIM list.
                        subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                        PPApplication.logE("MobileCellsScanner.constructor", "subscriptionList=" + subscriptionList);
                    } catch (SecurityException e) {
                        //PPApplication.recordException(e);
                    }
                    if (subscriptionList != null) {
//                        PPApplication.logE("MobileCellsScanner.constructor", "subscriptionList.size()=" + subscriptionList.size());
                        for (int i = 0; i < subscriptionList.size(); i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                            PPApplication.logE("MobileCellsScanner.constructor", "subscriptionInfo=" + subscriptionInfo);
                            if (subscriptionInfo != null) {
                                int subscriptionId = subscriptionInfo.getSubscriptionId();
                                if (subscriptionInfo.getSimSlotIndex() == 0) {
                                    if (telephonyManagerSIM1 == null) {
//                                        PPApplication.logE("MobileCellsScanner.constructor", "subscriptionId=" + subscriptionId);
                                        //noinspection ConstantConditions
                                        telephonyManagerSIM1 = telephonyManager.createForSubscriptionId(subscriptionId);
                                        mobileCellsListenerSIM1 = new MobileCellsListener(subscriptionInfo, context, this, telephonyManagerSIM1);
                                    }
                                }
                                if (subscriptionInfo.getSimSlotIndex() == 1) {
                                    if (telephonyManagerSIM2 == null) {
//                                        PPApplication.logE("MobileCellsScanner.constructor", "subscriptionId=" + subscriptionId);
                                        //noinspection ConstantConditions
                                        telephonyManagerSIM2 = telephonyManager.createForSubscriptionId(subscriptionId);
                                        mobileCellsListenerSIM2 = new MobileCellsListener(subscriptionInfo, context, this, telephonyManagerSIM2);
                                    }
                                }
                            }
//                            else
//                                PPApplication.logE("MobileCellsScanner.constructor", "subscriptionInfo == null");
                        }
                    }
//                    else
//                        PPApplication.logE("MobileCellsScanner.constructor", "subscriptionList == null");
                }
//                else
//                    PPApplication.logE("MobileCellsScanner.constructor", "mSubscriptionManager == null");
            }
            else {
                telephonyManagerDefault = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                mobileCellsListenerDefault = new MobileCellsListener(null, context, this, telephonyManagerDefault);
                PPApplication.logE("MobileCellsScanner.constructor", "default telephone manager");
            }
        }

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    @SuppressLint("InlinedApi")
    void connect() {
//        PPApplication.logE("MobileCellsScanner.connect", "xxx");
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("MobileCellsScanner.connect", "telephonyManager=" + telephonyManager);
            PPApplication.logE("MobileCellsScanner.connect", "FEATURE_TELEPHONY=" + PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY));
            PPApplication.logE("MobileCellsScanner.connect", "checkLocation=" + Permissions.checkLocation(context.getApplicationContext()));
        }*/

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if ((telephonyManager != null) &&
                PPApplication.HAS_FEATURE_TELEPHONY &&
                Permissions.checkLocation(context.getApplicationContext())) {
            int simCount = telephonyManager.getPhoneCount();
            if ((Build.VERSION.SDK_INT >= 26) && (simCount > 1)) {
                if ((telephonyManagerSIM1 != null) && (mobileCellsListenerSIM1 != null)) {
//                    PPApplication.logE("MobileCellsScanner.connect", "listen SIM 1");
                    telephonyManagerSIM1.listen(mobileCellsListenerSIM1,
                            PhoneStateListener.LISTEN_CELL_INFO
                                    | PhoneStateListener.LISTEN_CELL_LOCATION // is required for some devices, especially with AP level < 26
                                    | PhoneStateListener.LISTEN_SERVICE_STATE
                    );
                }
                if ((telephonyManagerSIM2 != null) && (mobileCellsListenerSIM2 != null)) {
//                    PPApplication.logE("MobileCellsScanner.connect", "listen SIM 2");
                    telephonyManagerSIM2.listen(mobileCellsListenerSIM2,
                            PhoneStateListener.LISTEN_CELL_INFO
                                    | PhoneStateListener.LISTEN_CELL_LOCATION  // is required for some devices, especially with AP level < 26
                                    | PhoneStateListener.LISTEN_SERVICE_STATE
                    );
                }
            }
            else {
                PPApplication.logE("MobileCellsScanner.connect", "listen default");
                telephonyManagerDefault.listen(mobileCellsListenerDefault,
                        PhoneStateListener.LISTEN_CELL_INFO
                                | PhoneStateListener.LISTEN_CELL_LOCATION  // is required for some devices, especially with AP level < 26
                                | PhoneStateListener.LISTEN_SERVICE_STATE
                        );
            }

        }
        startAutoRegistration(context, true);
    }

    void disconnect() {
        //PPApplication.logE("MobileCellsScanner.disconnect", "xxx");

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
        //PPApplication.logE("MobileCellsScanner.getRegisteredCell", "xxx");
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.registerCell();
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.registerCell();
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.registerCell();
    }

    void rescanMobileCells() {
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.rescanMobileCells();
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.rescanMobileCells();
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.rescanMobileCells();
    }

    void handleEvents(/*final Context appContext*/) {
        if (mobileCellsListenerDefault != null)
            mobileCellsListenerDefault.handleEvents();
        if (mobileCellsListenerSIM1 != null)
            mobileCellsListenerSIM1.handleEvents();
        if (mobileCellsListenerSIM2 != null)
            mobileCellsListenerSIM2.handleEvents();
    }

    int getRegisteredCell(int forSimCard) {
        if ((forSimCard == 0) && (mobileCellsListenerDefault != null))
            return mobileCellsListenerDefault.registeredCell;
        if ((forSimCard == 1) && (mobileCellsListenerSIM1 != null))
            return mobileCellsListenerSIM1.registeredCell;
        if ((forSimCard == 2) && (mobileCellsListenerSIM2 != null))
            return mobileCellsListenerSIM2.registeredCell;
        return 0;
    }

    long getLastConnectedTime(int forSimCard) {
        if ((forSimCard == 0) && (mobileCellsListenerDefault != null))
            return mobileCellsListenerDefault.lastConnectedTime;
        if ((forSimCard == 1) && (mobileCellsListenerSIM1 != null))
            return mobileCellsListenerSIM1.lastConnectedTime;
        if ((forSimCard == 2) && (mobileCellsListenerSIM2 != null))
            return mobileCellsListenerSIM2.lastConnectedTime;
        return 0;
    }

    boolean isNotUsedCellsNotificationEnabled() {
        /*if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
            return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        else {*/
            return ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
        //}
    }

    static boolean isValidCellId(int cid) {
        return (cid != -1) && (cid != 0) /*&& (cid != 1)*/ && (cid != Integer.MAX_VALUE);
    }

    static void startAutoRegistration(Context context, boolean forConnect) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (!forConnect) {
            enabledAutoRegistration = true;
            // save to shared preferences
//            PPApplication.logE("[REG] MobileCellsScanner.startAutoRegistration", "setMobileCellsAutoRegistration(true)");
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
        }
        else
            // read from shared preferences
            MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("MobileCellsScanner.startAutoRegistration", "enabledAutoRegistration=" + enabledAutoRegistration);
            PPApplication.logE("MobileCellsScanner.startAutoRegistration", "cellsNameForAutoRegistration=" + cellsNameForAutoRegistration);
        }*/

        if (enabledAutoRegistration) {
            try {
                // start registration service
                Intent serviceIntent = new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class);
//                PPApplication.logE("[START_PP_SERVICE] MobileCellsScanner.startAutoRegistration", "MobileCellsRegistrationService");
                PPApplication.startPPService(context, serviceIntent);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void stopAutoRegistration(Context context, boolean clearRegistration) {
        //PPApplication.logE("MobileCellsScanner.stopAutoRegistration", "xxx");

        // stop registration service
        context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
        //MobileCellsRegistrationService.stop(context);

        if (clearRegistration) {
            //clearEventList();
            // set enabledAutoRegistration=false
            //PPApplication.logE("[REG] MobileCellsScanner.stopAutoRegistration", "setMobileCellsAutoRegistration(true)");
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, true);
        }
    }

    static boolean isEventAdded(long event_id) {
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.contains(event_id);
        }
    }

    static void addEvent(long event_id) {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.add(event_id);
        }
    }

    static void removeEvent(long event_id) {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.remove(event_id);
        }
    }

    static void clearEventList() {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.clear();
        }
    }

    static int getEventCount() {
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.size();
        }
    }

    static void getAllEvents(SharedPreferences sharedPreferences,
                             @SuppressWarnings("SameParameterValue") String key) {
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
                              @SuppressWarnings("SameParameterValue") String key) {
        synchronized (autoRegistrationEventList) {
            Gson gson = new Gson();
            String json = gson.toJson(autoRegistrationEventList);
            editor.putString(key, json);
        }
    }

    static String addCellId(String cells, int cellId) {
        String[] splits = cells.split("\\|");
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
