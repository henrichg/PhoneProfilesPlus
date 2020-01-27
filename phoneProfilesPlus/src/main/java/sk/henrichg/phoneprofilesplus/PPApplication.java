package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import dev.doubledot.doki.views.DokiContentView;
import io.fabric.sdk.android.Fabric;

import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

public class PPApplication extends Application /*implements Application.ActivityLifecycleCallbacks*/ {

    private static PPApplication instance;
    private static boolean applicationStarted = false;
    static boolean globalEventsRunStop = true;

    static long lastRefreshOfGUI = 0;

    static final int DURATION_FOR_GUI_REFRESH = 500;

    static final ApplicationPreferencesMutex applicationPreferencesMutex = new ApplicationPreferencesMutex();
    static final ApplicationGlobalPreferencesMutex applicationGlobalPreferencesMutex = new ApplicationGlobalPreferencesMutex();
    private static final ApplicationStartedMutex applicationStartedMutex = new ApplicationStartedMutex();
    static final ProfileActivationMutex profileActivationMutex = new ProfileActivationMutex();
    static final GlobalEventsRunStopMutex globalEventsRunStopMutex = new GlobalEventsRunStopMutex();
    static final EventsRunMutex eventsRunMutex = new EventsRunMutex();
    static final EventCallSensorMutex eventCallSensorMutex = new EventCallSensorMutex();
    static final EventPeripheralsSensorMutex eventPeripheralsSensorMutex = new EventPeripheralsSensorMutex();
    static final EventWifiBluetoothSensorMutex eventWifiBluetoothSensorMutex = new EventWifiBluetoothSensorMutex();
    static final EventWifiSensorMutex eventWifiSensorMutex = new EventWifiSensorMutex();
    static final EventBluetoothSensorMutex eventBluetoothSensorMutex = new EventBluetoothSensorMutex();

    //static final String romManufacturer = getROMManufacturer();
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();
    static final boolean deviceIsLG = isLG();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean deviceIsOppo = isOppo();
    static final boolean romIsMIUI = isMIUIROM();
    static final boolean romIsEMUI = isEMUIROM();

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";

    //static final int VERSION_CODE_EXTENDER_1_0_4 = 60;
    //static final int VERSION_CODE_EXTENDER_2_0 = 100;
    static final int VERSION_CODE_EXTENDER_3_0 = 200;
    static final int VERSION_CODE_EXTENDER_4_0 = 400;
    static final int VERSION_CODE_EXTENDER_5_1_2 = 465;
    static final int VERSION_CODE_EXTENDER_LATEST = VERSION_CODE_EXTENDER_5_1_2;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    static final boolean logIntoFile = false;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && BuildConfig.DEBUG;
    private static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                         //+"|PPApplication.isXiaomi"
                                         //+"|PPApplication.isHuawei"
                                         //+"|PPApplication.isSamsung"
                                         //+"|PPApplication.isLG"
                                         //+"|PPApplication.getEmuiRomName"
                                         //+"|PPApplication.isEMUIROM"
                                         //+"|PPApplication.isMIUIROM"
                                         +"|PPApplication.exitApp"
                                         +"|PPApplication._exitApp"
                                         //+"|PPApplication.createProfileNotificationChannel"
                                         +"|PhoneProfilesService.onCreate"
                                         +"|PhoneProfilesService.onStartCommand"
                                         +"|PhoneProfilesService.doForFirstStart"
                                         +"|PhoneProfilesService.doCommand"
                                         //+"|PhoneProfilesService.isServiceRunningInForeground"
                                         //+"|PhoneProfilesService.showProfileNotification"
                                         //+"|PhoneProfilesService._showProfileNotification"
                                         //+"|[CUST] PhoneProfilesService._showProfileNotification"
                                         +"|PhoneProfilesService.stopReceiver"
                                         +"|PhoneProfilesService.onDestroy"
                                         +"|DataWrapper.firstStartEvents"
                                         //+"|DataWrapper.setProfileActive"
                                         //+"|DataWrapper.activateProfileOnBoot"
                                         +"|BootUpReceiver"
                                         +"|PackageReplacedReceiver"
                                         +"|PhoneProfilesBackupAgent"
                                         +"|ShutdownBroadcastReceiver"

                                         //+"|BluetoothConnectedDevices"

                                         //+"|[BRS] SettingsContentObserver.onChange"
                                         //+"|BrightnessDialogPreferenceFragmentX"
                                         //+"|[BRSD] SettingsContentObserver"

                                         //+"|EditorProfilesActivity.finishBroadcastReceiver"
                                         //+"|EditorProfilesActivity.onStart"
                                         //+"|EditorProfilesActivity.onStop"

                                         //+"|DataWrapper.restartEventsWithAlert"
                                         //+"|DataWrapper.restartEventsWithDelay"
                                         //+"|[TEST HANDLER] DataWrapper.restartEventsWithDelay"

                                         // for list of TRANSACTION_* for "phone" service
                                         //+"|[LIST] PPApplication.getTransactionCode"

                                         //+"|PhoneProfilesService.onConfigurationChanged"
                                         //+"|IgnoreBatteryOptimizationNotification"

                                         /*+"|DatabaseHandler.onUpgrade"
                                         +"|EditorProfilesActivity.doImportData"
                                         +"|PPApplication.setBlockProfileEventActions"
                                         +"|ImportantInfoHelpFragment.onViewCreated"
                                         +"|ImportantInfoNotification"*/

                                         //+"|TonesHandler"
                                         //+"|TonesHandler.isPhoneProfilesSilent"
                                         //+"|TonesHandler.getToneName"
                                         //+"|DatabaseHandler.fixPhoneProfilesSilentInProfiles"

                                         //+"|[RJS] PPApplication"
                                         //+"|##### ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|@@@ ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|[XXX] ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|[Screen] DataWrapper.doHandleEvents"
                                         //+"|LockDeviceAfterScreenOffBroadcastReceiver"

                                         //+"|PPApplication.startHandlerThread"

                                         //+"|DataWrapper.updateNotificationAndWidgets"
                                         //+"|ActivateProfileHelper.updateGUI"
                                         //+"|OneRowWidgetProvider.onUpdate"

                                         //+"|%%%%%%% DataWrapper.doHandleEvents"
                                         //+"|#### EventsHandler.handleEvents"
                                         //+"|[DEFPROF] EventsHandler"
                                         //+"|$$$ EventsHandler.handleEvents"
                                         //+"|[NOTIFY] EventsHandler"
                                         //+"|Profile.mergeProfiles"
                                         //+"|@@@ Event.pauseEvent"
                                         //+"|@@@ Event.stopEvent"
                                        //+"|$$$ restartEvents"
                                        //+"|DataWrapper._restartEvents"
                                        //+"|DataWrapper.restartEvents"
                                        //+"|PPApplication.startHandlerThread"
                                        //+"|Event.startEvent"
                                        //+"|Event.pauseEvent"
                                        //+"|[DSTART] DataWrapper.doHandleEvents"

                                         //+"|EditorProfilesActivity"
                                         //+"|EditorProfilesActivity.onStart"
                                         //+"|EditorProfilesActivity.onActivityResult"
                                         //+"|EditorProfileListViewHolder"
                                         //+"|EditorEventListViewHolder"
                                         //+"|EditorProfileListFragment"
                                         //+"|EditorEventListFragment"
                                         //+"|EditorProfileListAdapter"
                                         //+"|EditorEventListAdapter"

                                         //+"|PostDelayedBroadcastReceiver"


                                         /*
                                         +"|DataWrapper.restartEventsWithDelay"
                                         +"|DataWrapper.restartEvents"
                                         +"|DataWrapper._restartEvents"
                                         +"|RefreshActivitiesBroadcastReceiver"
                                         +"|$$$$$ EditorProfilesActivity"
                                         */

                                         //+"|ActivateProfileHelper.doExecuteForRadios"

                                         //+"|[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers"

                                         //+"|PPApplication.startPPService"

                                         //+"|GrantPermissionActivity"
                                         //+"|PhoneProfilesPreferencesNestedFragment.doOnActivityResult"

                                         //+"|[****] BatteryBroadcastReceiver.onReceive"
                                         /*
                                         +"|[XXX] PowerSaveModeBroadcastReceiver.onReceive"
                                         +"|[XXX] BatteryBroadcastReceiver.onReceive"
                                         +"|[XXX] ScreenOnOffBroadcastReceiver.onReceive"
                                         */

                                         //+"|DataWrapper.activateProfileFromMainThread"
                                         //+"|ActivateProfileHelper.execute"
                                         //+"|Profile.convertPercentsToBrightnessManualValue"
                                         //+"|Profile.convertPercentsToBrightnessAdaptiveValue"
                                         //+"|SettingsContentObserver"

                                         //+"|$$$ DataWrapper._activateProfile"
                                         //+"|ProfileDurationAlarmBroadcastReceiver.onReceive"
                                         //+"|DataWrapper.activateProfileAfterDuration"
                                         //+"|DataWrapper.getIsManualProfileActivation"

                                         //+"|BillingManager"
                                         //+"|DonationFragment"

                                         //+"|Permissions.grantProfilePermissions"
                                         //+"|Permissions.checkProfileVibrateWhenRinging"
                                         //+"|Permissions.checkVibrateWhenRinging"
                                         //+"|ActivateProfileHelper.executeForVolumes"
                                         //+"|Permissions.checkProfileAccessNotificationPolicy"
                                         //+"|ActivateProfileHelper.setZenMode"
                                         //+"|ActivateProfileHelper.setRingerMode"
                                         //+"|ActivateProfileHelper.setVolumes"
                                         //+"|ActivateProfileHelper.changeRingerModeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.isAudibleSystemRingerMode"
                                         //+"|ActivateProfileHelper.setVibrateWhenRinging"
                                         //+"|PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume"

                                         //+"|PhoneProfilesPrefsActivity"
                                         //+"|PhoneProfilesPrefsActivity.onCreate"
                                         //+"|PhoneProfilesPrefsActivity.onStart"
                                         //+"|PhoneProfilesPrefsActivity.onStop"
                                         //+"|PhoneProfilesPrefsActivity.finish"
                                         //+"|PhoneProfilesPrefsActivity.doPreferenceChanges"
                                         //+"|EditorProfilesActivity.onActivityResult"
                                         //+"|PhoneProfilesPrefsFragment.onCreate"
                                         //+"|PhoneProfilesPrefsFragment.onCreatePreferences"
                                         //+"|PhoneProfilesPrefsFragment.updateSharedPreferences"
                                         //+"|PhoneProfilesPrefsFragment.initPreferenceFragment"
                                         //+"|PhoneProfilesPrefsFragment.loadSharedPreferences"
                                         //+"|PhoneProfilesPrefsFragment.onDestroy"
                                         //+"|PhoneProfilesPrefsFragment.onSharedPreferenceChanged"
                                         //+"|ProfilesPrefsActivity"
                                         //+"|ProfilesPrefsFragment"
                                         //+"|ProfilesPrefsFragment.onCreate"
                                         //+"|ProfilesPrefsFragment.onDisplayPreferenceDialog"
                                         //+"|ProfilesPrefsFragment.onActivityCreated"
                                         //+"|ProfilesPrefsFragment.setRedTextToPreferences"
                                         //+"|ProfilesPrefsActivity.getProfileFromPreferences"
                                         /*+"|EventsPrefsActivity"
                                         +"|EventsPrefsFragment"
                                         +"|PhoneProfilesPrefsNotifications"
                                         +"|LocationGeofencePreferenceX"
                                         +"|ProfilePreferenceX"
                                         +"|RingtonePreferenceX"
                                         +"|VolumeDialogPreferenceX"
                                         +"|VolumeDialogPreferenceFragmentX"
                                         +"|ApplicationsDialogPreferenceX"
                                         +"|ApplicationsDialogPreferenceFragmentX"
                                         +"|LocationGeofencePreferenceX"
                                         +"|LocationGeofencePreferenceFragmentX"
                                         +"|MobileCellsRegistrationDialogPreferenceX"
                                         +"|MobileCellsRegistrationDialogPreferenceFragmentX"
                                         +"|ProfileIconPreferenceX"
                                         +"|ProfileIconPreferenceFragmentX"
                                         +"|TimePreferenceX"
                                         +"|TimePreferenceFragmentX"*/

                                         //+"|Event.notifyEventStart"
                                         //+"|StartEventNotificationBroadcastReceiver"
                                         //+"|StartEventNotificationDeletedReceiver"
                                         //+"|PhoneProfilesService.playNotificationSound"

                                         //+"|PPNotificationListenerService"
                                         //+"|PPNotificationListenerService.onNotificationPosted"
                                         //+"|[NOTIF] EventsHandler.handleEvents"
                                         //+"|[NOTIF] DataWrapper.doHandleEvents"
                                         //+"|EventPreferencesNotification.isContactConfigured"
                                         //+"|EventPreferencesNotification.isNotificationActive"
                                         //+"|EventPreferencesNotification.isNotificationVisible"
                                         //+"|NotificationEventEndBroadcastReceiver"

                                         //+"|[CALL] DataWrapper.doHandleEvents"

                                         //+"|"+CallsCounter.LOG_TAG
                                         //+"|[RJS] PPApplication"
                                         //+"|[RJS] PhoneProfilesService"

                                         //+"|ActivateProfileHelper.setAirplaneMode_SDK17"
                                         //+"|ActivateProfileHelper.executeForRadios"
                                         //+"|ActivateProfileHelper.setMobileData"
                                         //+"|ActivateProfileHelper.doExecuteForRadios"
                                        //+"|ActivateProfileHelper.doExecuteForRadios"
                                        //+"|CmdMobileData.isEnabled"
                                        //+"|$$$ WifiAP"

                                         //+"|DeviceIdleModeBroadcastReceiver"

                                         //+"|##### GeofenceScanner"
                                         //+"|GeofenceScannerJob"
                                         //+"|GeofenceScannerJob.scheduleJob"
                                         //+"|GeofenceScannerJob.onRunJob"
                                         //+"|LocationGeofenceEditorActivity.updateEditedMarker"
                                         //+"|LocationModeChangedBroadcastReceiver"
                                         //+"|PhoneProfilesService.scheduleGeofenceScannerJob"
                                         //+"|PhoneProfilesService.startGeofenceScanner"
                                         //+"|PhoneProfilesService.stopGeofenceScanner"
                                         //+"|[GeoSensor] DataWrapper.doHandleEvents"
                                         //+"|[***] GeofenceScanner"
                                         //+"|GeofenceScanWorker"
                                         //+"|GeofenceScanner.updateTransitionsByLastKnownLocation"
                                         //+"|GeofenceScanWorker.doWork"
                                         //+"|GeofenceScanner.LocationCallback"

                                         //+"|WifiStateChangedBroadcastReceiver"
                                         //+"|ConnectToSSIDDialogPreferenceFragmentX.onBindDialogView"
                                         //+"|WifiScanWorker.fillWifiConfigurationList"
                                         //+"|WifiScanWorker.saveWifiConfigurationList"
                                         //+"|WifiConnectionBroadcastReceiver"
                                         //+"|WifiBluetoothScanner"
                                         //+"|%%%% WifiBluetoothScanner.doScan"
                                         //+"|$$$W WifiBluetoothScanner"
                                         //+"|[WiFi] DataWrapper.doHandleEvents"
                                         //+"|[***] DataWrapper.doHandleEvents"

                                         /*+"|BluetoothScanWorker.doWork"
                                         +"|BluetoothScanWorker.startScanner"
                                         +"|BluetoothScanWorker.startCLScan"
                                         +"|BluetoothScanWorker.stopCLScan"
                                         +"|BluetoothScanWorker.startLEScan"
                                         +"|BluetoothScanWorker.stopLEScan"
                                         +"|BluetoothScanWorker.doWork"
                                         +"|BluetoothScanWorker.finishCLScan"
                                         +"|BluetoothScanWorker.finishLEScan"
                                         +"|BluetoothScanBroadcastReceiver.onReceive"
                                         +"|@@@ BluetoothScanBroadcastReceiver.onReceive"
                                         +"|BluetoothLEScanCallback21"*/
                                        //+"|[BTScan] DataWrapper.doHandleEvents"
                                        //+"|BluetoothConnectedDevices"
                                        //+"|BluetoothConnectionBroadcastReceiver"
                                        //+"|BluetoothStateChangedBroadcastReceiver"
                                        /*+"|BluetoothScanBroadcastReceiver"
                                        +"|BluetoothLEScanCallback21"
                                        +"|BluetoothLEScanBroadcastReceiver"
                                        +"|BluetoothScanWorker"
                                        +"|$$$B WifiBluetoothScanner"
                                        +"|$$$BCL WifiBluetoothScanner"
                                        +"|$$$BLE WifiBluetoothScanner"*/

                                         //+"|PostDelayedBroadcastReceiver.onReceive"

                                         //+"|WifiScanWorker"
                                         //+"|WifiScanWorker.doWork"
                                         //+"|%%%% WifiScanBroadcastReceiver.onReceive"

                                         //+"|WifiSSIDPreference.refreshListView"

                                         //+"|%%%%%%% DataWrapper.doHandleEvents"

                                         //+"|[RJS] PhoneProfilesService.registerForegroundApplicationChangedReceiver"
                                         //+"|PhoneProfilesService.registerReceiverForOrientationSensor"
                                         //+"|PhoneProfilesService.runEventsHandlerForOrientationChange"
                                         //+"|PhoneProfilesService.startListeningOrientationSensors"
                                         //+"|PhoneProfilesService.stopListeningOrientationSensors"
                                         //+"|EventPreferencesOrientation"
                                         //+"|OrientationScanner.onSensorChanged"
                                         //+"|OrientationEventBroadcastReceiver"
                                         //+"|PhoneProfilesService.startOrientationScanner"
                                         //+"|PPPExtenderBroadcastReceiver"
                                         //+"|[OriSensor] DataWrapper.doHandleEvents"


                                         //+"|EventsHandler.doEndHandler"
                                         //+"|PhoneProfilesService.doSimulatingRingingCall"
                                         //+"|PhoneProfilesService.startSimulatingRingingCall"
                                         //+"|PhoneProfilesService.stopSimulatingRingingCall"
                                         //+"|PhoneProfilesService.onAudioFocusChange"

                                         //+"|@@@ EventsHandler.handleEvents"
                                         //+"|EventsHandler.doEndService"

                                         //+"|RunApplicationWithDelayBroadcastReceiver"

                                         //+"|PreferenceFragment"

                                        //+"|PhoneProfilesService.registerAccessibilityServiceReceiver"
                                        //+"|DatabaseHandler.getTypeProfilesCount"
                                        //+"|[RJS] PhoneProfilesService.registerPPPPExtenderReceiver"
                                        //+"|PPPExtenderBroadcastReceiver.onReceive"
                                        //+"|SMSEventEndBroadcastReceiver.onReceive"
                                        //+"|[SMS sensor]"

                                        //+ "|[RJS] PhoneProfilesService.startPhoneStateScanner"
                                        //+ "|PhoneStateScanner"
                                        //+"|MobileCellsPreference"
                                        //+"|MobileCellsPreference.refreshListView"
                                        //+"|PhoneStateScanner.constructor"
                                        //+"|PhoneStateScanner.connect"
                                        //+"|PhoneStateScanner.disconnect"
                                        //+"|PhoneStateScanner.startAutoRegistration"
                                        //+"|PhoneStateScanner.stopAutoRegistration"
                                        //+"|PhoneStateScanner.getAllCellInfo"
                                        //+"|PhoneStateScanner.getCellLocation"
                                        //+"|PhoneStateScanner.doAutoRegistration"
                                        //+"|MobileCellsRegistrationDialogPreference.startRegistration"
                                        //+"|MobileCellsRegistrationService"
                                        //+"|NotUsedMobileCellsNotificationDeletedReceiver.onReceive"

                                        //+"|PermissionsNotificationDeletedReceiver.onReceive"

                                        //+"|[RJS] PhoneProfilesService.registerReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.unregisterReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.reregisterReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.registerReceiverForTimeSensor"

                                        //+"|EventPreferencesActivity.savePreferences"

                                        //+"|PhoneCallReceiver"
                                        //+"|PhoneCallBroadcastReceiver"
                                        //+"|PhoneCallBroadcastReceiver.callAnswered"

                                        //+"|#### EventsHandler.handleEvents"
                                        //+"|[CALL] EventsHandler.handleEvents"
                                        //+"|%%%%%%% DataWrapper.doHandleEvents"
                                        //+"|[CALL] DataWrapper.doHandleEvents"
                                        //+"|DataWrapper.pauseAllEvents"
                                        //+"|EventPreferencesCall"
                                        //+"|MissedCallEventEndBroadcastReceiver"

                                        //+"|StartLauncherFromNotificationReceiver"
                                        //+"|LauncherActivity"
                                        //+"|ActivateProfileActivity"

                                        //+"|AlarmClockBroadcastReceiver"
                                        //+"|AlarmClockEventEndBroadcastReceiver"
                                        //+"|EventPreferencesAlarmClock.removeAlarm"
                                        //+"|EventPreferencesAlarmClock.setAlarm"
                                        //+"|EventPreferencesAlarmClock.computeAlarm"
                                        //+"|NextAlarmClockBroadcastReceiver"
                                        //+"|TimeChangedReceiver"

                                        //+"|@@@ ScreenOnOffBroadcastReceiver"
                                        //+"|LockDeviceActivity"

                                        //+"|DialogHelpPopupWindow.showPopup"

                                        //+"|SMSBroadcastReceiver.onReceive"

                                        //+"|EditorProfilesActivity.changeEventOrder"
                                        //+"|EditorProfilesActivity.selectDrawerItem"

                                        //+"|NFCTagPreference.showEditMenu"

                                        //+"|Profile.generateIconBitmap"

                                        //+"|CalendarProviderChangedBroadcastReceiver"


                                        //+"|EventPreferencesTime.computeAlarm"
                                        /*
                                        +"|EventPreferencesTime.removeSystemEvent"
                                        +"|EventPreferencesTime.setSystemEventForStart"
                                        +"|EventPreferencesTime.setSystemEventForPause"
                                        */
                                        //+"|EventPreferencesTime.removeAlarm"
                                        //+"|EventPreferencesTime.setAlarm"
                                        //+"|[TIME] DataWrapper.doHandleEvents"
                                        /*+"|TwilightScanner"
                                        +"|TwilightScanner.updateTwilightState"
                                        +"|TwilightScanner.doWork"
                                        */
                                        //+"|EventTimeBroadcastReceiver"

                                        //+"|EventPreferencesCalendar"
                                        //+"|EventCalendarBroadcastReceiver"

                                        //+"|DatabaseHandler.importDB"
                                        //+ "|ApplicationsMultiSelectDialogPreference.getValueAMSDP"
                                        //+ "|ApplicationsDialogPreference"
                                        //+ "|ApplicationEditorDialogAdapter"
                                        //+ "|ApplicationEditorDialogViewHolder"
                                        //+ "|ApplicationEditorDialog"
                                        //+ "|ApplicationEditorIntentActivity"
                                        //+ "|ApplicationsCache.cacheApplicationsList"
                                        //+ "|@ Application."

                                        //+ "|BitmapManipulator.resampleBitmapUri"

                                        //+"|CmdGoToSleep"
                                        //+"|CmdNfc"
                                        //+"|ActivateProfileHelper.wifiServiceExists"

                                        //+"|ActivateProfileHelper.lockDevice"

                                        //+"|#### setWifiEnabled"

                                        //+"|PPNumberPicker"
                                        //+"|RingtonePreference.setRingtone"
                                        //+"|RingtonePreferenceX"
                                        //+"|PhoneProfilesService.playNotificationSound"

                                        //+"|[RJS] PhoneProfilesService.scheduleWifiWorker"
                                        //+"|[RJS] PhoneProfilesService.cancelWifiWorker"

                                        //+"|EditorProfilesActivity.selectFilterItem"
                                        //+"|EventsPrefsFragment.onResume"
                                        //+"|ActivateProfileHelper.setScreenCarMode"

                                        //+"|FastAccessDurationDialog.updateProfileView"

                                        //+"|NotUsedMobileCellsNotificationDisableReceiver"
                                        //+"|NotUsedMobileCellsNotificationDeletedReceiver"

                                        //+"|ActivateProfileHelper.executeForForceStopApplications"

                                        //+"|DaysOfWeekPreferenceX"
                                        //+"|EventPreferencesTime.getDayOfWeekByLocale"

                                        //+"|SearchCalendarEventsWorker"

                                        //+"|Profile.getBrightnessPercentage_A9"
                                        //+"|Profile.getBrightnessValue_A9"
                                        //+"|Profile.convertPercentsToBrightnessManualValue"
                                        //+"|Profile.convertPercentsToBrightnessAdaptiveValue"
                                        //+"|Profile.convertBrightnessToPercents"

                                        //+"|EditorProfileListFragment.refreshGUI"
                                        //+"|EditorEventListFragment.refreshGUI"

                                        //+"|----- ActivateProfileHelper.execute"
                                        //+"|BluetoothNamePreferenceFragmentX"

                                        //+"|[VOL] SettingsContentObserver"
                                        //+"|[BAT] DataWrapper.doHandleEvents"

                                        //+"|ShortcutCreatorListFragment"
                                        //+"|BitmapManipulator"

                                        //+"|FetchAddressWorker"
                                        //+"|LocationGeofenceEditorActivity.getWorkInfoByIdLiveData"
                                        //+"|BluetoothNamePreferenceFragmentX.refreshListView"
                                        //+"|WifiSSIDPreferenceFragmentX.refreshListView"
                                        //+"|BluetoothNamePreferenceFragmentX.onDialogClosed"

                                        //+"|[OPT] EditorProfileListFragment"
                                        //+"|[OPT] EditorEventListFragment"

                                        //+"|ActivateProfileHelper.executeForInteractivePreferences"

                                        //+"|PhoneProfilesService.isLocationEnabled"

                                        //+"|PhoneProfilesPrefsFragment.updateSharedPreferences"
                                        //+"|CustomColorDialogPreferenceX"
                                        //+"|CustomColorDialogPreferenceFragmentX"

                                        //+"|[HANDLER] DisableInternalChangeWorker.doWork"

                                        //+"|[HANDLER] Event.setDelayStartAlarm"
                                        //+"|[HANDLER] Event.setDelayEndAlarm"
                                        //+"|DonationBroadcastReceiver"
                                        //+"|[ALARM] EventsHandler.handleEvents"
                                        //+"|EventPreferencesSMS"
                                        //+"|SMSEventEndBroadcastReceiver"
                                        //+"|ElapsedAlarmsWorker"
                                        //+"|[WIFI] ActivateProfileHelper.doExecuteForRadios"
                                        //+"|CmdWifi.setWifi"

                                        //+"|ApplicationEditorIntentActivityX"

                                        //+"|WifiApManager.startTethering"
                                        //+"|WifiApManager.stopTethering"
                                        //+"|WifiApManager.callStartTethering"
                                        //+"|CmdWifiAP"

                                        //+"|ActivateProfileHelper.updateGUI"
                                        //+"|UpdateGUIBroadcastReceiver.onReceive"
                                        //+"|ActivateProfileActivity.refreshGUI"
                                        //+"|EditorProfilesActivity.refreshGUI"
            ;


    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_EVENT_STATUS = "event_status";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_BOOT = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_SERVICE = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    static final int STARTUP_SOURCE_LAUNCHER_START = 10;
    static final int STARTUP_SOURCE_LAUNCHER = 11;
    static final int STARTUP_SOURCE_SERVICE_MANUAL = 12;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 13;

    //static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    //static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_activated_profile";
    static final String MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_mobile_cells_registration";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_grant_permission";
    static final String NOTIFY_EVENT_START_NOTIFICATION_CHANNEL = "phoneProfilesPlus_repeat_notify_event_start";
    static final String NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL = "phoneProfilesPlus_new_mobile_cell";
    static final String DONATION_CHANNEL = "phoneProfilesPlus_donation";

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    //static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 700425;
    //static final int LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID = 700426;
    //static final int LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID = 700427;
    static final int GEOFENCE_SCANNER_ERROR_NOTIFICATION_ID = 700428;
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 700429;
    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 700430;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 700431;
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 700432;
    //static final int EVENT_START_NOTIFICATION_ID = 700433;
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 700434;
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 700435;
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 700436;
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 700437;
    static final int MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID = 700438;
    //static final int GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID = 700439;
    //static final int LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID = 700440;
    static final int IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID = 700441;

    // shared preferences names !!! Configure also in res/xml/phoneprofiles_backup_scheme.xml !!!
    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    //static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    static final String ACTIVATED_PROFILE_PREFS_NAME = "profile_preferences_activated_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    //static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";

    //public static final String RESCAN_TYPE_SCREEN_ON = "1";
    //public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";
    private static final String PREF_DONATION_DONATED = "donation_donated";
    private static final String PREF_NOTIFICATION_PROFILE_NAME = "notification_profile_name";
    private static final String PREF_WIDGET_PROFILE_NAME = "widget_profile_name";
    private static final String PREF_ACTIVITY_PROFILE_NAME = "activity_profile_name";
    private static final String PREF_LAST_ACTIVATED_PROFILE = "last_activated_profile";

    // scanner start/stop types
    static final int SCANNER_START_GEOFENCE_SCANNER = 1;
    static final int SCANNER_STOP_GEOFENCE_SCANNER = 2;
    static final int SCANNER_RESTART_GEOFENCE_SCANNER = 3;

    static final int SCANNER_START_ORIENTATION_SCANNER = 4;
    static final int SCANNER_STOP_ORIENTATION_SCANNER = 5;
    static final int SCANNER_RESTART_ORIENTATION_SCANNER = 6;

    static final int SCANNER_START_PHONE_STATE_SCANNER = 7;
    static final int SCANNER_STOP_PHONE_STATE_SCANNER = 8;
    static final int SCANNER_FORCE_START_PHONE_STATE_SCANNER = 9;
    static final int SCANNER_RESTART_PHONE_STATE_SCANNER = 10;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 11;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 12;
    static final int SCANNER_RESTART_WIFI_SCANNER = 13;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 14;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 15;
    static final int SCANNER_RESTART_BLUETOOTH_SCANNER = 16;

    static final int SCANNER_START_TWILIGHT_SCANNER = 17;
    static final int SCANNER_STOP_TWILIGHT_SCANNER = 18;
    static final int SCANNER_RESTART_TWILIGHT_SCANNER = 19;

    static final int SCANNER_RESTART_ALL_SCANNERS = 50;

    static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    static final String ACTION_FOREGROUND_APPLICATION_CHANGED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FOREGROUND_APPLICATION_CHANGED";
    static final String ACTION_REGISTER_PPPE_FUNCTION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_REGISTER_PPPE_FUNCTION";
    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_SMS_MMS_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_SMS_MMS_RECEIVED";
    static final String ACTION_CALL_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_CALL_RECEIVED";
    static final String ACTION_LOCK_DEVICE = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_LOCK_DEVICE";
    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACCESSIBILITY_SERVICE_PERMISSION";

    static final String ACTION_UPDATE_GUI = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_UPDATE_GUI";
    static final String ACTION_DONATION = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_DONATION";
    static final String ACTION_FINISH_ACTIVITY = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_FINISH_ACTIVITY";
    static final String EXTRA_WHAT_FINISH = "what_finish";

    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER = 2;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER = -2;
    static final int REGISTRATION_TYPE_SMS_REGISTER = 3;
    static final int REGISTRATION_TYPE_SMS_UNREGISTER = -3;
    static final int REGISTRATION_TYPE_CALL_REGISTER = 4;
    static final int REGISTRATION_TYPE_CALL_UNREGISTER = -4;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    static final String EXTRA_APPLICATIONS = "extra_applications";

    static final String CRASHLYTICS_LOG_DEVICE_ROOTED = "DEVICE_ROOTED";
    static final String CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION = "GOOGLE_PLAY_SERVICES_VERSION";

    private static final String SYS_PROP_MOD_VERSION = "ro.modversion";

    //public static long lastUptimeTime;
    //public static long lastEpochTime;

    static SensorManager sensorManager = null;
    static Sensor accelerometerSensor = null;
    static Sensor magneticFieldSensor = null;
    static Sensor lightSensor = null;
    static Sensor proximitySensor = null;

    public static boolean isScreenOn;

//    static private FirebaseAnalytics firebaseAnalytics;

    public static HandlerThread handlerThread = null;
    //public static HandlerThread handlerThreadInternalChangeToFalse = null;
    public static HandlerThread handlerThreadWidget = null;
    public static HandlerThread handlerThreadProfileNotification = null;
    public static HandlerThread handlerThreadPlayTone = null;
    public static HandlerThread handlerThreadPPScanners = null;
    public static HandlerThread handlerThreadPPCommand = null;

    //private static HandlerThread handlerThreadRoot = null;
    public static HandlerThread handlerThreadVolumes = null;
    public static HandlerThread handlerThreadRadios = null;
    public static HandlerThread handlerThreadAdaptiveBrightness = null;
    public static HandlerThread handlerThreadWallpaper = null;
    public static HandlerThread handlerThreadPowerSaveMode = null;
    public static HandlerThread handlerThreadLockDevice = null;
    public static HandlerThread handlerThreadRunApplication = null;
    public static HandlerThread handlerThreadHeadsUpNotifications = null;
    //public static HandlerThread handlerThreadMobileCells = null;
    public static HandlerThread handlerThreadBluetoothConnectedDevices = null;
    //public static HandlerThread handlerThreadBluetoothLECallback = null;
    public static HandlerThread handlerThreadNotificationLed = null;
    public static HandlerThread handlerThreadAlwaysOnDisplay = null;
    public static OrientationScannerHandlerThread handlerThreadOrientationScanner = null;

    //private static HandlerThread handlerThreadRestartEventsWithDelay = null;
    //public static Handler restartEventsWithDelayHandler = null;

    public static Handler toastHandler;
    //public static Handler brightnessHandler;
    public static Handler screenTimeoutHandler;

    public static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    public static final RootMutex rootMutex = new RootMutex();
    private static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    //public static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    public static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    //public static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    public static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    public static final GeofenceScannerLastLocationMutex geofenceScannerLastLocationMutex = new GeofenceScannerLastLocationMutex();
    public static final GeofenceScannerMutex geofenceScannerMutex = new GeofenceScannerMutex();
    public static final WifiBluetoothScannerMutex wifiBluetoothscannerMutex = new WifiBluetoothScannerMutex();
    public static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    public static final PhoneStateScannerMutex phoneStateScannerMutex = new PhoneStateScannerMutex();
    public static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    public static final BluetoothScanMutex bluetoothScanMutex = new BluetoothScanMutex();
    public static final BluetoothLEScanMutex bluetoothLEScanMutex = new BluetoothLEScanMutex();
    public static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    public static final TwilightScannerMutex twilightScannerMutex = new TwilightScannerMutex();
    public static final PPNotificationListenerService ppNotificationListenerService = new PPNotificationListenerService();

    //public static boolean isPowerSaveMode = false;

    // !! this must be here
    public static boolean blockProfileEventActions = false;

    // Samsung Look instance
    public static Slook sLook = null;
    public static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    //public static final Random requestCodeForAlarm = new Random();


    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;

        //registerActivityLifecycleCallbacks(PPApplication.this);

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = getAccelerometerSensor(getApplicationContext());
        magneticFieldSensor = getMagneticFieldSensor(getApplicationContext());
        proximitySensor = getProximitySensor(getApplicationContext());
        lightSensor = getLightSensor(getApplicationContext());

        loadApplicationPreferences(getApplicationContext());
        loadGlobalApplicationData(getApplicationContext());
        loadProfileActivationData(getApplicationContext());

        if (logEnabled()) {
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsXiaomi=" + deviceIsXiaomi);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsHuawei=" + deviceIsHuawei);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsSamsung=" + deviceIsSamsung);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsLG=" + deviceIsLG);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOnePlus=" + deviceIsOnePlus);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOppo=" + deviceIsOppo);

            PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI=" + romIsMIUI);
            PPApplication.logE("##### PPApplication.onCreate", "romIsEMUI=" + romIsEMUI);
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsEMUI=" + isEMUIROM());
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsMIUI=" + isMIUIROM());

            PPApplication.logE("##### PPApplication.onCreate", "manufacturer=" + Build.MANUFACTURER);
            PPApplication.logE("##### PPApplication.onCreate", "model=" + Build.MODEL);
            PPApplication.logE("##### PPApplication.onCreate", "display=" + Build.DISPLAY);
            PPApplication.logE("##### PPApplication.onCreate", "brand=" + Build.BRAND);
            PPApplication.logE("##### PPApplication.onCreate", "fingerprint=" + Build.FINGERPRINT);
            PPApplication.logE("##### PPApplication.onCreate", "type=" + Build.TYPE);

            PPApplication.logE("##### PPApplication.onCreate", "modVersion=" + getReadableModVersion());
            PPApplication.logE("##### PPApplication.onCreate", "osVersion=" + System.getProperty("os.version"));
        }

        if (checkAppReplacingState())
            return;

        ///////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // https://github.com/tiann/FreeReflection
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                Object vmRuntime = getRuntime.invoke(null);

                setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
            } catch (Exception e) {
                Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
            }
        }
        //////////////////////////////////////////

        // Fix for FC: java.lang.IllegalArgumentException: register too many Broadcast Receivers
        //LoadedApkHuaWei.hookHuaWeiVerifier(this);

        /*
        if (logIntoFile || crashIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());
        */

        try {
            //if (!BuildConfig.DEBUG) {
                // Obtain the FirebaseAnalytics instance.
                //firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            //}

            /*
            // Set up Crashlytics, disabled for debug builds
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build();

            Fabric.with(this, crashlyticsKit);
            */
            //if (!BuildConfig.DEBUG) {
                Fabric.with(this, new Crashlytics());
            //}
            // Crashlytics.getInstance().core.logException(exception); -- this log will be associated with crash log.
        } catch (Exception e) {
            /*
            java.lang.IllegalStateException:
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:447)
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:432)
              at android.content.ContextWrapper.getSharedPreferences (ContextWrapper.java:174)
              at io.fabric.sdk.android.services.persistence.PreferenceStoreImpl.<init> (PreferenceStoreImpl.java:39)
              at io.fabric.sdk.android.services.common.AdvertisingInfoProvider.<init> (AdvertisingInfoProvider.java:37)
              at io.fabric.sdk.android.services.common.IdManager.<init> (IdManager.java:114)
              at io.fabric.sdk.android.Fabric$Builder.build (Fabric.java:289)
              at io.fabric.sdk.android.Fabric.with (Fabric.java:340)

              This exception occurs, when storage is protected and PPP is started via LOCKED_BOOT_COMPLETED

              Code from android.app.ContextImpl:
                if (getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.O) {
                    if (isCredentialProtectedStorage()
                            && !getSystemService(UserManager.class)
                                    .isUserUnlockingOrUnlocked(UserHandle.myUserId())) {
                        throw new IllegalStateException("SharedPreferences in credential encrypted "
                                + "storage are not available until after user is unlocked");
                    }
                }
            */
            Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
        }

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Crashlytics.getInstance().core.logException(error);
            }
        });
        anrWatchDog.start();
        */

        try {
            Crashlytics.setBool("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        //if (BuildConfig.DEBUG) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

        //lastUptimeTime = SystemClock.elapsedRealtime();
        //lastEpochTime = System.currentTimeMillis();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = false;

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        startHandlerThread("PPApplication.onCreate");
        //startHandlerThreadInternalChangeToFalse();
        startHandlerThreadPPScanners();
        startHandlerThreadPPCommand();
        //startHandlerThreadRoot();
        startHandlerThreadWidget();
        startHandlerThreadProfileNotification();
        startHandlerThreadPlayTone();
        startHandlerThreadVolumes();
        startHandlerThreadRadios();
        startHandlerThreadAdaptiveBrightness();
        startHandlerThreadWallpaper();
        startHandlerThreadPowerSaveMode();
        startHandlerThreadLockDevice();
        startHandlerThreadRunApplication();
        startHandlerThreadHeadsUpNotifications();
        //startHandlerThreadMobileCells();
        //startHandlerThreadRestartEventsWithDelay();
        startHandlerThreadBluetoothConnectedDevices();
        //startHandlerThreadBluetoothLECallback();
        startHandlerThreadNotificationLed();
        startHandlerThreadAlwaysOnDisplay();
        startHandlerThreadOrientationScanner();

        toastHandler = new Handler(getMainLooper());
        //brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

        /*
        JobConfig.setApiEnabled(JobApi.WORK_MANAGER, true);
        //JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        //JobConfig.setApiEnabled(JobApi.GCM, false); // is only important for Android 4.X

        JobManager.create(this).addJobCreator(new PPJobsCreator());
        */

        PPApplication.initRoot();

        /*
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            F field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        */

        //Log.d("PPApplication.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());

        //Log.d("PPApplication.onCreate","xxx");

        // Samsung Look initialization
        sLook = new Slook();
        try {
            sLook.initialize(this);
            // true = The Device supports Edge Single Mode, Edge Single Plus Mode, and Edge Feeds Mode.
            sLookCocktailPanelEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_PANEL);
            // true = The Device supports Edge Immersive Mode feature.
            //sLookCocktailBarEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_BAR);
        } catch (SsdkUnsupportedException e) {
            sLook = null;
        }

        if (PPApplication.getApplicationStarted(false)) {
            try {
                PPApplication.logE("##### PPApplication.onCreate", "start service");
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                startPPService(getApplicationContext(), serviceIntent);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        GlobalGUIRoutines.collator = GlobalGUIRoutines.getCollator();
        MultiDex.install(this);
    }

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            Log.w("PPApplication.onCreate", "app is replacing...kill");
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

    /*
    static boolean isNewVersion(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        PPApplication.logE("PPApplication.isNewVersion", "oldVersionCode="+oldVersionCode);
        int actualVersionCode;
        try {
            if (oldVersionCode == 0) {
                // save version code
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("PPApplication.isNewVersion", "actualVersionCode=" + actualVersionCode);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        */

        File path = instance.getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, LOG_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        if (instance == null)
            return;

        try {
            //Log.e("PPApplication.logIntoFile", "----- path=" + path.getAbsolutePath());

            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
            */

            File path = instance.getApplicationContext().getExternalFilesDir(null);
            File logFile = new File(path, LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            Log.e("PPApplication.logIntoFile", Log.getStackTraceString(e));
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = logFilterTags.split("\\|");
        for (String split : splits) {
            if (tag.contains(split)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    static public boolean logEnabled() {
        return (logIntoLogCat || logIntoFile);
    }

    @SuppressWarnings("unused")
    static public void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    /*
    public static String intentToString(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.toString() + " " + bundleToString(intent.getExtras());
    }
    */

    /*
    private static String bundleToString(Bundle bundle) {
        StringBuilder out = new StringBuilder("Bundle[");

        if (bundle == null) {
            out.append("null");
        } else {
            boolean first = true;
            for (String key : bundle.keySet()) {
                if (!first) {
                    out.append(", ");
                }

                out.append(key).append('=');

                Object value = bundle.get(key);

                if (value instanceof int[]) {
                    out.append(Arrays.toString((int[]) value));
                } else if (value instanceof byte[]) {
                    out.append(Arrays.toString((byte[]) value));
                } else if (value instanceof boolean[]) {
                    out.append(Arrays.toString((boolean[]) value));
                } else if (value instanceof short[]) {
                    out.append(Arrays.toString((short[]) value));
                } else if (value instanceof long[]) {
                    out.append(Arrays.toString((long[]) value));
                } else if (value instanceof float[]) {
                    out.append(Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    out.append(Arrays.toString((double[]) value));
                } else if (value instanceof String[]) {
                    out.append(Arrays.toString((String[]) value));
                } else if (value instanceof CharSequence[]) {
                    out.append(Arrays.toString((CharSequence[]) value));
                } else if (value instanceof Parcelable[]) {
                    out.append(Arrays.toString((Parcelable[]) value));
                } else if (value instanceof Bundle) {
                    out.append(bundleToString((Bundle) value));
                } else {
                    out.append(value);
                }

                first = false;
            }
        }

        out.append("]");
        return out.toString();
    }
    */

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent) {
        //PPApplication.logE("PPApplication.startPPService", "xxx");
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else
            context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
        //PPApplication.logE("PPApplication.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static void loadGlobalApplicationData(Context context) {
        synchronized (applicationStartedMutex) {
            applicationStarted = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_APPLICATION_STARTED, false);
        }
        synchronized (globalEventsRunStopMutex) {
            globalEventsRunStop = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, true);
        }
        IgnoreBatteryOptimizationNotification.getShowIgnoreBatteryOptimizationNotificationOnStart(context);
        getActivityLogEnabled(context);
        getNotificationProfileName(context);
        getWidgetProfileName(context);
        getActivityProfileName(context);
        getLastActivatedProfile(context);
        Event.getEventsBlocked(context);
        Event.getForceRunEventRunning(context);
        PPPExtenderBroadcastReceiver.getApplicationInForeground(context);
        EventPreferencesCall.getEventCallEventType(context);
        EventPreferencesCall.getEventCallEventTime(context);
        EventPreferencesCall.getEventCallPhoneNumber(context);
        HeadsetConnectionBroadcastReceiver.getEventHeadsetParameters(context);
        WifiBluetoothScanner.getForceOneWifiScan(context);
        WifiBluetoothScanner.getForceOneBluetoothScan(context);
        WifiBluetoothScanner.getForceOneLEBluetoothScan(context);
        BluetoothScanWorker.getBluetoothEnabledForScan(context);
        BluetoothScanWorker.getScanRequest(context);
        BluetoothScanWorker.getLEScanRequest(context);
        BluetoothScanWorker.getWaitForResults(context);
        BluetoothScanWorker.getWaitForLEResults(context);
        BluetoothScanWorker.getScanKilled(context);
        WifiScanWorker.getWifiEnabledForScan(context);
        WifiScanWorker.getScanRequest(context);
        WifiScanWorker.getWaitForResults(context);
        ApplicationPreferences.loadStartTargetHelps(context);
    }

    static void loadApplicationPreferences(Context context) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            ApplicationPreferences.editorOrderSelectedItem(context);
            ApplicationPreferences.editorSelectedView(context);
            ApplicationPreferences.editorProfilesViewSelectedItem(context);
            ApplicationPreferences.editorEventsViewSelectedItem(context);
            //ApplicationPreferences.applicationFirstStart(context);
            ApplicationPreferences.applicationStartOnBoot(context);
            ApplicationPreferences.applicationActivate(context);
            ApplicationPreferences.applicationStartEvents(context);
            ApplicationPreferences.applicationActivateWithAlert(context);
            ApplicationPreferences.applicationClose(context);
            ApplicationPreferences.applicationLongClickActivation(context);
            //ApplicationPreferences.applicationLanguage(context);
            ApplicationPreferences.applicationTheme(context);
            //ApplicationPreferences.applicationActivatorPrefIndicator(context);
            ApplicationPreferences.applicationEditorPrefIndicator(context);
            //ApplicationPreferences.applicationActivatorHeader(context);
            //ApplicationPreferences.applicationEditorHeader(context);
            ApplicationPreferences.notificationsToast(context);
            //ApplicationPreferences.notificationStatusBar(context);
            //ApplicationPreferences.notificationStatusBarPermanent(context);
            //ApplicationPreferences.notificationStatusBarCancel(context);
            ApplicationPreferences.notificationStatusBarStyle(context);
            ApplicationPreferences.notificationShowInStatusBar(context);
            ApplicationPreferences.notificationTextColor(context);
            ApplicationPreferences.notificationHideInLockScreen(context);
            //ApplicationPreferences.notificationTheme(context);
            ApplicationPreferences.applicationWidgetListPrefIndicator(context);
            ApplicationPreferences.applicationWidgetListHeader(context);
            ApplicationPreferences.applicationWidgetListBackground(context);
            ApplicationPreferences.applicationWidgetListLightnessB(context);
            ApplicationPreferences.applicationWidgetListLightnessT(context);
            ApplicationPreferences.applicationWidgetIconColor(context);
            ApplicationPreferences.applicationWidgetIconLightness(context);
            ApplicationPreferences.applicationWidgetListIconColor(context);
            ApplicationPreferences.applicationWidgetListIconLightness(context);
            //ApplicationPreferences.applicationEditorAutoCloseDrawer(context);
            //ApplicationPreferences.applicationEditorSaveEditorState(context);
            ApplicationPreferences.notificationPrefIndicator(context);
            ApplicationPreferences.applicationHomeLauncher(context);
            ApplicationPreferences.applicationWidgetLauncher(context);
            ApplicationPreferences.applicationNotificationLauncher(context);
            ApplicationPreferences.applicationEventWifiScanInterval(context);
            ApplicationPreferences.applicationBackgroundProfile(context);
            ApplicationPreferences.applicationBackgroundProfileNotificationSound(context);
            ApplicationPreferences.applicationBackgroundProfileNotificationVibrate(context);
            ApplicationPreferences.applicationBackgroundProfileUsage(context);
            ApplicationPreferences.applicationActivatorGridLayout(context);
            ApplicationPreferences.applicationWidgetListGridLayout(context);
            ApplicationPreferences.applicationEventBluetoothScanInterval(context);
            ApplicationPreferences.applicationEventWifiRescan(context);
            ApplicationPreferences.applicationEventBluetoothRescan(context);
            ApplicationPreferences.applicationWidgetIconHideProfileName(context);
            ApplicationPreferences.applicationShortcutEmblem(context);
            ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context);
            ApplicationPreferences.applicationPowerSaveModeInternal(context);
            ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context);
            ApplicationPreferences.applicationEventLocationUseGPS(context);
            ApplicationPreferences.applicationEventLocationRescan(context);
            ApplicationPreferences.applicationEventOrientationScanInterval(context);
            ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventMobileCellsRescan(context);
            ApplicationPreferences.applicationDeleteOldActivityLogs(context);
            ApplicationPreferences.applicationWidgetIconBackground(context);
            ApplicationPreferences.applicationWidgetIconLightnessB(context);
            ApplicationPreferences.applicationWidgetIconLightnessT(context);
            ApplicationPreferences.applicationEventUsePriority(context);
            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context);
            ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
            //ApplicationPreferences.applicationSamsungEdgePrefIndicator(context);
            ApplicationPreferences.applicationSamsungEdgeHeader(context);
            ApplicationPreferences.applicationSamsungEdgeBackground(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            ApplicationPreferences.applicationSamsungEdgeIconColor(context);
            ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
            //ApplicationPreferences.applicationSamsungEdgeGridLayout(context);
            ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationRestartEventsWithAlert(context);
            ApplicationPreferences.applicationWidgetListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
            ApplicationPreferences.applicationWidgetListBackgroundType(context);
            ApplicationPreferences.applicationWidgetListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetIconBackgroundType(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColor(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundType(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context);
            //ApplicationPreferences.applicationEventWifiEnableWifi(context);
            //ApplicationPreferences.applicationEventBluetoothEnableBluetooth(context);
            ApplicationPreferences.applicationEventWifiScanIfWifiOff(context);
            ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff(context);
            ApplicationPreferences.applicationEventWifiEnableScanning(context);
            ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
            ApplicationPreferences.applicationEventOrientationEnableScanning(context);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNeverAskForEnableRun(context);
            ApplicationPreferences.applicationUseAlarmClock(context);
            ApplicationPreferences.applicationNeverAskForGrantRoot(context);
            ApplicationPreferences.notificationShowButtonExit(context);
            //ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
            ApplicationPreferences.applicationWidgetOneRowBackground(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
            ApplicationPreferences.applicationWidgetOneRowIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
            ApplicationPreferences.applicationWidgetListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
            ApplicationPreferences.applicationWidgetIconLightnessBorder(context);
            ApplicationPreferences.applicationWidgetListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
            ApplicationPreferences.applicationWidgetIconShowBorder(context);
            ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetIconCustomIconLightness(context);
            ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context);
            //ApplicationPreferences.notificationDarkBackground(context);
            ApplicationPreferences.notificationUseDecoration(context);
            ApplicationPreferences.notificationLayoutType(context);
            ApplicationPreferences.notificationBackgroundColor(context);
            //ApplicationPreferences.applicationNightModeOffTheme(context);
            ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context);
            ApplicationPreferences.applicationSamsungEdgeVerticalPosition(context);
            ApplicationPreferences.notificationBackgroundCustomColor(context);
            ApplicationPreferences.notificationNightMode(context);
            ApplicationPreferences.applicationEditorHideHeaderOrBottomBar(context);
            ApplicationPreferences.applicationWidgetIconShowProfileDuration(context);
        }
    }

    static void loadProfileActivationData(Context context) {
        ActivateProfileHelper.getRingerVolume(context);
        ActivateProfileHelper.getNotificationVolume(context);
        ActivateProfileHelper.getRingerMode(context);
        ActivateProfileHelper.getZenMode(context);
        ActivateProfileHelper.getLockScreenDisabled(context);
        ActivateProfileHelper.getActivatedProfileScreenTimeout(context);
        ActivateProfileHelper.getMergedRingNotificationVolumes(context);
        Profile.getActivatedProfileForDuration(context);
        Profile.getActivatedProfileEndDurationTime(context);
    }

    //--------------------------------------------------------------

    static boolean getApplicationStarted(boolean testService)
    {
        synchronized (applicationStartedMutex) {
            if (testService)
                return applicationStarted &&
                        (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
            else
                return applicationStarted;
        }
    }

    static void setApplicationStarted(Context context, boolean appStarted)
    {
        synchronized (applicationStartedMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
            editor.apply();
            applicationStarted = appStarted;
        }
    }

    static public int getSavedVersionCode(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static public void setSavedVersionCode(Context context, int version)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }

    static boolean prefActivityLogEnabled;
    private static void getActivityLogEnabled(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefActivityLogEnabled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_ACTIVITY_LOG_ENABLED, true);
            //return prefActivityLogEnabled;
        }
    }
    static void setActivityLogEnabled(Context context, boolean enabled)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_ACTIVITY_LOG_ENABLED, enabled);
            editor.apply();
            prefActivityLogEnabled = enabled;
        }
    }

    static String prefNotificationProfileName;
    private static void getNotificationProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefNotificationProfileName = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_NAME, "");
            //return prefNotificationProfileName;
        }
    }
    static public void setNotificationProfileName(Context context, String notificationProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_NOTIFICATION_PROFILE_NAME, notificationProfileName);
            editor.apply();
            prefNotificationProfileName = notificationProfileName;
        }
    }

    static String prefWidgetProfileName1;
    static String prefWidgetProfileName2;
    static String prefWidgetProfileName3;
    static String prefWidgetProfileName4;
    static String prefWidgetProfileName5;
    private static void getWidgetProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefWidgetProfileName1 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_1", "");
            prefWidgetProfileName2 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_2", "");
            prefWidgetProfileName3 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_3", "");
            prefWidgetProfileName4 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_4", "");
            prefWidgetProfileName5 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_5", "");
            //return prefNotificationProfileName;
        }
    }
    static void setWidgetProfileName(Context context, int widgetType, String widgetProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, widgetProfileName);
            editor.apply();
            switch (widgetType) {
                case 1:
                    prefWidgetProfileName1 = widgetProfileName;
                    break;
                case 2:
                    prefWidgetProfileName2 = widgetProfileName;
                    break;
                case 3:
                    prefWidgetProfileName3 = widgetProfileName;
                    break;
                case 4:
                    prefWidgetProfileName4 = widgetProfileName;
                    break;
                case 5:
                    prefWidgetProfileName5 = widgetProfileName;
                    break;
            }
        }
    }

    static String prefActivityProfileName1;
    static String prefActivityProfileName2;
    static String prefActivityProfileName3;
    private static void getActivityProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefActivityProfileName1 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_1", "");
            prefActivityProfileName2 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_2", "");
            prefActivityProfileName3 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_3", "");
            //return prefActivityProfileName;
        }
    }
    static void setActivityProfileName(Context context, int activityType, String activityProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, activityProfileName);
            editor.apply();
            switch (activityType) {
                case 1:
                    prefActivityProfileName1 = activityProfileName;
                    break;
                case 2:
                    prefActivityProfileName2 = activityProfileName;
                    break;
                case 3:
                    prefActivityProfileName3 = activityProfileName;
                    break;
            }
        }
    }

    static long prefLastActivatedProfile;
    private static void getLastActivatedProfile(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefLastActivatedProfile = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PREF_LAST_ACTIVATED_PROFILE, 0);
            //return prefLastActivatedProfile;
        }
    }
    static public void setLastActivatedProfile(Context context, long profileId)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_LAST_ACTIVATED_PROFILE, profileId);
            editor.apply();
            prefLastActivatedProfile = profileId;
        }
    }

    static public int getDaysAfterFirstStart(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }
    static public void setDaysAfterFirstStart(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static public int getDonationNotificationCount(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DONATION_NOTIFICATION_COUNT, 0);
    }
    static public void setDonationNotificationCount(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static public int getDaysForNextDonationNotification(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }
    static public void setDaysForNextDonationNotification(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
        editor.apply();
    }

    static public boolean getDonationDonated(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PREF_DONATION_DONATED, false);
    }
    static public void setDonationDonated(Context context)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_DONATION_DONATED, true);
        editor.apply();
    }

    // --------------------------------

    // notification channels -------------------------

    static void createProfileNotificationChannel(/*Profile profile, */Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            int importance;
            //PPApplication.logE("PPApplication.createProfileNotificationChannel","show in status bar="+ApplicationPreferences.notificationShowInStatusBar(context));
            //if (ApplicationPreferences.notificationShowInStatusBar(context)) {
                /*KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    if ((ApplicationPreferences.notificationHideInLockScreen(context) && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        importance = NotificationManager.IMPORTANCE_MIN;
                    else
                        importance = NotificationManager.IMPORTANCE_LOW;
                }
                else*/
            //        importance = NotificationManager.IMPORTANCE_DEFAULT;
            //}
            //else
            //    importance = NotificationManager.IMPORTANCE_MIN;
            importance = NotificationManager.IMPORTANCE_LOW;

            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_activated_profile);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_activated_profile_description_ppp);

            NotificationChannel channel = new NotificationChannel(PROFILE_NOTIFICATION_CHANNEL, name, importance);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);
            channel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createMobileCellsRegistrationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_mobile_cells_registration_description);

            NotificationChannel channel = new NotificationChannel(MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);
            channel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createInformationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_information);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(INFORMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createExclamationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_exclamation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createGrantPermissionNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_grant_permission);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_grant_permission_description);

            NotificationChannel channel = new NotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createNotifyEventStartNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_notify_event_start);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_notify_event_start_description);

            NotificationChannel channel = new NotificationChannel(NOTIFY_EVENT_START_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createMobileCellsNewCellNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_not_used_mobile_cell);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_not_used_mobile_cell_description);

            NotificationChannel channel = new NotificationChannel(NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createDonationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_donation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(DONATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }


    static void createNotificationChannels(Context appContext) {
        PPApplication.createProfileNotificationChannel(appContext);
        PPApplication.createMobileCellsRegistrationNotificationChannel(appContext);
        PPApplication.createInformationNotificationChannel(appContext);
        PPApplication.createExclamationNotificationChannel(appContext);
        PPApplication.createGrantPermissionNotificationChannel(appContext);
        PPApplication.createNotifyEventStartNotificationChannel(appContext);
        PPApplication.createMobileCellsNewCellNotificationChannel(appContext);
        PPApplication.createDonationNotificationChannel(appContext);
    }

    static void showProfileNotification(/*Context context,*/ final boolean refresh, final boolean forService) {
        try {
            //PPApplication.logE("PPApplication.showProfileNotification", "xxx");

            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(refresh, forService/*, false*/);

        } catch (Exception ignored) {}
    }

    // -----------------------------------------------

    // root ------------------------------------------

    static synchronized void initRoot() {
        synchronized (PPApplication.rootMutex) {
            rootMutex.rootChecked = false;
            rootMutex.rooted = false;
            //rootMutex.grantRootChecked = false;
            //rootMutex.rootGranted = false;
            rootMutex.settingsBinaryChecked = false;
            rootMutex.settingsBinaryExists = false;
            //rootMutex.isSELinuxEnforcingChecked = false;
            //rootMutex.isSELinuxEnforcing = false;
            //rootMutex.suVersion = null;
            //rootMutex.suVersionChecked = false;
            rootMutex.serviceBinaryChecked = false;
            rootMutex.serviceBinaryExists = false;
        }
    }

    private static boolean _isRooted()
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.rootChecked) {
            try {
                Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
            } catch (Exception ignored) {}
            return rootMutex.rooted;
        }

        try {
            //PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //if (RootTools.isRootAvailable()) {
            //noinspection RedundantIfStatement
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                //PPApplication.logE("PPApplication._isRooted", "root available");
                rootMutex.rooted = true;
            } else {
                //PPApplication.logE("PPApplication._isRooted", "root NOT available");
                rootMutex.rooted = false;
                //rootMutex.settingsBinaryExists = false;
                //rootMutex.settingsBinaryChecked = false;
                //rootMutex.isSELinuxEnforcingChecked = false;
                //rootMutex.isSELinuxEnforcing = false;
                //rootMutex.suVersionChecked = false;
                //rootMutex.suVersion = null;
                //rootMutex.serviceBinaryExists = false;
                //rootMutex.serviceBinaryChecked = false;
            }
            rootMutex.rootChecked = true;
            try {
                Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Log.e("PPApplication._isRooted", Log.getStackTraceString(e));
        }
        //if (rooted)
        //	getSUVersion();
        return rootMutex.rooted;
    }

    static boolean isRooted(boolean fromUIThread) {
        if (rootMutex.rootChecked)
            return rootMutex.rooted;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            return _isRooted();
        }
    }

    static void isRootGranted(/*boolean onlyCheck*/)
    {
        RootShell.debugMode = rootToolsDebug;

        /*if (onlyCheck && rootMutex.grantRootChecked)
            return rootMutex.rootGranted;*/

        if (isRooted(false)) {
            synchronized (PPApplication.rootMutex) {
                try {
                    //PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    //noinspection StatementWithEmptyBody
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        //PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    }/* else {
                        // grant denied
                        PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }*/
                } catch (Exception e) {
                    Log.e("PPApplication.isRootGranted", Log.getStackTraceString(e));
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        } /*else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
        }*/
        //return false;
    }

    static boolean settingsBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.settingsBinaryChecked)
            return rootMutex.settingsBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.settingsBinaryChecked) {
                //PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                rootMutex.settingsBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists=" + rootMutex.settingsBinaryExists);
            return rootMutex.settingsBinaryExists;
        }
    }

    static boolean serviceBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.serviceBinaryChecked)
            return rootMutex.serviceBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.serviceBinaryChecked) {
                //PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                rootMutex.serviceBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists=" + rootMutex.serviceBinaryExists);
            return rootMutex.serviceBinaryExists;
        }
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     * 
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    /*public static boolean isSELinuxEnforcing()
    {
        RootShell.debugMode = rootToolsDebug;

        synchronized (PPApplication.rootMutex) {
            if (!isSELinuxEnforcingChecked)
            {
                boolean enforcing = false;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                //if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                            //noinspection TryFinallyCanBeTryWithResources
                            try {
                                enforcing = (is.read() == '1');
                            } finally {
                                is.close();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                //}

                isSELinuxEnforcing = enforcing;
                isSELinuxEnforcingChecked = true;
            }

            PPApplication.logE("PPApplication.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);

            return isSELinuxEnforcing;
        }
    }*/

    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
        if ((suVersion != null) && suVersion.contains("SUPERSU"))
            return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
        else
            return command;
    }

    public static String getSUVersion()
    {
        if (!suVersionChecked)
        {
            Command command = new Command(0, false, "su -v")
            {
                @Override
                public void commandOutput(int id, String line) {
                    suVersion = line;

                    super.commandOutput(id, line);
                }
            }
            ;
            try {
                RootTools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
            } catch (Exception e) {
                Log.e("PPApplication.getSUVersion", Log.getStackTraceString(e));
            }
        }
        return suVersion;
    }
    */

    public static String getJavaCommandFile(Class<?> mainClass, String name, Context context, Object cmdParam) {
        try {
            String cmd =
                    "#!/system/bin/sh\n" +
                            "base=/system\n" +
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            /*String dir = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).dataDir;
            File fDir = new File(dir);
            File file = new File(fDir, name);
            OutputStream out = new FileOutputStream(file);
            out.write(cmd.getBytes());
            out.close();*/

            FileOutputStream fos = context.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(name);
            if (!file.setExecutable(true))
                return null;

            return file.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }

    static void getServicesList() {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList == null)
                serviceListMutex.serviceList = new ArrayList<>();
            else
                serviceListMutex.serviceList.clear();
        }

        try
        {
            //noinspection RegExpRedundantEscape
            Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Process p=Runtime.getRuntime().exec("service list");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = compile.matcher(line);
                if (matcher.find()) {
                    synchronized (PPApplication.serviceListMutex) {
                        //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                        serviceListMutex.serviceList.add(Pair.create(matcher.group(1), matcher.group(2)));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
        }

        /*
        synchronized (PPApplication.rootMutex) {
            //noinspection RegExpRedundantEscape
            final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Command command = new Command(0, false, "service list") {
                @Override
                public void commandOutput(int id, String line) {
                    //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - line="+line);
                    Matcher matcher = compile.matcher(line);
                    if (matcher.find()) {
                        synchronized (PPApplication.serviceListMutex) {
                            //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                            serviceListMutex.serviceList.add(Pair.create(matcher.group(1), matcher.group(2)));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                        }
                    }
                    super.commandOutput(id, line);
                }
            };
            try {
                RootTools.getShell(false).add(command);
                commandWait(command);
            } catch (Exception e) {
                Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
            }
        }
        */
    }

    static Object getServiceManager(String serviceType) {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList != null) {
                for (Pair pair : serviceListMutex.serviceList) {
                    if (serviceType.equals(pair.first)) {
                        return pair.second;
                    }
                }
            }
            return null;
        }
    }

    static int getTransactionCode(String serviceManager, String method) {
        int code = -1;
        try {
            for (Class declaredFields : Class.forName(serviceManager).getDeclaredClasses()) {
                Field[] declaredFields2 = declaredFields.getDeclaredFields();
                int length = declaredFields2.length;
                int iField = 0;
                while (iField < length) {
                    Field field = declaredFields2[iField];
                    String name = field.getName();
                    if (method.isEmpty()) {
                        //if (name.contains("TRANSACTION_"))
                        //    PPApplication.logE("[LIST] PPApplication.getTransactionCode", "field.getName()="+name);
                        iField++;
                    }
                    else {
                        if (/*name == null ||*/ !name.equals("TRANSACTION_" + method)) {
                            iField++;
                        } else {
                            try {
                                field.setAccessible(true);
                                code = field.getInt(field);
                                break;
                            } catch (Exception e) {
                                Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
        }
        return code;
    }

    static String getServiceCommand(String serviceType, int transactionCode, Object... params) {
        if (params.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service").append(" ").append("call").append(" ").append(serviceType).append(" ").append(transactionCode);
            for (Object param : params) {
                if (param != null) {
                    stringBuilder.append(" ");
                    if (param instanceof Integer) {
                        stringBuilder.append("i32").append(" ").append(param);
                    } else if (param instanceof String) {
                        stringBuilder.append("s16").append(" ").append("'").append(((String) param).replace("'", "'\\''")).append("'");
                    }
                }
            }
            return stringBuilder.toString();
        }
        else
            return null;
    }

    static void commandWait(Command cmd) /*throws Exception*/ {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 milliseconds (3200 * 2 - 50)
        // 1.              50
        // 2. 2 * 50 =    100
        // 3. 2 * 100 =   200
        // 4. 2 * 200 =   400
        // 5. 2 * 400 =   800
        // 6. 2 * 800 =  1600
        // 7. 2 * 1600 = 3200
        // ------------------
        //               6350

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    //if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    //}
                } catch (InterruptedException e) {
                    Log.e("PPApplication.commandWait", Log.getStackTraceString(e));
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PPApplication.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForWifiScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.reregisterReceiversForWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartWifiScanner(Context context, boolean forScreenOn) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.reregisterReceiversForBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartBluetoothScanner(Context context, boolean forScreenOn) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartGeofenceScanner(Context context, boolean forScreenOn) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartGeofenceScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_GEOFENCE_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_GEOFENCE_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void forceStartPhoneStateScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PhoneProfilesService.forceStartPhoneStateScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_PHONE_STATE_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_PHONE_STATE_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartPhoneStateScanner(Context context, boolean forScreenOn) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartPhoneStateScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PHONE_STATE_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PHONE_STATE_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartTwilightScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartAllScanners(Context context, boolean forScreenOn) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartAllScanners", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartEvents(Context context, boolean unblockEventsRun, boolean reactivateProfile) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartEvents", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    /*
    static boolean isScreenOn(PowerManager powerManager) {
        //if (Build.VERSION.SDK_INT >= 20)
            return powerManager.isInteractive();
        //else
        //    return powerManager.isScreenOn();
    }
    */

    public static void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

    /*
    private static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
    */

    private static boolean isXiaomi() {
        return Build.BRAND.equalsIgnoreCase("xiaomi") ||
               Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
               Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static boolean isMIUIROM() {
        boolean miuiRom1 = false;
        boolean miuiRom2 = false;
        boolean miuiRom3 = false;

        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            miuiRom1 = line.length() != 0;
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = line.length() != 0;
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = line.length() != 0;
                input.close();
            }

        } catch (IOException ex) {
            Log.e("PPApplication.isMIUIROM", Log.getStackTraceString(ex));
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom1=" + miuiRom1);
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom2=" + miuiRom2);
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom3=" + miuiRom3);
        }*/

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    private static String getEmuiRomName() {
        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.build.version.emui");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        } catch (IOException ex) {
            Log.e("PPApplication.getEmuiRomName", Log.getStackTraceString(ex));
            return "";
        }
    }

    private static boolean isHuawei() {
        return Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isEMUIROM() {
        String emuiRomName = getEmuiRomName();
        //PPApplication.logE("PPApplication.isEMUIROM", "emuiRomName="+emuiRomName);

        return (emuiRomName.length() != 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3");// || "EMUI 2.3".equalsIgnoreCase(emuiRomName);
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    private static boolean isLG() {
        //PPApplication.logE("PPApplication.isLG", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isLG", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isLG", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("lge") ||
                Build.MANUFACTURER.equalsIgnoreCase("lge") ||
                Build.FINGERPRINT.toLowerCase().contains("lge");
    }

    private static boolean isOnePlus() {
        //PPApplication.logE("PPApplication.isOnePlus", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isOnePlus", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isOnePlus", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("oneplus") ||
                Build.MANUFACTURER.equalsIgnoreCase("oneplus") ||
                Build.FINGERPRINT.toLowerCase().contains("oneplus");
    }

    private static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo") ||
                Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                Build.FINGERPRINT.toLowerCase().contains("oppo");
    }

    private static String getReadableModVersion() {
        String modVer = getSystemProperty(SYS_PROP_MOD_VERSION);
        return (modVer == null || modVer.length() == 0 ? "Unknown" : modVer);
    }

    @SuppressWarnings("SameParameterValue")
    private static String getSystemProperty(String propName)
    {
        String line;
        BufferedReader input = null;
        try
        {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex)
        {
            Log.e("PPApplication.getSystemProperty", "Unable to read sysprop " + propName, ex);
            return null;
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    Log.e("PPApplication.getSystemProperty", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    static boolean hasSystemFeature(Context context, String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    private static void _exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            PPApplication.logE("PPApplication._exitApp", "shutdown="+shutdown);
            // stop all events
            //if (removeAlarmClock)
            //    ApplicationPreferences.forceNotUseAlarmClock = true;

            if (dataWrapper != null)
                dataWrapper.stopAllEvents(false, false);

            if (!shutdown) {

                // remove notifications
                ImportantInfoNotification.removeNotification(context);
                IgnoreBatteryOptimizationNotification.removeNotification(context);
                Permissions.removeNotifications(context);

                if (dataWrapper != null)
                    dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_EXIT, null, null, null, 0);

                /*if (PPApplication.brightnessHandler != null) {
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeBrightnessView(context);
                        }
                    });
                }*/
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            //ActivateProfileHelper.removeBrightnessView(context);
                            ActivateProfileHelper.removeKeepScreenOnView();
                        }
                    });
                }

                //PPApplication.initRoot();
            }

            if (dataWrapper != null) {
                if (!dataWrapper.profileListFilled)
                    dataWrapper.fillProfileList(false, false);
                for (Profile profile : dataWrapper.profileList)
                    ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);

                if (!dataWrapper.eventListFilled)
                    dataWrapper.fillEventList();
                for (Event event : dataWrapper.eventList)
                    StartEventNotificationBroadcastReceiver.removeAlarm(event, context);
            }
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
            Profile.setActivatedProfileForDuration(context, 0);
            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            PPApplication.logE("PPApplication._exitApp", "stop service");
            // maybe fixes ANR Context.startForegroundService() did not then call Service.startForeground
            //PhoneProfilesService.getInstance().showProfileNotification(false);
            //context.stopService(new Intent(context, PhoneProfilesService.class));
            PhoneProfilesService.stop(context);

            Permissions.setAllShowRequestPermissions(context.getApplicationContext(), true);

            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            //PhoneStateScanner.setShowEnableLocationNotification(context.getApplicationContext(), true);
            //ActivateProfileHelper.setScreenUnlocked(context, true);

            PPApplication.logE("PPApplication._exitApp", "set application started = false");
            PPApplication.setApplicationStarted(context, false);

            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from PPApplication._exitApp");
            ActivateProfileHelper.updateGUI(context, false, true);

            if (!shutdown) {
                Handler _handler = new Handler(context.getMainLooper());
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            if (activity != null)
                                activity.finish();
                        } catch (Exception ignored) {}
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }

        } catch (Exception e) {
            Log.e("PPApplication._exitApp", Log.getStackTraceString(e));
        }
    }

    static void exitApp(final boolean useHandler, final Context context, final DataWrapper dataWrapper, final Activity activity,
                                 final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            if (useHandler) {
                PPApplication.startHandlerThread("PPApplication.exitApp");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_exitApp");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPApplication.exitApp");

                            _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.exitApp");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
            else
                _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);
        } catch (Exception ignored) {

        }
    }

    static void showDoNotKillMyAppDialog(final Fragment fragment) {
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getActivity());
        dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);

        LayoutInflater inflater = fragment.getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_do_not_kill_my_app_dialog, null);
        dialogBuilder.setView(layout);

        DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
        if (doki != null) {
            doki.setButtonsVisibility(false);
            doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
        }

        dialogBuilder.show();
    }

    static void startHandlerThread(@SuppressWarnings("unused") String from) {
        //PPApplication.logE("PPApplication.startHandlerThread", "from="+from);
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThread.start();
        }
    }

    static void startHandlerThreadPPScanners() {
        if (handlerThreadPPScanners == null) {
            handlerThreadPPScanners = new HandlerThread("PPHandlerThreadPPScanners", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPScanners.start();
        }
    }

    static void startHandlerThreadPPCommand() {
        if (handlerThreadPPCommand == null) {
            handlerThreadPPCommand = new HandlerThread("PPHandlerThreadPPCommand", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPCommand.start();
        }
    }

    /*
    static void startHandlerThreadInternalChangeToFalse() {
        if (handlerThreadInternalChangeToFalse == null) {
            handlerThreadInternalChangeToFalse = new HandlerThread("PPHandlerThreadInternalChangeToFalse", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadInternalChangeToFalse.start();
        }
    }
    */
    /*
    private static void startHandlerThreadRoot() {
        if (handlerThreadRoot == null) {
            handlerThreadRoot = new HandlerThread("PPHandlerThreadRoot", THREAD_PRIORITY_MORE_FAVORABLE); //);;
            handlerThreadRoot.start();
        }
    }
    */

    static void startHandlerThreadWidget() {
        if (handlerThreadWidget == null) {
            handlerThreadWidget = new HandlerThread("PPHandlerThreadWidget", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWidget.start();
        }
    }

    static void startHandlerThreadProfileNotification() {
        if (handlerThreadProfileNotification == null) {
            handlerThreadProfileNotification = new HandlerThread("PPHandlerThreadProfileNotification", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadProfileNotification.start();
        }
    }

    static void startHandlerThreadPlayTone() {
        if (handlerThreadPlayTone == null) {
            handlerThreadPlayTone = new HandlerThread("PPHandlerThreadPlayTone", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPlayTone.start();
        }
    }

    static void startHandlerThreadVolumes() {
        if (handlerThreadVolumes == null) {
            handlerThreadVolumes = new HandlerThread("handlerThreadVolumes", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadVolumes.start();
        }
    }

    static void startHandlerThreadRadios() {
        if (handlerThreadRadios == null) {
            handlerThreadRadios = new HandlerThread("handlerThreadRadios", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRadios.start();
        }
    }

    static void startHandlerThreadAdaptiveBrightness() {
        if (handlerThreadAdaptiveBrightness == null) {
            handlerThreadAdaptiveBrightness = new HandlerThread("handlerThreadAdaptiveBrightness", THREAD_PRIORITY_MORE_FAVORABLE); //);;
            handlerThreadAdaptiveBrightness.start();
        }
    }

    static void startHandlerThreadWallpaper() {
        if (handlerThreadWallpaper == null) {
            handlerThreadWallpaper = new HandlerThread("handlerThreadWallpaper", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWallpaper.start();
        }
    }

    static void startHandlerThreadPowerSaveMode() {
        if (handlerThreadPowerSaveMode == null) {
            handlerThreadPowerSaveMode = new HandlerThread("handlerThreadPowerSaveMode", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPowerSaveMode.start();
        }
    }

    static void startHandlerThreadLockDevice() {
        if (handlerThreadLockDevice == null) {
            handlerThreadLockDevice = new HandlerThread("handlerThreadLockDevice", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadLockDevice.start();
        }
    }

    static void startHandlerThreadRunApplication() {
        if (handlerThreadRunApplication == null) {
            handlerThreadRunApplication = new HandlerThread("handlerThreadRunApplication", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRunApplication.start();
        }
    }

    static void startHandlerThreadHeadsUpNotifications() {
        if (handlerThreadHeadsUpNotifications == null) {
            handlerThreadHeadsUpNotifications = new HandlerThread("handlerThreadHeadsUpNotifications", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadHeadsUpNotifications.start();
        }
    }

    /*
    static void startHandlerThreadMobileCells() {
        if (handlerThreadMobileCells == null) {
            handlerThreadMobileCells = new HandlerThread("handlerThreadMobileCells", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadMobileCells.start();
        }
    }
    */
    /*
    static void startHandlerThreadRestartEventsWithDelay() {
        if (handlerThreadRestartEventsWithDelay == null) {
            handlerThreadRestartEventsWithDelay = new HandlerThread("handlerThreadRestartEventsWithDelay", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRestartEventsWithDelay.start();
            restartEventsWithDelayHandler = new Handler(PPApplication.handlerThreadRestartEventsWithDelay.getLooper());
        }
    }
    */

    static void startHandlerThreadBluetoothConnectedDevices() {
        if (handlerThreadBluetoothConnectedDevices == null) {
            handlerThreadBluetoothConnectedDevices = new HandlerThread("handlerThreadBluetoothConnectedDevices", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadBluetoothConnectedDevices.start();
        }
    }

    /*
    static void startHandlerThreadBluetoothLECallback() {
        if (handlerThreadBluetoothLECallback == null) {
            handlerThreadBluetoothLECallback = new HandlerThread("handlerThreadBluetoothLECallback", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadBluetoothLECallback.start();
        }
    }
    */

    static void startHandlerThreadNotificationLed() {
        if (handlerThreadNotificationLed == null) {
            handlerThreadNotificationLed = new HandlerThread("handlerThreadNotificationLed", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadNotificationLed.start();
        }
    }

    static void startHandlerThreadAlwaysOnDisplay() {
        if (handlerThreadAlwaysOnDisplay == null) {
            handlerThreadAlwaysOnDisplay = new HandlerThread("handlerThreadAlwaysOnDisplay", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadAlwaysOnDisplay.start();
        }
    }

    static void startHandlerThreadOrientationScanner() {
        if (handlerThreadOrientationScanner == null) {
            handlerThreadOrientationScanner = new OrientationScannerHandlerThread("PPHandlerThreadOrientationScanner", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadOrientationScanner.start();
        }
    }

    static void setBlockProfileEventActions(boolean enable, Context context) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                            .addTag("setBlockProfileEventsActionWork")
                            .setInputData(workData)
                            .setInitialDelay(30, TimeUnit.SECONDS)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());
                workManager.enqueueUniqueWork("setBlockProfileEventsActionWork", ExistingWorkPolicy.REPLACE, worker);
            } catch (Exception ignored) {}

            /*PPApplication.startHandlerThread("PPApplication.setBlockProfileEventActions");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPApplication.setBlockProfileEventActions");

                    PPApplication.logE("PPApplication.setBlockProfileEventActions", "delayed boot up");
                    PPApplication.blockProfileEventActions = false;

                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.setBlockProfileEventActions");
                }
            }, 30000);*/
        }
    }

/*    //-----------------------------

    private static WeakReference<Activity> foregroundEditorActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity = null;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    static Activity getEditorActivity() {
        if (foregroundEditorActivity != null && foregroundEditorActivity.get() != null) {
            return foregroundEditorActivity.get();
        }
        return null;
    }
*/

    // Sensor manager ------------------------------------------------------------------------------

    static Sensor getAccelerometerSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
            return null;
    }

    static Sensor getMagneticFieldSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else
            return null;
    }

    static Sensor getProximitySensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        else
            return null;
    }

    /*
    private Sensor getOrientationSensor(Context context) {
        synchronized (PPApplication.orientationScannerMutex) {
            if (mOrientationSensorManager == null)
                mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }*/

    static Sensor getLightSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        else
            return null;
    }

    // Google Analytics ----------------------------------------------------------------------------

    /*
    static void logAnalyticsEvent(String itemId, String itemName, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    */

    //---------------------------------------------------------------------------------------------

}
