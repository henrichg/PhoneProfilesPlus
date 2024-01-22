package sk.henrichg.phoneprofilesplus;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.internal.telephony.TelephonyIntents;

import java.lang.ref.WeakReference;
import java.util.List;

class PhoneProfilesServiceStatic
{

    static void registerAllTheTimeRequiredPPPBroadcastReceivers(boolean register, Context context) {
        final Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.startEventNotificationDeletedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationDeletedReceiver);
                    PPApplication.startEventNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationDeletedReceiver = null;
                }
            }
            /*if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver);
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                }
            }*/
            if (PPApplication.eventDelayStartBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayStartBroadcastReceiver);
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                }
            }
            if (PPApplication.eventDelayEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayEndBroadcastReceiver);
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                }
            }
            if (PPApplication.profileDurationAlarmBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.profileDurationAlarmBroadcastReceiver);
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                }
            }
            if (PPApplication.runApplicationWithDelayBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver);
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                }
            }
            if (PPApplication.startEventNotificationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationBroadcastReceiver);
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                }
            }
            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver);
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                }
            }
            if (PPApplication.pppExtenderBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderBroadcastReceiver);
                    PPApplication.pppExtenderBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderBroadcastReceiver = null;
                }
            }
            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver);
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                }
            }
            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver);
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                }
            }

            if (PPApplication.donationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.donationBroadcastReceiver);
                    PPApplication.donationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.donationBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkPPPReleasesBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.checkPPPReleasesBroadcastReceiver);
                    PPApplication.checkPPPReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkPPPReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkCriticalPPPReleasesBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.checkCriticalPPPReleasesBroadcastReceiver);
                    PPApplication.checkCriticalPPPReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkCriticalPPPReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkRequiredExtenderReleasesBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.checkRequiredExtenderReleasesBroadcastReceiver);
                    PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkLatestPPPPSReleasesBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.checkLatestPPPPSReleasesBroadcastReceiver);
                    PPApplication.checkLatestPPPPSReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkLatestPPPPSReleasesBroadcastReceiver = null;
                }
            }
            /*if (PPApplication.restartEventsWithDelayBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.restartEventsWithDelayBroadcastReceiver);
                    PPApplication.restartEventsWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.restartEventsWithDelayBroadcastReceiver = null;
                }
            }*/

            if (Build.VERSION.SDK_INT >= 33) {
                if (PPApplication.ppAppNotificationDeletedReceiver != null) {
                    try {
                        appContext.unregisterReceiver(PPApplication.ppAppNotificationDeletedReceiver);
                        PPApplication.ppAppNotificationDeletedReceiver = null;
                    } catch (Exception e) {
                        PPApplication.ppAppNotificationDeletedReceiver = null;
                    }
                }
                if (PPApplication.keepScreenOnNotificationDeletedReceiver != null) {
                    try {
                        appContext.unregisterReceiver(PPApplication.keepScreenOnNotificationDeletedReceiver);
                        PPApplication.keepScreenOnNotificationDeletedReceiver = null;
                    } catch (Exception e) {
                        PPApplication.keepScreenOnNotificationDeletedReceiver = null;
                    }
                }
                if (PPApplication.profileListNotificationDeletedReceiver != null) {
                    try {
                        appContext.unregisterReceiver(PPApplication.profileListNotificationDeletedReceiver);
                        PPApplication.profileListNotificationDeletedReceiver = null;
                    } catch (Exception e) {
                        PPApplication.profileListNotificationDeletedReceiver = null;
                    }
                }
            }

        }
        if (register) {

            if (PPApplication.startEventNotificationDeletedReceiver == null) {
                PPApplication.startEventNotificationDeletedReceiver = new StartEventNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(StartEventNotificationDeletedReceiver.ACTION_START_EVENT_NOTIFICATION_DELETED);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.startEventNotificationDeletedReceiver, intentFilter5, receiverFlags);
            }

            /*if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver == null) {
                PPApplication.notUsedMobileCellsNotificationDeletedReceiver = new NotUsedMobileCellsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(MobileCellsScanner.ACTION_NEW_MOBILE_CELLS_NOTIFICATION_DELETED);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver, intentFilter5, receiverFlags);
            }*/

            if (PPApplication.eventDelayStartBroadcastReceiver == null) {
                PPApplication.eventDelayStartBroadcastReceiver = new EventDelayStartBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.eventDelayStartBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.eventDelayEndBroadcastReceiver == null) {
                PPApplication.eventDelayEndBroadcastReceiver = new EventDelayEndBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.eventDelayEndBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.profileDurationAlarmBroadcastReceiver == null) {
                PPApplication.profileDurationAlarmBroadcastReceiver = new ProfileDurationAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.profileDurationAlarmBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.runApplicationWithDelayBroadcastReceiver == null) {
                PPApplication.runApplicationWithDelayBroadcastReceiver = new RunApplicationWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.startEventNotificationBroadcastReceiver == null) {
                PPApplication.startEventNotificationBroadcastReceiver = new StartEventNotificationBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.startEventNotificationBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver == null) {
                PPApplication.lockDeviceActivityFinishBroadcastReceiver = new LockDeviceActivityFinishBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.pppExtenderBroadcastReceiver == null) {
                PPApplication.pppExtenderBroadcastReceiver = new PPExtenderBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PPApplication.ACTION_PPPEXTENDER_STARTED);
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_EXPORTED;
                appContext.registerReceiver(PPApplication.pppExtenderBroadcastReceiver, intentFilter14,
                        PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
            }

            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver == null) {
                PPApplication.notUsedMobileCellsNotificationDisableReceiver = new NotUsedMobileCellsNotificationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(MobileCellsScanner.ACTION_NEW_MOBILE_CELLS_NOTIFICATION_DISABLE);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver, intentFilter5, receiverFlags);
            }
            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver == null) {
                PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = new LockDeviceAfterScreenOffBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver, intentFilter14, receiverFlags);
            }

            if (PPApplication.donationBroadcastReceiver == null) {
                PPApplication.donationBroadcastReceiver = new DonationBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_DONATION);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.donationBroadcastReceiver, intentFilter5, receiverFlags);
            }
            if (PPApplication.checkPPPReleasesBroadcastReceiver == null) {
                PPApplication.checkPPPReleasesBroadcastReceiver = new CheckPPPReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.checkPPPReleasesBroadcastReceiver, intentFilter5, receiverFlags);
            }
            if (PPApplication.checkCriticalPPPReleasesBroadcastReceiver == null) {
                PPApplication.checkCriticalPPPReleasesBroadcastReceiver = new CheckCriticalPPPReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.checkCriticalPPPReleasesBroadcastReceiver, intentFilter5, receiverFlags);
            }
            if (PPApplication.checkRequiredExtenderReleasesBroadcastReceiver == null) {
                PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = new CheckRequiredExtenderReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.checkRequiredExtenderReleasesBroadcastReceiver, intentFilter5, receiverFlags);
            }
            if (PPApplication.checkLatestPPPPSReleasesBroadcastReceiver == null) {
                PPApplication.checkLatestPPPPSReleasesBroadcastReceiver = new CheckLatestPPPPSReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_LATEST_PPPPS_RELEASES);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.checkLatestPPPPSReleasesBroadcastReceiver, intentFilter5, receiverFlags);
            }
            /*if (PPApplication.restartEventsWithDelayBroadcastReceiver == null) {
                PPApplication.restartEventsWithDelayBroadcastReceiver = new RestartEventsWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RESTART_EVENTS_WITH_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.restartEventsWithDelayBroadcastReceiver, intentFilter14);
            }*/

            if (Build.VERSION.SDK_INT >= 33) {
                if (PPApplication.ppAppNotificationDeletedReceiver == null) {
                    PPApplication.ppAppNotificationDeletedReceiver = new PPAppNotificationDeletedReceiver();
                    IntentFilter intentFilter5 = new IntentFilter();
                    intentFilter5.addAction(PPAppNotificationDeletedReceiver.ACTION_PP_APP_NOTIFICATION_DELETED);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.ppAppNotificationDeletedReceiver, intentFilter5, receiverFlags);
                }
                if (PPApplication.keepScreenOnNotificationDeletedReceiver == null) {
                    PPApplication.keepScreenOnNotificationDeletedReceiver = new KeepScreenOnNotificationDeletedReceiver();
                    IntentFilter intentFilter5 = new IntentFilter();
                    intentFilter5.addAction(KeepScreenOnNotificationDeletedReceiver.ACTION_KEEP_SCREEN_ON_NOTIFICATION_DELETED);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.keepScreenOnNotificationDeletedReceiver, intentFilter5, receiverFlags);
                }
                if (PPApplication.profileListNotificationDeletedReceiver == null) {
                    PPApplication.profileListNotificationDeletedReceiver = new ProfileListNotificationDeletedReceiver();
                    IntentFilter intentFilter5 = new IntentFilter();
                    intentFilter5.addAction(ProfileListNotificationDeletedReceiver.ACTION_PROFILE_LIST_NOTIFICATION_DELETED);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.profileListNotificationDeletedReceiver, intentFilter5, receiverFlags);
                }
            }

        }
    }

    static void registerAllTheTimeRequiredSystemReceivers(boolean register, Context context) {
        final Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.timeChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.timeChangedReceiver);
                    PPApplication.timeChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.timeChangedReceiver = null;
                }
            }
            if (PPApplication.shutdownBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.shutdownBroadcastReceiver);
                    PPApplication.shutdownBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.shutdownBroadcastReceiver = null;
                }
            }
            if (PPApplication.screenOnOffReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.screenOnOffReceiver);
                    PPApplication.screenOnOffReceiver = null;
                } catch (Exception e) {
                    PPApplication.screenOnOffReceiver = null;
                }
            }
            if (PPApplication.interruptionFilterChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.interruptionFilterChangedReceiver);
                    PPApplication.interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.interruptionFilterChangedReceiver = null;
                }
            }

            registerPhoneCallsListener(false, appContext);

            if (PPApplication.ringerModeChangeReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.ringerModeChangeReceiver);
                    PPApplication.ringerModeChangeReceiver = null;
                } catch (Exception e) {
                    PPApplication.ringerModeChangeReceiver = null;
                }
            }
            if (PPApplication.deviceIdleModeReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.deviceIdleModeReceiver);
                    PPApplication.deviceIdleModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.deviceIdleModeReceiver = null;
                }
            }
            if (PPApplication.bluetoothStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver);
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                }
            }
            if (PPApplication.bluetoothConnectionBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothConnectionBroadcastReceiver);
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                }
            }
            if (PPApplication.wifiStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiStateChangedBroadcastReceiver);
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                }
            }
            if (PPApplication.powerSaveModeReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.powerSaveModeReceiver);
                    PPApplication.powerSaveModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.powerSaveModeReceiver = null;
                }
            }
            if (PPApplication.checkOnlineStatusBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.checkOnlineStatusBroadcastReceiver);
                    PPApplication.checkOnlineStatusBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkOnlineStatusBroadcastReceiver = null;
                }
            }
            if (PPApplication.simStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.simStateChangedBroadcastReceiver);
                    PPApplication.simStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.simStateChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (PPApplication.timeChangedReceiver == null) {
                //PPApplication.lastUptimeTime = SystemClock.elapsedRealtime();
                //PPApplication.lastEpochTime = System.currentTimeMillis();
                PPApplication.timeChangedReceiver = new TimeChangedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                //intentFilter5.addAction(Intent.ACTION_TIME_TICK);
                //intentFilter5.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter5.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                appContext.registerReceiver(PPApplication.timeChangedReceiver, intentFilter5);
            }

            if (PPApplication.shutdownBroadcastReceiver == null) {
                PPApplication.shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SHUTDOWN);
                intentFilter5.addAction("android.intent.action.QUICKBOOT_POWEROFF");
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                appContext.registerReceiver(PPApplication.shutdownBroadcastReceiver, intentFilter5, receiverFlags);
            }

            // required for Lock device, Hide notification in lock screen, screen timeout +
            // screen on/off event + rescan wifi, bluetooth, location, mobile cells
            if (PPApplication.screenOnOffReceiver == null) {
                PPApplication.screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
                appContext.registerReceiver(PPApplication.screenOnOffReceiver, intentFilter5);
            }

            // required for Do not disturb ringer mode
            if (PPApplication.interruptionFilterChangedReceiver == null) {
                //if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                    PPApplication.interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                    IntentFilter intentFilter11 = new IntentFilter();
                    intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                    appContext.registerReceiver(PPApplication.interruptionFilterChangedReceiver, intentFilter11);
                //}
            }

            // required for unlink ring and notification volume
            registerPhoneCallsListener(true, appContext);

            // required for unlink ring and notification volume
            if (PPApplication.ringerModeChangeReceiver == null) {
                PPApplication.ringerModeChangeReceiver = new RingerModeChangeReceiver();
                IntentFilter intentFilter7 = new IntentFilter();
                intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.ringerModeChangeReceiver, intentFilter7);
            }

            // required for start EventsHandler in idle maintenance window
            if (PPApplication.deviceIdleModeReceiver == null) {
                PPApplication.deviceIdleModeReceiver = new DeviceIdleModeBroadcastReceiver();
                IntentFilter intentFilter9 = new IntentFilter();
                intentFilter9.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
                // is @hide :-(
                // intentFilter9.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
                appContext.registerReceiver(PPApplication.deviceIdleModeReceiver, intentFilter9);
            }

            if (PPApplication.bluetoothStateChangedBroadcastReceiver == null) {
                PPApplication.bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
                IntentFilter intentFilter15 = new IntentFilter();
                intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                appContext.registerReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver, intentFilter15);
            }

            // required for (un)register connected bluetooth devices
            if (PPApplication.bluetoothConnectionBroadcastReceiver == null) {
                PPApplication.bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                appContext.registerReceiver(PPApplication.bluetoothConnectionBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.wifiStateChangedBroadcastReceiver == null) {
                PPApplication.wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
                IntentFilter intentFilter8 = new IntentFilter();
                intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.wifiStateChangedBroadcastReceiver, intentFilter8);
            }

            if (PPApplication.powerSaveModeReceiver == null) {
                PPApplication.powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                appContext.registerReceiver(PPApplication.powerSaveModeReceiver, intentFilter10);
            }

            if (PPApplication.checkOnlineStatusBroadcastReceiver == null) {
                PPApplication.checkOnlineStatusBroadcastReceiver = new CheckOnlineStatusBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                intentFilter10.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                appContext.registerReceiver(PPApplication.checkOnlineStatusBroadcastReceiver, intentFilter10);
            }

            if (PPApplication.simStateChangedBroadcastReceiver == null) {
                PPApplication.simStateChangedBroadcastReceiver = new SimStateChangedBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                // https://android.googlesource.com/platform/frameworks/base/+/84303f5/telephony/java/com/android/internal/telephony/TelephonyIntents.java
                // this requires READ_PHONE_STATE
                intentFilter10.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED); //"android.intent.action.SIM_STATE_CHANGED");
                appContext.registerReceiver(PPApplication.simStateChangedBroadcastReceiver, intentFilter10);
            }
        }
    }

    static void registerPhoneCallsListener(final boolean register, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for cal listener
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            // unregister and then register, when register=true
            //if (!register) {
                if (PPApplication.phoneCallsListenerSIM1 != null) {
                    try {
                        if (PPApplication.telephonyManagerSIM1 != null)
                            PPApplication.telephonyManagerSIM1.listen(PPApplication.phoneCallsListenerSIM1, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerSIM1 = null;
                        PPApplication.telephonyManagerSIM1 = null;
                    } catch (Exception ignored) {
                    }
                }
                if (PPApplication.phoneCallsListenerSIM2 != null) {
                    try {
                        if (PPApplication.telephonyManagerSIM2 != null)
                            PPApplication.telephonyManagerSIM2.listen(PPApplication.phoneCallsListenerSIM2, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerSIM2 = null;
                        PPApplication.telephonyManagerSIM2 = null;
                    } catch (Exception ignored) {
                    }
                }
                if (PPApplication.phoneCallsListenerDefaul != null) {
                    try {
                        if (PPApplication.telephonyManagerDefault != null)
                            PPApplication.telephonyManagerDefault.listen(PPApplication.phoneCallsListenerDefaul, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerDefaul = null;
                        PPApplication.telephonyManagerDefault = null;
                    } catch (Exception ignored) {
                    }
                }
            //} else {
            if (register) {
                GlobalUtils.sleep(1000);

                PPApplication.telephonyManagerDefault = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (PPApplication.telephonyManagerDefault != null) {
                    int simCount = PPApplication.telephonyManagerDefault.getPhoneCount();
                    if (simCount > 1) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
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
                                            if (PPApplication.telephonyManagerSIM1 == null) {
                                                try {
                                                    PPApplication.telephonyManagerSIM1 = PPApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                                    PPApplication.phoneCallsListenerSIM1 = new PhoneCallsListener(appContext, 1);
                                                    //noinspection deprecation
                                                    PPApplication.telephonyManagerSIM1.listen(PPApplication.phoneCallsListenerSIM1,
                                                            PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                                                } catch (Exception e) {
                                                    PPApplication.phoneCallsListenerSIM1 = null;
                                                    PPApplication.telephonyManagerSIM1 = null;
                                                    PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                        if ((subscriptionInfo.getSimSlotIndex() == 1)) {
                                            if (PPApplication.telephonyManagerSIM2 == null) {
                                                try {
                                                    PPApplication.telephonyManagerSIM2 = PPApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                                    PPApplication.phoneCallsListenerSIM2 = new PhoneCallsListener(appContext, 2);
                                                    //noinspection deprecation
                                                    PPApplication.telephonyManagerSIM2.listen(PPApplication.phoneCallsListenerSIM2,
                                                            PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                                                } catch (Exception e) {
                                                    PPApplication.phoneCallsListenerSIM2 = null;
                                                    PPApplication.telephonyManagerSIM2 = null;
                                                    PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            PPApplication.phoneCallsListenerDefaul = new PhoneCallsListener(appContext, 0);
                            //noinspection deprecation
                            PPApplication.telephonyManagerDefault.listen(PPApplication.phoneCallsListenerDefaul,
                                    PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                        } catch (Exception e) {
                            PPApplication.phoneCallsListenerDefaul = null;
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            }
        });
    }

    static void registerAllTheTimeContentObservers(final boolean register, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast observers
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.settingsContentObserver != null) {
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.settingsContentObserver);
                        PPApplication.settingsContentObserver = null;
                    } catch (Exception e) {
                        PPApplication.settingsContentObserver = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.settingsContentObserver == null) {
                    try {
                        //settingsContentObserver = new SettingsContentObserver(appContext, new Handler(getMainLooper()));
                        PPApplication.settingsContentObserver = new SettingsContentObserver(appContext, new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                        appContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, PPApplication.settingsContentObserver);
                    } catch (Exception e) {
                        PPApplication.settingsContentObserver = null;
                        //PPApplicationStatic.recordException(e);
                    }
                }
            }
        });
    }

    static  void registerContactsContentObservers(final boolean register, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for observers
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.contactsContentObserver != null) {
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.contactsContentObserver);
                        PPApplication.contactsContentObserver = null;
                    } catch (Exception e) {
                        PPApplication.contactsContentObserver = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.contactsContentObserver == null) {
                    try {
                        if (Permissions.checkContacts(appContext)) {
                            PPApplication.contactsContentObserver = new ContactsContentObserver(new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                            appContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, PPApplication.contactsContentObserver);
                        }
                    } catch (Exception e) {
                        PPApplication.contactsContentObserver = null;
                        //PPApplicationStatic.recordException(e);
                    }
                }
            }
        });
    }

    static void registerAllTheTimeCallbacks(final boolean register, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for callbacks
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.wifiConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.wifiConnectionCallback);
                        }
                        PPApplication.wifiConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.wifiConnectionCallback = null;
                    }
                }
                if (PPApplication.mobileDataConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.mobileDataConnectionCallback);
                        }
                        PPApplication.mobileDataConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.mobileDataConnectionCallback = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.wifiConnectionCallback == null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            NetworkRequest networkRequest = new NetworkRequest.Builder()
                                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                    .build();

                            PPApplication.wifiConnectionCallback = new WifiNetworkCallback(/*appContext*/);
                            connectivityManager.registerNetworkCallback(networkRequest, PPApplication.wifiConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                        }
                    } catch (Exception e) {
                        PPApplication.wifiConnectionCallback = null;
                        //PPApplicationStatic.recordException(e);
                    }
                }
                if (PPApplication.mobileDataConnectionCallback == null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            NetworkRequest networkRequest = new NetworkRequest.Builder()
                                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                                    .build();

                            PPApplication.mobileDataConnectionCallback = new MobileDataNetworkCallback(/*appContext*/);
                            connectivityManager.registerNetworkCallback(networkRequest, PPApplication.mobileDataConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                        }
                    } catch (Exception e) {
                        PPApplication.mobileDataConnectionCallback = null;
                        //PPApplicationStatic.recordException(e);
                    }
                }
            }
        });
    }

    static void registerBatteryLevelChangedReceiver(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.batteryLevelChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.batteryLevelChangedReceiver);
                    PPApplication.batteryLevelChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.batteryLevelChangedReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL/*, false*/);
            }
            if (allowed) {
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                if (PPApplication.batteryLevelChangedReceiver == null) {
                    PPApplication.batteryLevelChangedReceiver = new BatteryLevelChangedBroadcastReceiver();
                    IntentFilter intentFilter1 = new IntentFilter();
                    intentFilter1.addAction(Intent.ACTION_BATTERY_CHANGED);
                    appContext.registerReceiver(PPApplication.batteryLevelChangedReceiver, intentFilter1);
                }
            }
            else
                registerBatteryLevelChangedReceiver(false, dataWrapper, appContext);
        }
    }

    static void registerBatteryChargingChangedReceiver(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.batteryChargingChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.batteryChargingChangedReceiver);
                    PPApplication.batteryChargingChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.batteryChargingChangedReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean eventsExists = false;
            if (allowed) {
                dataWrapper.fillEventList();
                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BATTERY/*, false*/);
            }
            if (!eventsExists) {
                allowed = false;
                dataWrapper.fillEventList();
                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALL_SCANNER_SENSORS/*, false*/);
                if (eventsExists) {
                    allowed = ApplicationPreferences.applicationEventWifiEnableScanning &&
                            (EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventBluetoothEnableScanning &&
                                (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventLocationEnableScanning &&
                                (EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed) {
//                        PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.registerBatteryChargingChangedReceiver", "******** ### *******");
                        allowed = ApplicationPreferences.applicationEventMobileCellEnableScanning &&
                                (EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    }
                    if (!allowed) {
//                        PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.registerBatteryChargingChangedReceiver", "******** ### *******");
                        allowed = ApplicationPreferences.applicationEventOrientationEnableScanning &&
                                (EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    }
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventPeriodicScanningEnableScanning;
                }
            }
            if (allowed) {
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                if (PPApplication.batteryChargingChangedReceiver == null) {
                    PPApplication.batteryChargingChangedReceiver = new BatteryChargingChangedBroadcastReceiver();
                    IntentFilter intentFilter1 = new IntentFilter();
                    intentFilter1.addAction(Intent.ACTION_POWER_CONNECTED);
                    intentFilter1.addAction(Intent.ACTION_POWER_DISCONNECTED);
                    appContext.registerReceiver(PPApplication.batteryChargingChangedReceiver, intentFilter1);
                }
            }
            else
                registerBatteryChargingChangedReceiver(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForAccessoriesSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.headsetPlugReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.headsetPlugReceiver);
                    PPApplication.headsetPlugReceiver = null;
                } catch (Exception e) {
                    PPApplication.headsetPlugReceiver = null;
                }
            }
            if (PPApplication.dockConnectionBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.dockConnectionBroadcastReceiver);
                    PPApplication.dockConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.dockConnectionBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ACCESSORY/*, false*/);
            }
            if (allowed) {
                if (PPApplication.headsetPlugReceiver == null) {
                    PPApplication.headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
                    IntentFilter intentFilter2 = new IntentFilter();
                    intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                    appContext.registerReceiver(PPApplication.headsetPlugReceiver, intentFilter2);
                }
                if (PPApplication.dockConnectionBroadcastReceiver == null) {
                    PPApplication.dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
                    IntentFilter intentFilter12 = new IntentFilter();
                    intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
                    intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED; // maybe working (???)
                    appContext.registerReceiver(PPApplication.dockConnectionBroadcastReceiver, intentFilter12, receiverFlags);
                }
            }
            else
                registerReceiverForAccessoriesSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForSMSSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            /*if (smsBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(smsBroadcastReceiver);
                    smsBroadcastReceiver = null;
                } catch (Exception e) {
                    smsBroadcastReceiver = null;
                }
            }
            if (mmsBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(mmsBroadcastReceiver);
                    mmsBroadcastReceiver = null;
                } catch (Exception e) {
                    mmsBroadcastReceiver = null;
                }
            }*/
            if (PPApplication.smsEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.smsEventEndBroadcastReceiver);
                    PPApplication.smsEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.smsEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS/*, false*/);
            }
            if (allowed) {
                /*if (smsBroadcastReceiver == null) {
                    smsBroadcastReceiver = new SMSBroadcastReceiver();
                    IntentFilter intentFilter21 = new IntentFilter();
                    intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                    intentFilter21.setPriority(Integer.MAX_VALUE);
                    appContext.registerReceiver(smsBroadcastReceiver, intentFilter21);
                }
                if (mmsBroadcastReceiver == null) {
                    mmsBroadcastReceiver = new SMSBroadcastReceiver();
                    IntentFilter intentFilter22;
                    intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
                    intentFilter22.setPriority(Integer.MAX_VALUE);
                    appContext.registerReceiver(mmsBroadcastReceiver, intentFilter22);
                }*/
                if (PPApplication.smsEventEndBroadcastReceiver == null) {
                    PPApplication.smsEventEndBroadcastReceiver = new SMSEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.smsEventEndBroadcastReceiver, intentFilter22, receiverFlags);
                }

//                Log.e("PhoneProfilesService.registerReceiverForSMSSensor", "xxx");
            }
            else
                registerReceiverForSMSSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForCalendarSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.calendarProviderChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.calendarProviderChangedBroadcastReceiver);
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                }
            }
            if (PPApplication.eventCalendarBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventCalendarBroadcastReceiver);
                    PPApplication.eventCalendarBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventCalendarBroadcastReceiver = null;
                }
            }
            if (PPApplication.calendarEventExistsCheckBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.calendarEventExistsCheckBroadcastReceiver);
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALENDAR/*, false*/);
            }
            if (allowed) {
                if (PPApplication.eventCalendarBroadcastReceiver == null) {
                    PPApplication.eventCalendarBroadcastReceiver = new EventCalendarBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.eventCalendarBroadcastReceiver, intentFilter23, receiverFlags);
                }
                if (PPApplication.calendarEventExistsCheckBroadcastReceiver == null) {
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = new CalendarEventExistsCheckBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_CALENDAR_EVENT_EXISTS_CHECK_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.calendarEventExistsCheckBroadcastReceiver, intentFilter23, receiverFlags);
                }
                if (PPApplication.calendarProviderChangedBroadcastReceiver == null) {
                    PPApplication.calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter();
                    intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
                    intentFilter23.addDataScheme("content");
                    intentFilter23.addDataAuthority("com.android.calendar", null);
                    intentFilter23.setPriority(Integer.MAX_VALUE);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_EXPORTED; // !!! must be exported !!!
                    appContext.registerReceiver(PPApplication.calendarProviderChangedBroadcastReceiver, intentFilter23, receiverFlags);
                }
            }
            else
                registerReceiverForCalendarSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForRadioSwitchAirplaneModeSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.airplaneModeStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.airplaneModeStateChangedBroadcastReceiver);
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE/*, false*/);
            }
            if (allowed) {
                if (PPApplication.airplaneModeStateChangedBroadcastReceiver == null) {
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = new AirplaneModeStateChangedBroadcastReceiver();
                    IntentFilter intentFilter19 = new IntentFilter();
                    intentFilter19.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    appContext.registerReceiver(PPApplication.airplaneModeStateChangedBroadcastReceiver, intentFilter19);
                }
            }
            else
                registerReceiverForRadioSwitchAirplaneModeSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForRadioSwitchNFCSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.HAS_FEATURE_NFC) {
                if (PPApplication.nfcStateChangedBroadcastReceiver != null) {
                    try {
                        appContext.unregisterReceiver(PPApplication.nfcStateChangedBroadcastReceiver);
                        PPApplication.nfcStateChangedBroadcastReceiver = null;
                    } catch (Exception e) {
                        PPApplication.nfcStateChangedBroadcastReceiver = null;
                    }
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NFC, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_NFC/*, false*/);
            }
            if (allowed) {
                if (PPApplication.nfcStateChangedBroadcastReceiver == null) {
                    if (PPApplication.HAS_FEATURE_NFC) {
                        PPApplication.nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                        IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                        appContext.registerReceiver(PPApplication.nfcStateChangedBroadcastReceiver, intentFilter21);
                    }
                }
            } else
                registerReceiverForRadioSwitchNFCSensor(false, dataWrapper, appContext);
        }
    }

    static void registerObserverForRadioSwitchMobileDataSensor(final boolean register, final DataWrapper dataWrapper, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for observers
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(dataWrapper);
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.mobileDataStateChangedContentObserver != null) {
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.mobileDataStateChangedContentObserver);
                        PPApplication.mobileDataStateChangedContentObserver = null;
                    } catch (Exception e) {
                        PPApplication.mobileDataStateChangedContentObserver = null;
                    }
                }
            }
            if (register) {
                DataWrapper _dataWrapper = dataWrapperWeakRef.get();
                if (_dataWrapper != null) {
                    boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (allowed) {
                        _dataWrapper.fillEventList();
                        allowed = _dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA/*, false*/);
                    }
                    if (allowed) {
                        if (PPApplication.mobileDataStateChangedContentObserver == null) {
                            PPApplication.mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                            appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, PPApplication.mobileDataStateChangedContentObserver);
                        }
                    } else
                        registerObserverForRadioSwitchMobileDataSensor(false, _dataWrapper, appContext);
                }
            }
        });
    }

    static void registerReceiverForRadioSwitchDefaultSIMSensor(final boolean register, final DataWrapper dataWrapper, final Context context) {
        final Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.defaultSIMChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.defaultSIMChangedBroadcastReceiver);
                    PPApplication.defaultSIMChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.defaultSIMChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS/*, false*/);
                allowed = allowed || dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS/*, false*/);
            }
            if (allowed) {
                if (PPApplication.defaultSIMChangedBroadcastReceiver == null) {
                    PPApplication.defaultSIMChangedBroadcastReceiver = new DefaultSIMChangedBroadcastReceiver();
                    IntentFilter intentFilter10 = new IntentFilter();
                    intentFilter10.addAction(SubscriptionManager.ACTION_DEFAULT_SUBSCRIPTION_CHANGED);
                    intentFilter10.addAction(SubscriptionManager.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED);
                    appContext.registerReceiver(PPApplication.defaultSIMChangedBroadcastReceiver, intentFilter10);
                }
            }
            else
                registerReceiverForRadioSwitchDefaultSIMSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForAlarmClockSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.alarmClockBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.alarmClockBroadcastReceiver);
                    PPApplication.alarmClockBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.alarmClockBroadcastReceiver = null;
                }
            }
            if (PPApplication.alarmClockEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.alarmClockEventEndBroadcastReceiver);
                    PPApplication.alarmClockEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.alarmClockEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALARM_CLOCK/*, false*/);
            }
            if (allowed) {
                if (PPApplication.alarmClockBroadcastReceiver == null) {
                    PPApplication.alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
                    IntentFilter intentFilter21 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.alarmClockBroadcastReceiver, intentFilter21, receiverFlags);
                }
                if (PPApplication.alarmClockEventEndBroadcastReceiver == null) {
                    PPApplication.alarmClockEventEndBroadcastReceiver = new AlarmClockEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.alarmClockEventEndBroadcastReceiver, intentFilter22, receiverFlags);
                }
            }
            else
                registerReceiverForAlarmClockSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForNotificationSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.notificationEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.notificationEventEndBroadcastReceiver);
                    PPApplication.notificationEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.notificationEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NOTIFICATION/*, false*/);
            }
            if (allowed) {
                if (PPApplication.notificationEventEndBroadcastReceiver == null) {
                    PPApplication.notificationEventEndBroadcastReceiver = new NotificationEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.notificationEventEndBroadcastReceiver, intentFilter22, receiverFlags);
                }
            }
            else
                registerReceiverForNotificationSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForDeviceBootSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.deviceBootEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.deviceBootEventEndBroadcastReceiver);
                    PPApplication.deviceBootEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.deviceBootEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_DEVICE_BOOT/*, false*/);
            }
            if (allowed) {
                if (PPApplication.deviceBootEventEndBroadcastReceiver == null) {
                    PPApplication.deviceBootEventEndBroadcastReceiver = new DeviceBootEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.deviceBootEventEndBroadcastReceiver, intentFilter22, receiverFlags);
                }
            }
            else
                registerReceiverForDeviceBootSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForPeriodicSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.periodicEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.periodicEventEndBroadcastReceiver);
                    PPApplication.periodicEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.periodicEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_PERIODIC/*, false*/);
            }
            if (allowed) {
                if (PPApplication.periodicEventEndBroadcastReceiver == null) {
                    PPApplication.periodicEventEndBroadcastReceiver = new PeriodicEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.periodicEventEndBroadcastReceiver, intentFilter22, receiverFlags);
                }
            }
            else
                registerReceiverForPeriodicSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForActivatedProfileSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.activatedProfileEventBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.activatedProfileEventBroadcastReceiver);
                    PPApplication.activatedProfileEventBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.activatedProfileEventBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ACTIVATED_PROFILE/*, false*/);
            }
            if (allowed) {
                if (PPApplication.activatedProfileEventBroadcastReceiver == null) {
                    PPApplication.activatedProfileEventBroadcastReceiver = new ActivatedProfileEventBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_ACTIVATED_PROFILE_EVENT_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.activatedProfileEventBroadcastReceiver, intentFilter23, receiverFlags);
                }
            }
            else
                registerReceiverForActivatedProfileSensor(false, dataWrapper, appContext);
        }
    }

    static void unregisterPPPExtenderReceiver(int type, Context context) {
        Context appContext = context.getApplicationContext();

        if (type == PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER) {
            if (PPApplication.pppExtenderForceStopApplicationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderForceStopApplicationBroadcastReceiver);
                    PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = null;
                }
            }

            // send broadcast to Extender for unregister of force stop
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            context.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER) {
            if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderForegroundApplicationBroadcastReceiver);
                    PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = null;
                }
            }

            // send broadcast to Extender for unregister foreground application
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            context.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER) {
            if (PPApplication.pppExtenderSMSBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderSMSBroadcastReceiver);
                    PPApplication.pppExtenderSMSBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderSMSBroadcastReceiver = null;
                }
            }

            // send broadcast to Extender for unregister sms
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            context.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER) {
            if (PPApplication.pppExtenderCallBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderCallBroadcastReceiver);
                    PPApplication.pppExtenderCallBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderCallBroadcastReceiver = null;
                }
            }

            // send broadcast to Extender for unregister call
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            context.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER) {
            // send broadcast to Extender for lock device

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
            context.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
    }

    static void registerPPPExtenderReceiverForSMSCall(boolean register, DataWrapper dataWrapper) {
        Context appContext = dataWrapper.context;
        if (!register) {
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
        }
        if (register) {
            boolean smsAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean callAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            if (smsAllowed || callAllowed) {
                //dataWrapper.fillProfileList(false, false);
                dataWrapper.fillEventList();
            }
            if (smsAllowed)
                smsAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS/*, false*/);
            if (callAllowed)
                callAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL/*, false*/);

//            Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "smsExists="+smsExists);
//            Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "smsAllowed="+smsAllowed);
//            Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "callExists="+callExists);
//            Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "callAllowed="+callAllowed);

            if (smsAllowed || callAllowed) {
                if (smsAllowed) {
                    if (PPApplication.pppExtenderSMSBroadcastReceiver == null) {
                        PPApplication.pppExtenderSMSBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register sms
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);

//                    Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "SMS");
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);

                if (callAllowed) {
                    if (PPApplication.pppExtenderCallBroadcastReceiver == null) {
                        PPApplication.pppExtenderCallBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register call
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);

//                    Log.e("PhoneProfilesService.registerPPPExtenderReceiverForSMSCall", "Call");
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
            }
            else {
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
            }
        }
    }

    static void registerPPPExtenderReceiver(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER, appContext);
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER, appContext);
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
            unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER, appContext);
        }
        if (register) {
            boolean forceStopAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean lockDeviceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, null, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean applicationsAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean orientationAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean smsAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean callAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);

            if (forceStopAllowed || applicationsAllowed || orientationAllowed || smsAllowed || callAllowed || lockDeviceAllowed) {
                dataWrapper.fillProfileList(false, false);
                dataWrapper.fillEventList();

                if (forceStopAllowed)
                    forceStopAllowed = dataWrapper.profileTypeExists(DatabaseHandler.PTYPE_FORCE_STOP/*, false*/);

                if (lockDeviceAllowed)
                    lockDeviceAllowed = dataWrapper.profileTypeExists(DatabaseHandler.PTYPE_LOCK_DEVICE/*, false*/);

                if (applicationsAllowed)
                    applicationsAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION/*, false*/);

                if (orientationAllowed)
                    orientationAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);

                if (smsAllowed)
                    smsAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS/*, false*/);

                if (callAllowed)
                    callAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL/*, false*/);

                if (forceStopAllowed) {
                    if (PPApplication.pppExtenderForceStopApplicationBroadcastReceiver == null) {
                        PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register force stop applications
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER, appContext);

                if (lockDeviceAllowed) {
                    // send broadcast to Extender for register lock device

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER, appContext);

                if ((applicationsAllowed) || (orientationAllowed)) {
                    if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver == null) {
                        PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderForegroundApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register foreground application
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER, appContext);

                if (smsAllowed) {
                    if (PPApplication.pppExtenderSMSBroadcastReceiver == null) {
                        PPApplication.pppExtenderSMSBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register sms
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);

                if (callAllowed) {
                    if (PPApplication.pppExtenderCallBroadcastReceiver == null) {
                        PPApplication.pppExtenderCallBroadcastReceiver = new PPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_EXPORTED;
                        appContext.registerReceiver(PPApplication.pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null, receiverFlags);
                    }

                    // send broadcast to Extender for register call
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, StringConstants.PHONE_PROFILES_PLUS);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_REGISTER);
                    appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
            }
            else {
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER, appContext);
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER, appContext);
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER, appContext);
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER, appContext);
                unregisterPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER, appContext);
            }
        }
    }

    static void registerLocationModeChangedBroadcastReceiver(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.locationModeChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.locationModeChangedBroadcastReceiver);
                    PPApplication.locationModeChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.locationModeChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_GPS, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                boolean eventsExists = false;
                if (allowed) {
                    dataWrapper.fillEventList();
                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_GPS/*, false*/);
                }
                if (!eventsExists) {
                    allowed = false;
                    // location scanner is enabled
                    //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                        if (allowed) {
                            dataWrapper.fillEventList();
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                        }
                        if (!eventsExists)
                            allowed = false;
                    }
                }

                if (allowed) {
                    if (PPApplication.locationModeChangedBroadcastReceiver == null) {
                        PPApplication.locationModeChangedBroadcastReceiver = new LocationModeChangedBroadcastReceiver();
                        IntentFilter intentFilter18 = new IntentFilter();
                        intentFilter18.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                        intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
                        appContext.registerReceiver(PPApplication.locationModeChangedBroadcastReceiver, intentFilter18);
                    }
                } else
                    registerLocationModeChangedBroadcastReceiver(false, dataWrapper, appContext);
            } else
                registerLocationModeChangedBroadcastReceiver(false, dataWrapper, appContext);
        }
    }

    /*
    static void registerBluetoothStateChangedBroadcastReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister, Context context) {
        Context appContext = context.getApplicationContext();
        if (!forceRegister && PPApplication.bluetoothForceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver);
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                boolean allowed;
                boolean eventsExists = false;
                if (PPApplication.bluetoothForceRegister)
                    allowed = true;
                else {
                    allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (allowed) {
                        dataWrapper.fillEventList();
                        eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH);
                    }
                    if (!eventsExists) {
                        allowed = false;
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                            // start only for screen On
                            allowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED);
                            if (allowed) {
                                dataWrapper.fillEventList();
                                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED);
                                if (!eventsExists)
                                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY);
                            }
                            if (!eventsExists)
                                allowed = false;
                        }
                    }
                }
                if (allowed) {
                    if (PPApplication.bluetoothStateChangedBroadcastReceiver == null) {
                        PPApplication.bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
                        IntentFilter intentFilter15 = new IntentFilter();
                        intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                        appContext.registerReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver, intentFilter15);
                    }
                } else
                    registerBluetoothStateChangedBroadcastReceiver(false, dataWrapper, forceRegister, appContext);
            } else
                registerBluetoothStateChangedBroadcastReceiver(false, dataWrapper, forceRegister, appContext);
        }
    }
    */

    static void registerBluetoothScannerReceivers(boolean register, DataWrapper dataWrapper, boolean forceRegister, Context context) {
        Context appContext = context.getApplicationContext();
        if (!forceRegister && PPApplication.bluetoothForceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothScanReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothScanReceiver);
                    PPApplication.bluetoothScanReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothScanReceiver = null;
                }
            }
            if (PPApplication.bluetoothLEScanReceiver != null) {
                try {
                    LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.bluetoothLEScanReceiver);
                    PPApplication.bluetoothLEScanReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothLEScanReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventBluetoothEnableScanning || PPApplication.bluetoothForceRegister) {
                boolean allowed = false;
                boolean eventsExists = false;
                if (PPApplication.bluetoothForceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                        if (allowed) {
                            dataWrapper.fillEventList();
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
                        }
                        if (!eventsExists)
                            allowed = false;
                    }
                }
                if (allowed) {
                    if (PPApplication.bluetoothLEScanReceiver == null) {
                        PPApplication.bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();
                        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.bluetoothLEScanReceiver,
                                new IntentFilter(PhoneProfilesService.ACTION_BLUETOOTHLE_SCAN_BROADCAST_RECEIVER));
                    }
                    if (PPApplication.bluetoothScanReceiver == null) {
                        PPApplication.bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_FOUND);
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        appContext.registerReceiver(PPApplication.bluetoothScanReceiver, intentFilter14);
                    }
                } else
                    registerBluetoothScannerReceivers(false, dataWrapper, forceRegister, appContext);
            } else
                registerBluetoothScannerReceivers(false, dataWrapper, forceRegister, appContext);
        }
    }

    static void registerWifiAPStateChangeBroadcastReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister, Context context) {
        Context appContext = context.getApplicationContext();
        if (!forceRegister && PPApplication.wifiSSIDForceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiAPStateChangeBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiAPStateChangeBroadcastReceiver);
                    PPApplication.wifiAPStateChangeBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.wifiAPStateChangeBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventWifiEnableScanning || PPApplication.wifiSSIDForceRegister) {
                boolean allowed = false;
                boolean eventsExists = false;
                if (PPApplication.wifiSSIDForceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                        allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                        if (allowed) {
                            dataWrapper.fillEventList();
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                        }
                        if (!eventsExists)
                            allowed = false;
                    }
                }
                if (allowed) {
                    if (PPApplication.wifiAPStateChangeBroadcastReceiver == null) {
                        PPApplication.wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
                        IntentFilter intentFilter17 = new IntentFilter();
                        intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                        appContext.registerReceiver(PPApplication.wifiAPStateChangeBroadcastReceiver, intentFilter17);
                    }
                }
                else
                    registerWifiAPStateChangeBroadcastReceiver(false, dataWrapper, forceRegister, appContext);
            }
            else
                registerWifiAPStateChangeBroadcastReceiver(false, dataWrapper, forceRegister, appContext);
        }
    }

    static void registerWifiScannerReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister, Context context) {
        Context appContext = context.getApplicationContext();
        if (!forceRegister && PPApplication.wifiSSIDForceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiScanReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiScanReceiver);
                    PPApplication.wifiScanReceiver = null;
                } catch (Exception e) {
                    PPApplication.wifiScanReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventWifiEnableScanning || PPApplication.wifiSSIDForceRegister) {
                boolean allowed = false;
                boolean eventsExists = false;
                if (PPApplication.wifiSSIDForceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                        if (allowed) {
                            dataWrapper.fillEventList();
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                        }
                        if (!eventsExists)
                            allowed = false;
                    }
                }
                if (allowed) {
                    //}
                    if (PPApplication.wifiScanReceiver == null) {
                        PPApplication.wifiScanReceiver = new WifiScanBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter();
                        intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        appContext.registerReceiver(PPApplication.wifiScanReceiver, intentFilter4);
                    }
                } else
                    registerWifiScannerReceiver(false, dataWrapper, forceRegister, appContext);
            } else
                registerWifiScannerReceiver(false, dataWrapper, forceRegister, appContext);
        }
    }

    static void registerReceiverForTimeSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.eventTimeBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventTimeBroadcastReceiver);
                    PPApplication.eventTimeBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventTimeBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_TIME/*, false*/);
            }
            if (allowed) {
                if (PPApplication.eventTimeBroadcastReceiver == null) {
                    PPApplication.eventTimeBroadcastReceiver = new EventTimeBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.eventTimeBroadcastReceiver, intentFilter23, receiverFlags);
                }
            }
            else
                registerReceiverForTimeSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForNFCSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.nfcEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.nfcEventEndBroadcastReceiver);
                    PPApplication.nfcEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.nfcEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NFC/*, false*/);
            }
            if (allowed) {
                if (PPApplication.nfcEventEndBroadcastReceiver == null) {
                    PPApplication.nfcEventEndBroadcastReceiver = new NFCEventEndBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.nfcEventEndBroadcastReceiver, intentFilter23, receiverFlags);
                }
            }
            else
                registerReceiverForNFCSensor(false, dataWrapper, appContext);
        }
    }

    static void registerReceiverForCallSensor(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.missedCallEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.missedCallEventEndBroadcastReceiver);
                    PPApplication.missedCallEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.missedCallEventEndBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                dataWrapper.fillEventList();
                allowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL/*, false*/);
            }
            if (allowed) {
                if (PPApplication.missedCallEventEndBroadcastReceiver == null) {
                    PPApplication.missedCallEventEndBroadcastReceiver = new MissedCallEventEndBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(PPApplication.missedCallEventEndBroadcastReceiver, intentFilter23, receiverFlags);
                }

//                Log.e("PhoneProfilesService.registerReceiverForCallSensor", "xxx");
            }
            else
                registerReceiverForCallSensor(false, dataWrapper, appContext);
        }
    }

    static void registerVPNCallback(final boolean register, final DataWrapper dataWrapper, final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for callbacks
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(dataWrapper);
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.vpnConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.vpnConnectionCallback);
                        }
                        PPApplication.vpnConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.vpnConnectionCallback = null;
                    }
                }
            }
            if (register) {
                DataWrapper _dataWrapper = dataWrapperWeakRef.get();
                if (_dataWrapper != null) {
                    boolean allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (allowed) {
                        _dataWrapper.fillEventList();
                        allowed = _dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_VPN/*, false*/);
                    }
                    if (allowed) {
                        if (PPApplication.vpnConnectionCallback == null) {
                            try {
                                ConnectivityManager connectivityManager =
                                        (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                                if (connectivityManager != null) {
                                    NetworkRequest networkRequest = new NetworkRequest.Builder()
                                            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                                            .build();

                                    PPApplication.vpnConnectionCallback = new VPNNetworkCallback(appContext);
                                    connectivityManager.registerNetworkCallback(networkRequest, PPApplication.vpnConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                                }
                            } catch (Exception e) {
                                PPApplication.vpnConnectionCallback = null;
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    } else
                        registerVPNCallback(false, _dataWrapper, appContext);
                }
            }
        });
    }

    static void registerLocationScannerReceiver(boolean register, DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (!register) {
            if (PPApplication.locationScannerSwitchGPSBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.locationScannerSwitchGPSBroadcastReceiver);
                    PPApplication.locationScannerSwitchGPSBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.locationScannerSwitchGPSBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                boolean allowed = false;
                boolean eventsExists = false;
                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                    // start only for screen On
                    allowed = EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (allowed) {
                        dataWrapper.fillEventList();
                        eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                    }
                    if (!eventsExists)
                        allowed = false;
                }
                if (allowed) {
                    if (PPApplication.locationScannerSwitchGPSBroadcastReceiver == null) {
                        PPApplication.locationScannerSwitchGPSBroadcastReceiver = new LocationScannerSwitchGPSBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_NOT_EXPORTED;
                        appContext.registerReceiver(PPApplication.locationScannerSwitchGPSBroadcastReceiver, intentFilter4, receiverFlags);
                    }
                } else
                    registerLocationScannerReceiver(false, dataWrapper, appContext);
            } else
                registerLocationScannerReceiver(false, dataWrapper, appContext);
        }
    }

    static void cancelPeriodicScanningWorker(/*boolean useHandler*/) {
        PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false/*, useHandler*/);
        PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false/*, useHandler*/);
    }

    // this is called from ThreadHanlder
    static void schedulePeriodicScanningWorker() {
        //final Context appContext = getApplicationContext();

        //if (schedule) {
        if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                eventAllowed = true;
            }
            if (eventAllowed) {
                //PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                //PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
                PPApplicationStatic._cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                PPApplicationStatic._cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
                //PPApplication.sleep(5000);
                OneTimeWorkRequest periodicEventsHandlerWorker =
                        new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                                .addTag(PeriodicEventsHandlerWorker.WORK_TAG_SHORT)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                        //if (PPApplicationStatic.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesServiceStatic.schedulePeriodicScanningWorker", "xxx");
                        workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            } else
                cancelPeriodicScanningWorker(/*false*/);
        } else
            cancelPeriodicScanningWorker(/*false*/);
        //}
        //else
        //    cancelPeriodicScanningWorker();
    }

    static void cancelWifiWorker(final Context context, boolean forSchedule, boolean useHandler) {
        if ((!forSchedule) ||
                (WifiScanWorker.isWorkScheduled(false) || WifiScanWorker.isWorkScheduled(true))) {
            WifiScanWorker.cancelWork(context, useHandler/*, null*/);
        }

        WifiScanWorker.setScanRequest(context, false);
        WifiScanWorker.setWaitForResults(context, false);
        WifiScanWorker.setWifiEnabledForScan(context, false);
    }

    // this is called from ThreadHanlder
    static void scheduleWifiWorker(final DataWrapper dataWrapper) {
        final Context appContext = dataWrapper.context;

        if (/*!forceStart &&*/ PPApplication.wifiSSIDForceRegister)
            return;

        //if (schedule) {
        if (ApplicationPreferences.applicationEventWifiEnableScanning) {
            boolean eventAllowed = false;
            boolean eventsExists = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    dataWrapper.fillEventList();
                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                }
                if (!eventsExists)
                    eventAllowed = false;
            }
            if (eventAllowed) {
                //if (!(WifiScanWorker.isWorkScheduled(false) || WifiScanWorker.isWorkScheduled(true))) {
                //    WifiScanWorker.scheduleWork(appContext, true);
                //} else {
                //    if (rescan) {
//                        PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.scheduleWifiWorker", "shortInterval=true");
                        WifiScanWorker.scheduleWork(appContext, true);
                //    }
                //}
            } else
                cancelWifiWorker(appContext, true, false);
        } else
            cancelWifiWorker(appContext, true, false);
        //}
        //else
        //    cancelWifiWorker(appContext, handler);
    }

    static void cancelBluetoothWorker(final Context context, boolean forSchedule, boolean useHandler) {
        if ((!forSchedule) ||
                (BluetoothScanWorker.isWorkScheduled(false) || BluetoothScanWorker.isWorkScheduled(true))) {
            BluetoothScanWorker.cancelWork(context, useHandler);
        }

        BluetoothScanWorker.setScanRequest(context, false);
        BluetoothScanWorker.setLEScanRequest(context, false);
        BluetoothScanWorker.setWaitForResults(context, false);
        BluetoothScanWorker.setWaitForLEResults(context, false);
        BluetoothScanWorker.setBluetoothEnabledForScan(context, false);
        BluetoothScanWorker.setScanKilled(context, false);
    }

    // this is called from ThreadHanlder
    static void scheduleBluetoothWorker(final DataWrapper dataWrapper) {
        final Context appContext = dataWrapper.context;

        if (/*!forceStart &&*/ PPApplication.bluetoothForceRegister)
            return;

        //if (schedule) {
        if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
            boolean eventAllowed = false;
            boolean eventsExists = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    dataWrapper.fillEventList();
                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
                }
                if (!eventsExists)
                    eventAllowed = false;
            }
            if (eventAllowed) {
                /*if (BluetoothScanWorker.isWorkScheduled(false) || BluetoothScanWorker.isWorkScheduled(true)) {
                    BluetoothScanWorker.cancelWork(appContext, true);
                }*/
                BluetoothScanWorker.scheduleWork(appContext, true);
            } else
                cancelBluetoothWorker(appContext, true, false);
        } else
            cancelBluetoothWorker(appContext, true, false);
        //}
        //else
        //    cancelBluetoothWorker(appContext, handler);
    }

    static void cancelSearchCalendarEventsWorker(boolean forSchedule, boolean useHandler) {
        if ((!forSchedule) ||
                (SearchCalendarEventsWorker.isWorkScheduled(false) || SearchCalendarEventsWorker.isWorkScheduled(true))) {
            SearchCalendarEventsWorker.cancelWork(useHandler);
        }
    }

    // this is called from ThreadHanlder
    static void scheduleSearchCalendarEventsWorker(final DataWrapper dataWrapper) {
        final Context appContext = dataWrapper.context;

        //if (schedule) {
        boolean eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                PreferenceAllowed.PREFERENCE_ALLOWED;
        if (eventAllowed) {
            dataWrapper.fillEventList();
            eventAllowed = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALENDAR/*, false*/);
        }
        if (eventAllowed) {
            //if (!(SearchCalendarEventsWorker.isWorkScheduled(false) || SearchCalendarEventsWorker.isWorkScheduled(true))) {
                //if (rescan)
                SearchCalendarEventsWorker.scheduleWork(true);
            //}
        } else
            cancelSearchCalendarEventsWorker(true, false);
        //}
        //else
        //    cancelSearchCalendarEventsWorker(appContext, handler);
    }

    static void startLocationScanner(boolean start,
                                     @SuppressWarnings("SameParameterValue") boolean stop,
                                     DataWrapper dataWrapper, boolean forScreenOn, Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.startLocationScanner", "PPApplication.locationScannerMutex");
        synchronized (PPApplication.locationScannerMutex) {
            Context appContext = context.getApplicationContext();
            if (stop) {
                if (PPApplication.locationScanner != null) {
                    stopLocationScanner();
                }
            }
            if (start) {
                if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                    boolean eventAllowed = false;
                    boolean eventsExists = false;
                    boolean applicationEventLocationScanOnlyWhenScreenIsOn = ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn;
                    if ((PPApplication.isScreenOn) || (!applicationEventLocationScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                        if (eventAllowed) {
                            dataWrapper.fillEventList();
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                        }
                        if (!eventsExists)
                            eventAllowed = false;
                    }
                    if (eventAllowed) {
                        if (PPApplication.locationScanner == null) {
                            startLocationScanner(forScreenOn && PPApplication.isScreenOn &&
                                    applicationEventLocationScanOnlyWhenScreenIsOn, appContext);
                        }
                    } else
                        startLocationScanner(false, true, dataWrapper, forScreenOn, appContext);
                } else
                    startLocationScanner(false, true, dataWrapper, forScreenOn, appContext);
            }
        }
    }

    static void startMobileCellsScanner(final boolean start, final boolean stop,
                                         final DataWrapper dataWrapper,
                                         final boolean forceStart, final boolean rescan,
                                         final Context context) {
        final Context appContext = context.getApplicationContext();

        // keep this: it is required to use handlerThreadBroadcast for cells listener
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(dataWrapper);
        __handler.post(() -> {
//            PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.startMobileCellsScanner", "PPApplication.mobileCellsScannerMutex");
            synchronized (PPApplication.mobileCellsScannerMutex) {
                if (!forceStart && (PPApplication.mobileCellsForceStart || PPApplication.mobileCellsRegistraitonForceStart))
                    return;

                if (stop) {
                    if (PPApplication.mobileCellsScanner != null) {
                        //stopMobileCellsScanner();
                        PPApplication.mobileCellsScanner.disconnect();
                        PPApplication.mobileCellsScanner = null;
                    }
                }

                if (start) {
                    DataWrapper _dataWrapper = dataWrapperWeakRef.get();
                    if (_dataWrapper != null) {
                        //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || MobileCellsScanner.forceStart) {
                        if (ApplicationPreferences.applicationEventMobileCellEnableScanning ||
                                PPApplication.mobileCellsForceStart || PPApplication.mobileCellsRegistraitonForceStart) {
//                        PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startMobileCellsScanner", "******** ### *******");
                            boolean eventAllowed = false;
                            boolean eventsExists = false;
                            if (PPApplication.mobileCellsForceStart || PPApplication.mobileCellsRegistraitonForceStart)
                                eventAllowed = true;
                            else {
                                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn)) {
                                    // start only for screen On
                                    eventAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                                            PreferenceAllowed.PREFERENCE_ALLOWED);
                                    if (eventAllowed) {
                                        _dataWrapper.fillEventList();
                                        eventsExists = _dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_MOBILE_CELLS/*, false*/);
                                    }
                                    if (!eventsExists)
                                        eventAllowed = false;
                                }
                            }
                            if (eventAllowed) {
//                            Log.e("PhoneProfilesService.startMobileCellsScanner", "***************");
                                if (PPApplication.mobileCellsScanner == null) {
//                                PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startMobileCellsScanner", "******** ### ******* called startMobileCellsScanner()");
                                    //startMobileCellsScanner();
                                    PPApplication.mobileCellsScanner = new MobileCellsScanner(appContext);
                                    PPApplication.mobileCellsScanner.connect();
                                } else {
                                    if (rescan) {
                                        PPApplication.mobileCellsScanner.rescanMobileCells();
                                    }
                                }
                            } else
                                startMobileCellsScanner(false, true, _dataWrapper, forceStart, rescan, appContext);
                        } else
                            startMobileCellsScanner(false, true, _dataWrapper, forceStart, rescan, appContext);
                    }
                }
            }
        });
    }

    static void startOrientationScanner(boolean start, boolean stop,
                                         DataWrapper dataWrapper/*, boolean forceStart*/, Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.startOrientationScanner", "PPApplication.orientationScannerMutex");
        synchronized (PPApplication.orientationScannerMutex) {
            Context appContext = context.getApplicationContext();
            //if (!forceStart && EventsPrefsFragment.forceStart)
            //    return;
            if (stop) {
                if (isOrientationScannerStarted()) {
                    stopOrientationScanner();
                }
            }
            if (start) {
                //PPApplicationStatic.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "START");
                if (ApplicationPreferences.applicationEventOrientationEnableScanning /*||
                        EventsPrefsFragment.forceStart*/) {
//                    PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startOrientationScanner", "******** ### *******");
                    boolean eventAllowed = false;
                    boolean eventsExists = false;
                    /*if (EventsPrefsFragment.forceStart)
                        eventAllowed = true;
                    else*/ {
                        if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)) {
                            // start only for screen On
                            eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                            if (eventAllowed) {
                                dataWrapper.fillEventList();
                                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
                            }
                            if (!eventsExists)
                                eventAllowed = false;
                        }
                    }
                    if (eventAllowed) {
//                        Log.e("PhoneProfilesService.startOrientationScanner", "***************");
                        if (!isOrientationScannerStarted()) {
//                            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startOrientationScanner", "******** ### ******* called startOrientationScanner()");
                            startOrientationScanner(appContext);
//                            PPApplicationStatic.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "START");
                        }
//                        else
//                            PPApplicationStatic.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "started");
                    } else
                        startOrientationScanner(false, true, dataWrapper/*, forceStart*/, appContext);
                } else
                    startOrientationScanner(false, true, dataWrapper/*, forceStart*/, appContext);
            }
        }
    }

    static void startTwilightScanner(boolean start, boolean stop, DataWrapper dataWrapper) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.startTwilightScanner", "PPApplication.twilightScannerMutex");
        synchronized (PPApplication.twilightScannerMutex) {
            //Context appContext = getApplicationContext();
            if (stop) {
                if (PPApplication.twilightScanner != null) {
                    stopTwilightScanner();
                }
            }
            if (start) {
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_TIME_TWILIGHT/*, false*/);
                if (eventsExists) {
                    if (PPApplication.twilightScanner == null) {
                        startTwilightScanner(dataWrapper.context.getApplicationContext());
                    }
                } else {
                    startTwilightScanner(false, true, dataWrapper);
                }
            }
        }
    }

    static void startNotificationScanner(boolean start, boolean stop,
                                          DataWrapper dataWrapper, Context context) {
        Context appContext = context.getApplicationContext();
        if (stop) {
            if (PPApplication.notificationScannerRunning) {
                PPApplication.notificationScannerRunning = false;
            }
        }
        if (start) {
            if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                boolean eventAllowed = false;
                boolean eventsExists = false;
                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn)) {
                    // start only for screen On
                    eventAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        dataWrapper.fillEventList();
                        eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NOTIFICATION/*, false*/);
                    }
                    if (!eventsExists)
                        eventAllowed = false;
                }
                if (eventAllowed) {
                    if (!PPApplication.notificationScannerRunning) {
                        PPApplication.notificationScannerRunning = true;
                    }
                } else
                    startNotificationScanner(false, true, dataWrapper, appContext);
            } else
                startNotificationScanner(false, true, dataWrapper, appContext);
        }
    }

    static void registerEventsReceiversAndWorkers(boolean fromCommand, Context context) {
        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = context.getApplicationContext();

        // get actual battery status
        BatteryLevelChangedBroadcastReceiver.initialize(appContext);

        registerContactsContentObservers(true, appContext);

        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        // required for battery sensor
        registerBatteryLevelChangedReceiver(true, dataWrapper, appContext);
        registerBatteryChargingChangedReceiver(true, dataWrapper, appContext);

        // required for accessories sensor
        registerReceiverForAccessoriesSensor(true, dataWrapper, appContext);

        // required for sms/mms sensor
        registerReceiverForSMSSensor(true, dataWrapper, appContext);

        // required for calendar sensor
        registerReceiverForCalendarSensor(true, dataWrapper, appContext);

        // required for radio switch sensor
        registerObserverForRadioSwitchMobileDataSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchDefaultSIMSensor(true, dataWrapper, appContext);

        // required for alarm clock sensor
        registerReceiverForAlarmClockSensor(true, dataWrapper, appContext);

        // required for device boot sensor
        registerReceiverForDeviceBootSensor(true, dataWrapper, appContext);

        // required for periodic sensor
        registerReceiverForPeriodicSensor(true, dataWrapper, appContext);

        // required for location and radio switch sensor
        registerLocationModeChangedBroadcastReceiver(true, dataWrapper, appContext);

        /*
        // required for bluetooth connection type = (dis)connected +
        // radio switch event +
        // bluetooth scanner
        registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false, appContext);
        */

        // required for bluetooth connection type = (dis)connected +
        // bluetooth scanner
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);

        // required for bluetooth scanner
        registerBluetoothScannerReceivers(true, dataWrapper, false, appContext);

        // required for wifi scanner
        registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false, appContext);

        // required for all scanner events (wifi, bluetooth, location, mobile cells, device orientation) +
        // battery event
        // moved to all the time
        //registerPowerSaveModeReceiver(true, dataWrapper);

        /*
        // required for Connect to SSID profile preference +
        // wifi connection type = (dis)connected +
        // radio switch event +
        // wifi scanner
        registerWifiStateChangedBroadcastReceiver(true, true, false);
        */

        // required for Connect to SSID profile preference +
        // required for wifi connection type = (dis)connected event +
        // wifi scanner
        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);

        // required for wifi scanner
        registerWifiScannerReceiver(true, dataWrapper, false, appContext);

        // required for notification sensor
        registerReceiverForNotificationSensor(true, dataWrapper, appContext);

        // required for VPN sensor
        registerVPNCallback(true, dataWrapper, appContext);

        //SMSBroadcastReceiver.registerSMSContentObserver(appContext);
        //SMSBroadcastReceiver.registerMMSContentObserver(appContext);

        // ----------------------------------------------

        /*
        if (mSipManager != null) {
            mSipManager = SipManager.newInstance(appContext);

            SipProfile sipProfile = null;
            try {
                SipProfile.Builder builder = new SipProfile.Builder("henrichg", "domain");
                builder.setPassword("password");
                sipProfile = builder.build();

                Intent intent = new Intent();
                intent.setAction(PPApplication.PACKAGE_NAME + ".INCOMING_SIPCALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, Intent.FILL_IN_DATA);
                mSipManager.open(sipProfile, pendingIntent, null);

            } catch (Exception ignored) {
            }
        }
        */

        // register receiver for time sensor
        registerReceiverForTimeSensor(true, dataWrapper, appContext);

        // register receiver for nfc sensor
        registerReceiverForNFCSensor(true, dataWrapper, appContext);

        // register receiver for call event
        registerReceiverForCallSensor(true, dataWrapper, appContext);

        // register receiver for Location scanner
        registerLocationScannerReceiver(true, dataWrapper, appContext);

        // required for orientation event
        //registerReceiverForOrientationSensor(true, dataWrapper);

        // required for calendar event
        registerReceiverForActivatedProfileSensor(true, dataWrapper, appContext);

        WifiScanWorker.initialize(appContext, !fromCommand);
        BluetoothScanWorker.initialize(appContext, !fromCommand);

        startLocationScanner(true, true, dataWrapper, false, appContext);
        startMobileCellsScanner(true, true, dataWrapper, false, false, appContext);
        startOrientationScanner(true, true, dataWrapper/*, false*/, appContext);
        startTwilightScanner(true, true, dataWrapper);
        startNotificationScanner(true, true, dataWrapper, appContext);

        schedulePeriodicScanningWorker();
//        PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.registerEventsReceiversAndWorkers", "xxx");
        scheduleWifiWorker(/*true,*/  dataWrapper/*, false, false, false, true*/);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true, */dataWrapper/*, true*/);
        //scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);

        AvoidRescheduleReceiverWorker.enqueueWork();

        dataWrapper.invalidateDataWrapper();
    }

    static void unregisterEventsReceiversAndWorkers(boolean useHandler, Context context) {
        Context appContext = context.getApplicationContext();

        registerContactsContentObservers(false, appContext);

        registerBatteryLevelChangedReceiver(false, null, appContext);
        registerBatteryChargingChangedReceiver(false, null, appContext);
        registerReceiverForAccessoriesSensor(false, null, appContext);
        registerReceiverForSMSSensor(false, null, appContext);
        registerReceiverForCalendarSensor(false, null, appContext);
        registerObserverForRadioSwitchMobileDataSensor(false, null, appContext);
        registerReceiverForRadioSwitchNFCSensor(false, null, appContext);
        registerReceiverForRadioSwitchAirplaneModeSensor(false, null, appContext);
        registerReceiverForRadioSwitchDefaultSIMSensor(false, null, appContext);
        registerReceiverForAlarmClockSensor(false, null, appContext);
        registerReceiverForDeviceBootSensor(false, null, appContext);
        registerReceiverForPeriodicSensor(false, null, appContext);
        registerLocationModeChangedBroadcastReceiver(false, null, appContext);
        //registerBluetoothStateChangedBroadcastReceiver(false, null, false, appContext);
        //registerBluetoothConnectionBroadcastReceiver(false, true, false, false);
        registerBluetoothScannerReceivers(false, null, false, appContext);
        registerWifiAPStateChangeBroadcastReceiver(false, null, false, appContext);
        //registerPowerSaveModeReceiver(false, null);
        //registerWifiStateChangedBroadcastReceiver(false, false, false);
        //registerWifiConnectionBroadcastReceiver(false, null, false);
        registerWifiScannerReceiver(false, null, false, appContext);
        registerReceiverForTimeSensor(false, null, appContext);
        registerReceiverForNFCSensor(false, null, appContext);
        registerReceiverForCallSensor(false, null, appContext);
        registerLocationScannerReceiver(false,  null, appContext);
        registerReceiverForNotificationSensor(false, null, appContext);
        //registerReceiverForOrientationSensor(false, null);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        registerReceiverForActivatedProfileSensor(false, null, appContext);
        registerVPNCallback(false, null, appContext);

        startLocationScanner(false, true, null, false, appContext);
        startMobileCellsScanner(false, true, null, false, false, appContext);
        startOrientationScanner(false, true, null/*, false*/, appContext);
        startTwilightScanner(false, true, null);
        startNotificationScanner(false, true, null, appContext);

        cancelPeriodicScanningWorker(/*useHandler*/);
        cancelWifiWorker(appContext, false, useHandler);
        cancelBluetoothWorker(appContext, false, useHandler);
        //cancelGeofenceWorker(false);
        cancelSearchCalendarEventsWorker(false, useHandler);

    }

    static void reregisterEventsReceiversAndWorkers(Context context) {
        Context appContext = context.getApplicationContext();

        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        //final Context appContext = getApplicationContext();

        registerContactsContentObservers(true, appContext);

        registerBatteryLevelChangedReceiver(true, dataWrapper, appContext);
        registerBatteryChargingChangedReceiver(true, dataWrapper, appContext);
        registerReceiverForAccessoriesSensor(true, dataWrapper, appContext);
        registerReceiverForSMSSensor(true, dataWrapper, appContext);
        registerReceiverForCalendarSensor(true, dataWrapper, appContext);
        registerObserverForRadioSwitchMobileDataSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper, appContext);
        registerReceiverForRadioSwitchDefaultSIMSensor(true, dataWrapper, appContext);
        registerReceiverForAlarmClockSensor(true, dataWrapper, appContext);
        registerReceiverForDeviceBootSensor(true, dataWrapper, appContext);
        registerReceiverForPeriodicSensor(true, dataWrapper, appContext);
        registerLocationModeChangedBroadcastReceiver(true, dataWrapper, appContext);
        //registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false, appContext);
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);
        registerBluetoothScannerReceivers(true, dataWrapper, false, appContext);
        registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false, appContext);
        //registerPowerSaveModeReceiver(true, dataWrapper);
        //registerWifiStateChangedBroadcastReceiver(true, true, false);
        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
        registerWifiScannerReceiver(true, dataWrapper, false, appContext);
        registerReceiverForTimeSensor(true, dataWrapper, appContext);
        registerReceiverForNFCSensor(true, dataWrapper, appContext);
        registerReceiverForCallSensor(true, dataWrapper, appContext);
        registerLocationScannerReceiver(true, dataWrapper, appContext);
        //registerReceiverForOrientationSensor(true, dataWrapper);
        registerReceiverForNotificationSensor(true,dataWrapper, appContext);
        registerReceiverForActivatedProfileSensor(true, dataWrapper, appContext);
        registerVPNCallback(true, dataWrapper, appContext);

        schedulePeriodicScanningWorker();
//        PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.reregisterEventsReceiversAndWorkers", "xxx");
        scheduleWifiWorker(/*true,*/  dataWrapper/*, false, false, false, true*/);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true,*/ dataWrapper /*, true*/);

        startLocationScanner(true, true, dataWrapper, false, appContext);
        //scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);

        startMobileCellsScanner(true, true, dataWrapper, false, false, appContext);
        startOrientationScanner(true, true, dataWrapper/*, false*/, appContext);
        startTwilightScanner(true, true, dataWrapper);
        startNotificationScanner(true, true, dataWrapper, appContext);

        AvoidRescheduleReceiverWorker.enqueueWork();

        dataWrapper.invalidateDataWrapper();
    }


    static void doCommand(Intent _intent, Context context) {
        if (_intent != null) {

            final Context appContext = context.getApplicationContext();

            final WeakReference<Intent> intentWeakRef = new WeakReference<>(_intent);
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.doCommand (1)");

                Intent intent = intentWeakRef.get();
                if (intent == null)
                    return;

                Log.e("PhoneProfilesServiceStatic.doCommand", "xxxxxxxxxxxxxxx");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PhoneProfilesService_doCommand);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                        PPApplicationStatic.logE("[IN_EXECUTOR]  PhoneProfilesService.doCommand", "--- START");

                    PhoneProfilesService ppService = PhoneProfilesService.getInstance();

                    if (ppService != null) {
                        boolean disableNotUsedScanners = intent.getBooleanExtra(PhoneProfilesService.EXTRA_DISABLE_NOT_USED_SCANNERS, false);

                        /*if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                            // not needed, is already called in start of onStartCommand
                            //showProfileNotification();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                            clearProfileNotification();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                            // not needed, is already called in start of onStartCommand
                            //showProfileNotification();
                        }
                        else*/
                        /*if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                            //boolean isScreenOn;
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

                            boolean secureKeyguard;
                            //if (PPApplication.keyguardManager == null)
                            //    PPApplication.keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (PPApplication.keyguardManager != null) {
                                secureKeyguard = PPApplication.keyguardManager.isKeyguardSecure();
                                if (!secureKeyguard) {

                                    if (PPApplication.isScreenOn) {

                                        if (ApplicationPreferences.prefLockScreenDisabled) {
                                            ppService.reenableKeyguard();
                                            ppService.disableKeyguard();
                                        } else {
                                            ppService.reenableKeyguard();
                                        }
                                    }
                                }
                            }
                        }*/
                        /*
                        else
                        if (intent.getBooleanExtra(EXTRA_START_LOCATION_UPDATES, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_START_LOCATION_UPDATES");
                            //synchronized (PPApplication.locationScannerMutex) {
                                if (PhoneProfilesService.getLocationScanner() != null) {
                                    LocationScanner.useGPS = true;
                                    PhoneProfilesService.getLocationScanner().startLocationUpdates();
                                }
                            //}
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_STOP_LOCATION_UPDATES, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_STOP_LOCATION_UPDATES");
                            //synchronized (PPApplication.locationScannerMutex) {
                            if (PhoneProfilesService.getLocationScanner() != null)
                                PhoneProfilesService.getLocationScanner().stopLocationUpdates();
                            //}
                        }
                        */
                        /*else*/ if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            if (disableNotUsedScanners) {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "disableNotUsedScanners - EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
                                PhoneProfilesServiceStatic.disableNotUsedScanners(dataWrapper);
                            }
                            registerEventsReceiversAndWorkers(true, appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            if (disableNotUsedScanners) {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "disableNotUsedScanners - EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
                                PhoneProfilesServiceStatic.disableNotUsedScanners(dataWrapper);
                            }
                            unregisterEventsReceiversAndWorkers(false, appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            if (disableNotUsedScanners) {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "disableNotUsedScanners - EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
                                PhoneProfilesServiceStatic.disableNotUsedScanners(dataWrapper);
                            }
                            registerPPPExtenderReceiver(true, dataWrapper, appContext);
                            reregisterEventsReceiversAndWorkers(appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_CONTENT_OBSERVERS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_CONTENT_OBSERVERS");
                            registerAllTheTimeContentObservers(true, appContext);
                            registerContactsContentObservers(true, appContext);
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_CALLBACKS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_CALLBACKS");
                            registerAllTheTimeCallbacks(true, appContext);
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_PHONE_CALLS_LISTENER, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_PHONE_CALLS_LISTENER");
                            registerPhoneCallsListener(true, appContext);
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNREGISTER_PHONE_CALLS_LISTENER, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_PHONE_CALLS_LISTENER");
                            registerPhoneCallsListener(false, appContext);
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "******** EXTRA_SIMULATE_RINGING_CALL ********");
                            PlayRingingNotification.doSimulatingRingingCall(intent, appContext);
                        /*} else if (intent.getBooleanExtra(EXTRA_STOP_SIMULATING_RINGING_CALL, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "******** EXTRA_SIMULATE_RINGING_CALL ********");
                            ppService.stopSimulatingRingingCall(true, appContext);
                        } else if (intent.getBooleanExtra(EXTRA_STOP_SIMULATING_RINGING_CALL_NO_DISABLE_INTERNAL_CHANGE, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "******** EXTRA_SIMULATE_RINGING_CALL ********");
                            ppService.stopSimulatingRingingCall(false, appContext);*/
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerPPPExtenderReceiverForSMSCall(true, dataWrapper);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNREGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerPPPExtenderReceiverForSMSCall(false, dataWrapper);
                            dataWrapper.invalidateDataWrapper();
                        }  else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_CALL_SENSOR, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_FOR_CALL_SENSOR");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerReceiverForCallSensor(true, dataWrapper, appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SENSOR, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SENSOR");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerReceiverForCallSensor(false, dataWrapper, appContext);
                            dataWrapper.invalidateDataWrapper();
                        }  else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_SMS_SENSOR, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_FOR_SMS_SENSOR");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerReceiverForSMSSensor(true, dataWrapper, appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_SMS_SENSOR, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_FOR_SMS_SENSOR");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            registerReceiverForSMSSensor(false, dataWrapper, appContext);
                            dataWrapper.invalidateDataWrapper();
                        } else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_RESCAN_SCANNERS, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_RESCAN_SCANNERS");
                            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
//                                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.doCommand", "PPApplication.locationScannerMutex");
                                synchronized (PPApplication.locationScannerMutex) {
                                    if (PPApplication.locationScanner != null) {
                                        String provider = PPApplication.locationScanner.getProvider(true);
                                        PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                                    }
                                }
                            }

                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            //boolean eventsFilled = false;
                            if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                                //dataWrapper.fillEventList();
                                //eventsFilled = true;
//                                    PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.doCommand", "EXTRA_RESCAN_SCANNERS");
                                scheduleWifiWorker(dataWrapper);
                            }
                            if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                //if (!eventsFilled) {
                                //    dataWrapper.fillEventList();
                                //}
                                scheduleBluetoothWorker(dataWrapper);
                            }

                            if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                                    PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "******** ### ******* (1)");
//                                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.doCommand", "PPApplication.mobileCellsScannerMutex");
                                synchronized (PPApplication.mobileCellsScannerMutex) {
                                    if (PPApplication.mobileCellsScanner != null)
                                        PPApplication.mobileCellsScanner.rescanMobileCells();
                                }
                            }
                            if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                                    PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "******** ### ******* (1)");
                                if (PPApplication.orientationScanner != null) {
                                    PPApplicationStatic.startHandlerThreadOrientationScanner();
                                    if (PPApplication.handlerThreadOrientationScanner != null)
                                        PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
                                }

                                //setOrientationSensorAlarm(appContext);
                                //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                                //sendBroadcast(intent);
                            }
                            if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                                schedulePeriodicScanningWorker();
                            }

                            if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                                if (PPApplication.notificationScannerRunning) {
                                    PPExecutors.handleEvents(appContext,
                                            new int[]{EventsHandler.SENSOR_TYPE_NOTIFICATION},
                                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_NOTIFICATION, 5);
                                }
                            }
                            dataWrapper.invalidateDataWrapper();
                        }
                        //else
                        //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                        //    doSimulatingNotificationTone(intent);
                        else if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, false)) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_START_STOP_SCANNER");
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            //dataWrapper.fillEventList();
                            //dataWrapper.fillProfileList(false, false);
                            switch (intent.getIntExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                                /*case PPApplication.SCANNER_START_LOCATION_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_START_LOCATION_SCANNER");
                                    startLocationScanner(true, true, true, false);
                                    scheduleGeofenceWorker(true, true, false);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_LOCATION_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_STOP_LOCATION_SCANNER");
                                    startLocationScanner(false, true, false, false);
                                    scheduleGeofenceWorker(false, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_START_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, true, true);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                                    startOrientationScanner(false, true, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                                    MobileCellsScanner.forceStart = false;
                                    startMobileCellsScanner(true, true, true, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                                    startMobileCellsScanner(false, true, false, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_TWILIGHT_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_START_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, true, true);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_TWILIGHT_SCANNER:
                                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_STOP_TWILIGHT_SCANNER");
                                    startTwilightScanner(false, true, false);
                                    break;*/
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false, appContext);
                                    registerWifiScannerReceiver(true, dataWrapper, false, appContext);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, true);
                                    //registerWifiStateChangedBroadcastReceiver(true, false, true);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, true, appContext);
                                    registerWifiScannerReceiver(true, dataWrapper, true, appContext);
                                    break;
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    //registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false, appContext);
                                    registerBluetoothScannerReceivers(true, dataWrapper, false, appContext);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                                    //registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, true, appContext);
                                    registerBluetoothScannerReceivers(true, dataWrapper, true, appContext);
                                    break;
                                case PPApplication.SCANNER_RESTART_PERIODIC_SCANNING_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_PERIODIC_SCANNING_SCANNER");
                                    schedulePeriodicScanningWorker();
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false, appContext);
                                    registerWifiScannerReceiver(true, dataWrapper, false, appContext);
//                                        PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
                                    scheduleWifiWorker(/*true,*/ dataWrapper/*, forScreenOn, false, false, true*/);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    //registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false, appContext);
                                    registerBluetoothScannerReceivers(true, dataWrapper, false, appContext);
                                    scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_MOBILE_CELLS_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_MOBILE_CELLS_SCANNER");
                                    //MobileCellsScanner.forceStart = false;
                                    startMobileCellsScanner(true, true, dataWrapper, false, true, appContext);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_FORCE_START_MOBILE_CELLS_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_FORCE_START_MOBILE_CELLS_SCANNER");
                                    //MobileCellsScanner.forceStart = true;
                                    startMobileCellsScanner(true, false, dataWrapper, true, false, appContext);
                                    AvoidRescheduleReceiverWorker.enqueueWork();

                                    if (PPApplication.mobileCellsForceStart) {
//                                            PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PhoneProfilesServiceStatic.doCommand", "xxx");
                                        Intent refreshIntent = new Intent(MobileCellsEditorPreference.ACTION_MOBILE_CELLS_EDITOR_REFRESH_LISTVIEW_BROADCAST_RECEIVER);
                                        LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                    }

                                    break;
                                case PPApplication.SCANNER_RESTART_LOCATION_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_LOCATION_SCANNER");
                                    registerLocationModeChangedBroadcastReceiver(true, dataWrapper, appContext);
                                    startLocationScanner(true, true, dataWrapper, true, appContext);
                                    //scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, false, dataWrapper/*, false*/, appContext);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                /*case PPApplication.SCANNER_FORCE_START_ORIENTATION_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_FORCE_START_ORIENTATION_SCANNER");
                                    //MobileCellsScanner.forceStart = true;
                                    ppService.startOrientationScanner(true, false, dataWrapper, true);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;*/
                                case PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR]  PhoneProfilesService.doCommand", "SCANNER_RESTART_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, false, dataWrapper);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_NOTIFICATION_SCANNER:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_NOTIFICATION_SCANNER");
                                    startNotificationScanner(true, false, dataWrapper, appContext);
                                    AvoidRescheduleReceiverWorker.enqueueWork();
                                    break;
                                case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "SCANNER_RESTART_ALL_SCANNERS");

                                    final boolean fromBatteryChange = intent.getBooleanExtra(PhoneProfilesService.EXTRA_FROM_BATTERY_CHANGE, false);

                                    // background
                                    if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            schedulePeriodicScanningWorker();
                                        }
                                    }

                                    // wifi
                                    if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
//                                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn="+ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn);
//                                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "PPApplication.isScreenOn="+PPApplication.isScreenOn);
//                                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "wifi - canRestart="+canRestart);
                                        if ((!fromBatteryChange) || canRestart) {
//                                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "wifi - restart");
                                            //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                            //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                            registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false, appContext);
                                            registerWifiScannerReceiver(true, dataWrapper, false, appContext);
//                                                PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] PhoneProfilesServiceStatic.doCommand", "SCANNER_RESTART_ALL_SCANNERS");
                                            scheduleWifiWorker(/*true,*/ dataWrapper/*, forScreenOn, false, false, true*/);
                                        }
                                    }

                                    // bluetooth
                                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                            //registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false, appContext);
                                            registerBluetoothScannerReceivers(true, dataWrapper, false, appContext);
                                            scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
                                        }
                                    }

                                    // mobile cells
                                    if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                                            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "******** ### ******* (2)");
                                        boolean canRestart = (!ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            //MobileCellsScanner.forceStart = false;
                                            startMobileCellsScanner(true, true, dataWrapper, false, true, appContext);
                                        }
                                    }

                                    // location
                                    if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            registerLocationModeChangedBroadcastReceiver(true, dataWrapper, appContext);
                                            startLocationScanner(true, true, dataWrapper, true, appContext);
                                            //scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                        }
                                    }

                                    // orientation
                                    if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                                            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "******** ### ******* (2)");
                                        boolean canRestart = (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            startOrientationScanner(true, true, dataWrapper/*, false*/, appContext);
                                        }
                                    }

                                    // notification
                                    if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            startNotificationScanner(true, true, dataWrapper, appContext);
                                        }
                                    }

                                    // twilight - DO NOT RESTART BECAUSE THIS MISS ACTUAL LOCATION
                                    //startTwilightScanner(true, false, dataWrapper);

                                    AvoidRescheduleReceiverWorker.enqueueWork();

                                    break;
                            }
                            dataWrapper.invalidateDataWrapper();
                        }
                        /*else
                        if (intent.getBooleanExtra(EXTRA_RESTART_EVENTS, false)) {
                            PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesService.doCommand", "EXTRA_RESTART_EVENTS");
                            final boolean unblockEventsRun = intent.getBooleanExtra(EXTRA_UNBLOCK_EVENTS_RUN, false);
                            //final boolean reactivateProfile = intent.getBooleanExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, false);
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                            //dataWrapper.restartEvents(unblockEventsRun, true, reactivateProfile, false, false);
                            dataWrapper.restartEventsWithRescan(unblockEventsRun, false, false, false);
                            //dataWrapper.invalidateDataWrapper();
                        }*/
//                            else
//                                PPApplicationStatic.logE("[IN_EXECUTOR]  PhoneProfilesService.doCommand", "???? OTHER ????");

                    }

//                        PPApplicationStatic.logE("[IN_EXECUTOR]  PhoneProfilesService.doCommand", "--- END");

                } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

    static void disableNotUsedScanners(final DataWrapper dataWrapper) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesServiceStatic.disableNotUsedScanners", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            boolean eventsExists;

            dataWrapper.fillEventList();

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventLocationEnableScanning = false;
            }

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventWifiEnableScanning = false;
            }

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventBluetoothEnableScanning = false;
            }

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_MOBILE_CELLS/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventMobileCellEnableScanning = false;
            }

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventOrientationEnableScanning = false;
            }

            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NOTIFICATION/*, false*/);
            if (!eventsExists) {
                SharedPreferences applicationPreferences = ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                SharedPreferences.Editor editor = applicationPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, false);
                editor.apply();
                ApplicationPreferences.applicationEventNotificationEnableScanning = false;
            }
        }
    }

    // Location ----------------------------------------------------------------

    static void startLocationScanner(boolean resetUseGPS, Context context) {
        /*if (PPApplication.locationScanner != null) {
            PPApplication.locationScanner.disconnect();
            PPApplication.locationScanner = null;
        }*/

        if (PPApplication.locationScanner == null) {
            PPApplication.locationScanner = new LocationScanner(context.getApplicationContext());
            PPApplication.locationScanner.connect(resetUseGPS);
        }
        else {
            String provider = PPApplication.locationScanner.getProvider(true);
            PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
        }
    }

    static void stopLocationScanner() {
        if (PPApplication.locationScanner != null) {
            PPApplication.locationScanner.disconnect();
            PPApplication.locationScanner = null;
        }
    }

    /*
    boolean isLocationScannerStarted() {
        return (PPApplication.locationScanner != null);
    }
    */

    /*
    LocationScanner getLocationScanner() {
        return PPApplication.locationScanner;
    }
    */

    //--------------------------------------------------------------------------

    // Phone state ----------------------------------------------------------------

    /*
    private void startMobileCellsScanner() {
        //synchronized (PPApplication.mobileCellsScannerMutex) {
            if (PPApplication.mobileCellsScanner == null) {
                PPApplication.mobileCellsScanner = new MobileCellsScanner(getApplicationContext());
                PPApplication.mobileCellsScanner.connect();
            } else {
                PPApplication.mobileCellsScanner.rescanMobileCells();
            }
        //}
    }
    */

    /*
    private void stopMobileCellsScanner() {
        //synchronized (PPApplication.mobileCellsScannerMutex) {
            if (PPApplication.mobileCellsScanner != null) {
                PPApplication.mobileCellsScanner.disconnect();
                PPApplication.mobileCellsScanner = null;
            }
        //}
    }
    */

    /*
    boolean isMobileCellsScannerStarted() {
        return (PPApplication.mobileCellsScanner != null);
    }
    */

    /*
    MobileCellsScanner getMobileCellsScanner() {
        return PPApplication.mobileCellsScanner;
    }
    */

    //--------------------------------------------------------------------------

    // Device orientation ----------------------------------------------------------------

    static void startOrientationScanner(Context context) {
        //if (PPApplication.mStartedOrientationSensors)
        //    stopListeningOrientationSensors();

        if (!PPApplication.mStartedOrientationSensors)
            startListeningOrientationSensors(context.getApplicationContext());
        else {
            if (PPApplication.orientationScanner != null) {
                PPApplicationStatic.startHandlerThreadOrientationScanner();
                if (PPApplication.handlerThreadOrientationScanner != null)
                    PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
            }

            //setOrientationSensorAlarm(getApplicationContext());
            //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
            //sendBroadcast(intent);
        }
    }

    static void stopOrientationScanner() {
        stopListeningOrientationSensors();
    }

    static boolean isOrientationScannerStarted() {
        return PPApplication.mStartedOrientationSensors;
    }

    static void startListeningOrientationSensors(Context context) {
//        PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startListeningOrientationSensors", "******** ### ******* (1)");
        if (!PPApplication.mStartedOrientationSensors) {
//            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesService.startListeningOrientationSensors", "******** ### ******* (2)");

            PPApplication.orientationScanner = new OrientationScanner();
            PPApplicationStatic.startHandlerThreadOrientationScanner();
            Handler orentationScannerHandler = new Handler(PPApplication.handlerThreadOrientationScanner.getLooper());

            String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context.getApplicationContext());
            if (isPowerSaveMode) {
                if (applicationEventOrientationScanInPowerSaveMode.equals("2"))
                    // start scanning in power save mode is not allowed
                    return;
            }
            else {
                if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                        // not scan in configured time
                        return;
                    }
                }
            }

            //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            //if (DatabaseHandler.getInstance(getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) == 0)
            //    return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval;

            if (isPowerSaveMode) {
                if (applicationEventOrientationScanInPowerSaveMode.equals("1"))
                    interval = 2 * interval;
            }
            else {
                if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("1")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                        interval = 2 * interval;
                    }
                }
            }

            interval = interval / 2;

            if (PPApplication.accelerometerSensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.accelerometerSensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 1000000 * interval, handler);
            }
            if (PPApplication.magneticFieldSensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.magneticFieldSensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 1000000 * interval, handler);
            }

            if (PPApplication.proximitySensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.proximitySensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 1000000 * interval, handler);
            }

            if (PPApplication.lightSensor != null) {
                boolean registerLight = /*EventsPrefsFragment.forceStart ||*/
                        (DatabaseHandler.getInstance(context.getApplicationContext()).getOrientationWithLightSensorEventsCount() != 0);
                if (registerLight) {
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                    //if (PPApplication.lightSensor.getFifoMaxEventCount() > 0)
                    //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 200000 * interval, 1000000 * interval, handler);
                    //else
                    //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 1000000 * interval, handler);
                }
            }

            //Sensor orientation = PPApplication.getOrientationSensor(getApplicationContext());
            PPApplication.mStartedOrientationSensors = true;

            PPApplication.handlerThreadOrientationScanner.tmpSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.tmpSideTimestamp = 0;

            PPApplication.handlerThreadOrientationScanner.previousResultDisplayUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultLight = 0;

            PPApplication.handlerThreadOrientationScanner.resultDisplayUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultLight = 0;

            if (PPApplication.orientationScanner != null) {
                PPApplicationStatic.startHandlerThreadOrientationScanner();
                if (PPApplication.handlerThreadOrientationScanner != null)
                    PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
            }

            //setOrientationSensorAlarm(getApplicationContext());
            //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
            //sendBroadcast(intent);
        }
    }

    static void stopListeningOrientationSensors() {
        if (PPApplication.sensorManager != null) {
            PPApplication.sensorManager.unregisterListener(PPApplication.orientationScanner);
            //removeOrientationSensorAlarm(getApplicationContext());
            PPApplication.orientationScanner = null;
            //PPApplication.sensorManager = null;
        }
        PPApplication.mStartedOrientationSensors = false;
    }

    /*
    public void resetListeningOrientationSensors(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            stopListeningOrientationSensors();
            startListeningOrientationSensors();
        }
    }
    */

    /*
    private void removeOrientationSensorAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_ORIENTATION_EVENT_SENSOR_TAG_WORK);
    }

    void setOrientationSensorAlarm(Context context)
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return;

        int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
            interval *= 2;

        calEndTime.setTimeInMillis((calEndTime.getTimeInMillis() - gmtOffset) + (interval * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        //Intent intent = new Intent(context, OrientationEventEndBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
        //intent.setClass(context, OrientationEventEndBroadcastReceiver.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            }
        }
    }
    */

    // Twilight scanner ----------------------------------------------------------------

    static void startTwilightScanner(final Context context) {
        /*if (PPApplication.twilightScanner != null) {
            PPApplication.twilightScanner.stop();
            PPApplication.twilightScanner = null;
        }*/

        if (PPApplication.twilightScanner == null) {
            // keep this: it is required to use handlerThreadBroadcast for cal listener
            final Context appContext = context.getApplicationContext();
            PPApplicationStatic.startHandlerThreadBroadcast();
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            __handler.post(() -> {
                PPApplication.twilightScanner = new TwilightScanner(appContext);
                PPApplication.twilightScanner.start();
            });
        }
        else {
            PPApplication.twilightScanner.getTwilightState(/*true*/);
        }
    }

    static void stopTwilightScanner() {
        if (PPApplication.twilightScanner != null) {
            PPApplication.twilightScanner.stop();
            PPApplication.twilightScanner = null;
        }
    }

    /*
    private boolean isTwilightScannerStarted() {
        return (PPApplication.twilightScanner != null);
    }
    */

    /*
    TwilightScanner getTwilightScanner() {
        return PPApplication.twilightScanner;
    }
    */

    //--------------------------

}
