package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

class PreferenceAllowed {
    int allowed;
    int notAllowedReason;
    String notAllowedReasonDetail;
    boolean notAllowedRoot;
    boolean notAllowedG1;
    boolean notAllowedPPPPS;
    boolean notAllowedShizuku;

    static final int PREFERENCE_NOT_ALLOWED = 0;
    static final int PREFERENCE_ALLOWED = 1;
    static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 1;
    static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 2;
    static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 3;
    static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 4;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 5;
    private static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 6;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 7;
    private static final int PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED = 8;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION = 9;
    static final int PREFERENCE_NOT_ALLOWED_NO_SIM_CARD = 10;
    static final int PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION = 11;
    static final int PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED = 12;
    static final int PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION = 13;
    static final int PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT = 14;
    static final int PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS = 15;
    static final int PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS = 16;
    static final int PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED = 17;

    void copyFrom(PreferenceAllowed preferenceAllowed) {
        allowed = preferenceAllowed.allowed;
        notAllowedReason = preferenceAllowed.notAllowedReason;
        notAllowedReasonDetail = preferenceAllowed.notAllowedReasonDetail;
        notAllowedRoot = preferenceAllowed.notAllowedRoot;
        notAllowedG1 = preferenceAllowed.notAllowedG1;
        notAllowedPPPPS = preferenceAllowed.notAllowedPPPPS;
        notAllowedShizuku = preferenceAllowed.notAllowedShizuku;
    }

    String getNotAllowedPreferenceReasonString(Context context) {
        switch (notAllowedReason) {
            case PREFERENCE_NOT_ALLOWED_NO_HARDWARE: return context.getString(R.string.preference_not_allowed_reason_no_hardware);
            case PREFERENCE_NOT_ALLOWED_NOT_ROOTED: return context.getString(R.string.preference_not_allowed_reason_not_rooted);
            case PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_settings_not_found);
            case PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_service_not_found);
            case PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS: return context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM:
                return context.getString(R.string.preference_not_allowed_reason_not_supported) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_by_application) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED:
                return context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_android_version) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NO_SIM_CARD: return context.getString(R.string.preference_not_allowed_reason_no_sim_card);
            case PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS: return context.getString(R.string.preference_not_allowed_reason_not_two_sim_cards);
            case PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION: return context.getString(R.string.preference_not_allowed_reason_not_granted_g1_permission);
            case PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION: return context.getString(R.string.preference_not_allowed_reason_not_granted_phone_permission);
            case PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT: return context.getString(R.string.preference_not_allowed_reason_not_set_as_assistant);
            case PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS: return context.getString(R.string.preference_not_allowed_reason_not_installed_ppps);
            case PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED: return context.getString(R.string.preference_not_allowed_reason_not_granted_shizuku);
            default: return "";
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_AIRPLANE_MODE(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE;

        boolean assistantParameter = true;
        if (profile != null) {
            assistantParameter = profile._deviceAirplaneMode >= 4;
        } else if (sharedPreferences != null) {
            assistantParameter = Integer.parseInt(sharedPreferences.getString(preferenceKey, "0")) >= 4;
        }

        if ((!assistantParameter) && ShizukuUtils.hasShizukuPermission()) {
            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        }
        else
        if ((!assistantParameter) && RootUtils.isRooted(/*fromUIThread*/)) {
            // device is rooted

            if (profile != null) {
                // test if grant root is disabled
                if (profile._deviceAirplaneMode < 4) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                    }
                }
            } else
            //noinspection ConstantValue
            if (sharedPreferences != null) {
                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        } else {
            if (assistantParameter) {
                // check if default Assistent is set to PPP
                if (ActivateProfileHelper.isPPPSetAsDefaultAssistant(context)) {
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT;
                    //if ((profile != null) && (profile._deviceAirplaneMode != 0)) {
                    //    preferenceAllowed.notAllowedRoot = true;
                    //}
                }
            } else {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        preferenceAllowed.notAllowedShizuku = true;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            preferenceAllowed.notAllowedShizuku = true;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI(PreferenceAllowed preferenceAllowed,
                  Profile profile, SharedPreferences sharedPreferences/*, boolean fromUIThread*/) {

        if (PPApplication.HAS_FEATURE_WIFI) {
            // device has Wifi
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;

            boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

            String preferenceKey = Profile.PREF_PROFILE_DEVICE_WIFI;

            boolean requiresRoot = false;
            if (profile != null) {
                requiresRoot = (profile._deviceWiFi == 6) || (profile._deviceWiFi == 7) || (profile._deviceWiFi == 8);
            } else if (sharedPreferences != null) {
                String preferenceValue = sharedPreferences.getString(preferenceKey, "0");
                requiresRoot = preferenceValue.equals("6") || preferenceValue.equals("7") || preferenceValue.equals("8");
            }

            if (requiresRoot) {
                if (ShizukuUtils.hasShizukuPermission()) {
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                if (RootUtils.isRooted(/*fromUIThread*/)) {
                    // shizuku is not granted but device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        /*if ((profile._deviceWiFi == 6) ||
                                (profile._deviceWiFi == 7) ||
                                (profile._deviceWiFi == 8)) {*/
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                        //}
                    } else
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            //String preferenceValue = sharedPreferences.getString(preferenceKey, "0");
                            /*if (preferenceValue.equals("6") ||
                                    preferenceValue.equals("7") ||
                                    preferenceValue.equals("8")) {*/
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                            }
                            //}
                        }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                    if ((profile != null) && (profile._deviceWiFi != 0)) {
                        preferenceAllowed.notAllowedShizuku = true;
                    }
                }
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_BLUETOOTH(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_BLUETOOTH)
            // device has bluetooth
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA(PreferenceAllowed preferenceAllowed,
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, /*boolean fromUIThread,*/ Context context) {
        PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "*******************");

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        boolean mobileDataSupported = false;
        if (!PPApplication.HAS_FEATURE_TELEPHONY) {
            // check mobile data capability for devices without phone call hardware (for example tablets)

            ConnectivityManager connManager = null;
            try {
                connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception e) {
                // java.lang.NullPointerException: missing IConnectivityManager
                // Dual SIM?? Bug in Android ???
                PPApplicationStatic.recordException(e);
            }

            if (connManager != null) {
                Network[] networks = connManager.getAllNetworks();
                //noinspection ConstantValue,RedundantLengthCheck
                if ((networks != null) && (networks.length > 0)) {
                    for (Network network : networks) {
                        try {
                                /*if (Build.VERSION.SDK_INT < 28) {
                                    NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                    if (ntkInfo != null) {
                                        if (ntkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                            mobileDataSupported = true;
                                            PPApplicationStatic.logE("[DUAL_SIM] Profile.isProfilePreferenceAllowed", "mobileDataSupported=true");
                                            break;
                                        }
                                    }
                                }
                                else {*/
                            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                            if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                mobileDataSupported = true;
                                break;
                            }
                            //}
                        } catch (Exception ee) {
                            PPApplicationStatic.recordException(ee);
                        }
                    }
                }
            }
            //else
            //    mobileDataSupported = false;
        }
        else
            mobileDataSupported = true;
        if (mobileDataSupported)
        {
            //Log.d("Profile.isProfilePreferenceAllowed", "mobile data supported");
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
            // not working :-/
            if (Permissions.hasPermission(appContext, Manifest.permission.MODIFY_PHONE_STATE)) {
                if (ActivateProfileHelper.canSetMobileData(appContext)) {
                    if (profile != null) {
                        if (profile._deviceMobileData != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
            }
            else
            if (ShizukuUtils.hasShizukuPermission()) {
                // not needed, used is "svc data enable/disable"
                /*if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (PPApplication.serviceBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if (profile._deviceMobileData != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }*/

                preferenceAllowed.allowed = PREFERENCE_ALLOWED;

                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);

                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM1="+hasSIMCardData.hasSIM1);
                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM2="+hasSIMCardData.hasSIM2);

                    boolean sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;

                    if (!sim0Exists) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceMobileData != 0)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                // not needed, used is "svc data enable/disable"
                /*if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (PPApplication.serviceBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if (profile._deviceMobileData != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }*/

                preferenceAllowed.allowed = PREFERENCE_ALLOWED;

                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);

                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM1="+hasSIMCardData.hasSIM1);
                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM2="+hasSIMCardData.hasSIM2);

                    boolean sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;

                    if (!sim0Exists) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                if ((profile != null) && (profile._deviceMobileData != 0)) {
                    preferenceAllowed.notAllowedShizuku = true;
                }
            }
        }
        else {
            //Log.d("Profile.isProfilePreferenceAllowed", "mobile data not supported");
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    /*
    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA_DUAL_SIM(PreferenceAllowed preferenceAllowed,
                                                                           String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {
        Context appContext = context.getApplicationContext();


            boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

            boolean mobileDataSupported = false;
            if (!PPApplication.HAS_FEATURE_TELEPHONY) {
                // check mobile data capability for devices without phone call hardware (for example tablets)

                ConnectivityManager connManager = null;
                try {
                    connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                } catch (Exception e) {
                    // java.lang.NullPointerException: missing IConnectivityManager
                    // Dual SIM?? Bug in Android ???
                    PPApplicationStatic.recordException(e);
                }

                if (connManager != null) {
                    Network[] networks = connManager.getAllNetworks();
                    if ((networks != null) && (networks.length > 0)) {
                        for (Network network : networks) {
                            try {
                                //if (Build.VERSION.SDK_INT < 28) {
                                //    NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                //    if (ntkInfo != null) {
                                //        if (ntkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                //            mobileDataSupported = true;
                                //            PPApplicationStatic.logE("[DUAL_SIM] Profile.isProfilePreferenceAllowed", "mobileDataSupported=true");
                                //            break;
                                //        }
                                //    }
                                //}
                                //else {
                                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                    mobileDataSupported = true;
                                    break;
                                }
                                //}
                            } catch (Exception ee) {
                                PPApplicationStatic.recordException(ee);
                            }
                        }
                    }
                }
                //else
                //    mobileDataSupported = false;
            } else
                mobileDataSupported = true;
            if (mobileDataSupported) {
                //Log.d("Profile.isProfilePreferenceAllowed", "mobile data supported");
                // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
                // not working :-/
                if (Permissions.hasPermission(appContext, Manifest.permission.MODIFY_PHONE_STATE)) {
                    if (ActivateProfileHelper.canSetMobileData(appContext))
                        if (profile != null) {
                            if ((profile._deviceMobileDataSIM1 != 0) ||
                                    (profile._deviceMobileDataSIM2 != 0))
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else if (RootUtils.isRooted(fromUIThread)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if ((profile._deviceMobileDataSIM1 != 0) ||
                                (profile._deviceMobileDataSIM2 != 0)) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                        if (RootUtils.serviceBinaryExists(fromUIThread)) {
                            if (profile != null) {
                                if ((profile._deviceMobileDataSIM1 != 0) ||
                                        (profile._deviceMobileDataSIM2 != 0))
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            //if (!sim1Exists) {
                            //    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            //    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            //}
                            //if (!sim2Exists) {
                            //    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            //    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            //}
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                    if ((profile != null) &&
                            ((profile._deviceMobileDataSIM1 != 0) ||
                             (profile._deviceMobileDataSIM2 != 0))) {
                        preferenceAllowed.notAllowedRoot = true;
                    }
                }
            } else {
                //Log.d("Profile.isProfilePreferenceAllowed", "mobile data not supported");
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
    }
    */

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS(PreferenceAllowed preferenceAllowed) {
        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_GPS(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        if (Build.VERSION.SDK_INT < 29) {
            Context appContext = context.getApplicationContext();

            boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

            String preferenceKey = Profile.PREF_PROFILE_DEVICE_GPS;

            if (PPApplication.HAS_FEATURE_LOCATION_GPS) {
                // device has gps
                // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
                if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    if (profile != null) {
                        if (profile._deviceGPS != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else if (RootUtils.isRooted(/*fromUIThread*/)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._deviceGPS != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (RootUtils.settingsBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if (profile._deviceGPS != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                }
                /*else
                if (ActivateProfileHelper.canExploitGPS(appContext))
                {
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }*/
                else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                    if ((profile != null) && (profile._deviceGPS != 0)) {
                        //return preferenceAllowed;
                        //preferenceAllowed.notAllowedRoot = true;
                        preferenceAllowed.notAllowedG1 = true;
                    }
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        } else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            preferenceAllowed.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_LOCATION_MODE(PreferenceAllowed preferenceAllowed,
            Profile profile, Context context) {

        Context appContext = context.getApplicationContext();

        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            if (profile != null) {
                if (profile._deviceLocationMode != 0)
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
            /*else
            if (PPApplication.isRooted(fromUIThread))
            {
                // device is rooted - NOT WORKING

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._deviceLocationMode != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            //return preferenceAllowed;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return preferenceAllowed;
                        }
                    }
                }

                if (PPApplication.settingsBinaryExists(fromUIThread))
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }*/
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            if ((profile != null) && (profile._deviceLocationMode != 0)) {
                //return preferenceAllowed;
                //preferenceAllowed.notAllowedRoot = true;
                preferenceAllowed.notAllowedG1 = true;
            }
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NFC(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, /*boolean fromUIThread,*/ Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_NFC;

        if (PPApplication.HAS_FEATURE_NFC)
        {
            // device has nfc
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._deviceNFC != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
            if (RootUtils.isRooted(/*fromUIThread*/)) {

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._deviceNFC != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (profile != null) {
                    if (profile._deviceNFC != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._deviceNFC != 0)) {
                    //return preferenceAllowed;
                    //preferenceAllowed.notAllowedRoot = true;
                    preferenceAllowed.notAllowedG1 = true;
                }
            }
        }
        else
        {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }

    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_WIFI_AP;

        //if (Build.VERSION.SDK_INT < 30) {
            if (PPApplication.HAS_FEATURE_WIFI) {
                // device has Wifi
                if (Build.VERSION.SDK_INT < 28) {
                    if (WifiApManager.canExploitWifiTethering(appContext)) {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                    if (PPApplication.rootMutex.transactionCode_setWifiApEnabled != -1) {
                        if (ShizukuUtils.hasShizukuPermission()) {
                            if (ActivateProfileHelper.wifiServiceExists(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                                if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                    if (profile != null) {
                                        if (profile._deviceWiFiAP != 0)
                                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            }
                        } else if (RootUtils.isRooted(/*fromUIThread*/)) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if (profile._deviceWiFiAP != 0) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (ActivateProfileHelper.wifiServiceExists(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                                if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                    if (profile != null) {
                                        if (profile._deviceWiFiAP != 0)
                                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            }
                        } else {
                            if ((profile != null) && (profile._deviceWiFiAP != 0)) {
                                preferenceAllowed.notAllowedShizuku = true;
                            }
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else if (Build.VERSION.SDK_INT < 30) {
                    if (WifiApManager.canExploitWifiTethering(appContext)) {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        //if ((profile != null) && (profile._deviceWiFiAP != 0)) {
                        //    preferenceAllowed.notAllowedRoot = true;
                        //}
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else {
                    if (WifiApManager.canExploitWifiTethering30(appContext)) {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        //if ((profile != null) && (profile._deviceWiFiAP != 0)) {
                        //    preferenceAllowed.notAllowedRoot = true;
                        //}
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        //}
        //else {
        //    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
        //    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
        //    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_android_version);
        //}

    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_WHEN_RINGING(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING;

        if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) || PPApplication.deviceIsOnePlus) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                if (profile != null) {
                    if (profile._vibrateWhenRinging != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._vibrateWhenRinging != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile._vibrateWhenRinging != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && (profile._vibrateWhenRinging != 0)) {
                    boolean enabled = false;
                    if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 4))
                        enabled = true;
                    if (profile._volumeRingerMode == 5) {
                        if ((profile._volumeZenMode == 1) || (profile._volumeZenMode == 2))
                            enabled = true;
                    }
                    if (enabled) {
                        preferenceAllowed.notAllowedPPPPS = true;
                    }
                }
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            if (profile != null) {
                if (profile._vibrateWhenRinging != 0)
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
    }


    static void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_NOTIFICATIONS(PreferenceAllowed preferenceAllowed,
                                                                             Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {

            if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                    (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                    (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                    PPApplication.deviceIsOnePlus) {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            }
            else {

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS;

                if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                    if (profile != null) {
                        if (profile._vibrateNotifications != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                if (RootUtils.isRooted(/*fromUIThread*/)) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._vibrateNotifications != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (RootUtils.settingsBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if (profile._vibrateNotifications != 0)
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if ((profile != null) && (profile._vibrateNotifications != 0)) {
                        preferenceAllowed.notAllowedPPPPS = true;
                    }
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                }
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    static void isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(
            PreferenceAllowed preferenceAllowed,
            Context context) {

        Context appContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT < 29) {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);

        } else
        if ((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
            (PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT < 33)) ||
            (PPApplication.deviceIsOnePlus && (Build.VERSION.SDK_INT < 31))) {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        } else {
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_RINGING(
            PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(preferenceAllowed);
        PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
        if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                if (profile != null) {
                    if (profile.getVibrationIntensityRingingChange())
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityRingingChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityRingingChange())
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityRingingChange()) {
                    preferenceAllowed.notAllowedPPPPS = true;
                }
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        } else {
            preferenceAllowed.copyFrom(_preferenceAllowed);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS(
            PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(preferenceAllowed);
        PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
//        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS", "_preferenceAllowed.allowed="+_preferenceAllowed.allowed);
        if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                if (profile != null) {
                    if (profile.getVibrationIntensityNotificationsChange())
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityNotificationsChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityNotificationsChange())
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityNotificationsChange()) {
                    preferenceAllowed.notAllowedPPPPS = true;
                }
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            preferenceAllowed.copyFrom(_preferenceAllowed);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION(
            PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(preferenceAllowed);
        PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
        if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                if (profile != null) {
                    if (profile.getVibrationIntensityTouchInteractionChange())
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityTouchInteractionChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityTouchInteractionChange())
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityTouchInteractionChange()) {
                    preferenceAllowed.notAllowedPPPPS = true;
                }
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            preferenceAllowed.copyFrom(_preferenceAllowed);
        }
    }

    /*
    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS(PreferenceAllowed preferenceAllowed,
            SharedPreferences sharedPreferences, boolean fromUIThread) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (RootUtils.isRooted(fromUIThread)) {
            // device is rooted

            if (sharedPreferences != null) {
                String value = sharedPreferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
                if (ProfileStatic.getDeviceBrightnessChange(value) && ProfileStatic.getDeviceBrightnessAutomatic(value)) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        } else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
        }
    }
    */

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_POWER_SAVE_MODE(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE;

        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            if (profile != null) {
                if (profile._devicePowerSaveMode != 0)
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
        else
        if (RootUtils.isRooted(/*fromUIThread*/)) {
            // device is rooted

            if (profile != null) {
                // test if grant root is disabled
                if (profile._devicePowerSaveMode != 0) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                    }
                }
            }
            else
            if (sharedPreferences != null) {
                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                if (profile != null) {
                    if (profile._devicePowerSaveMode != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            if ((profile != null) && (profile._devicePowerSaveMode != 0)) {
                //preferenceAllowed.notAllowedRoot = true;
                preferenceAllowed.notAllowedG1 = true;
            }
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE(PreferenceAllowed preferenceAllowed,
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {

                final int phoneType = telephonyManager.getPhoneType();
                if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                    if (ShizukuUtils.hasShizukuPermission()) {
                        if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                if (profile == null)
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                else {
                                    if (profile._deviceNetworkType != 0)
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                }
                            }
                            else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                            }
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                        }

                        boolean sim0Exists;
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;

                        if (!sim0Exists) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    } else
                    if (RootUtils.isRooted(/*fromUIThread*/)) {
                        // device is rooted

                        if (profile != null) {
                            // test if grant root is disabled
                            if ((profile._deviceNetworkType != 0)) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                }
                            }
                        }
                        else
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    // not needed to test all parameters
                                    return;
                                }
                            }
                        }

                        if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                if (profile == null)
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                else {
                                    if (profile._deviceNetworkType != 0)
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                }
                            }
                            else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                            }
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                        }

                        boolean sim0Exists;
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;

                        if (!sim0Exists) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    } else {
                        if ((profile != null) && (profile._deviceNetworkType != 0)) {
                            preferenceAllowed.notAllowedShizuku = true;
                        }
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_DUAL_SIM(PreferenceAllowed preferenceAllowed,
                        String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

            boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
//                    PPApplicationStatic.logE("[DUAL_SIM] Profile.isProfilePreferenceAllowed", "phoneCount="+phoneCount);

                    final int phoneType = telephonyManager.getPhoneType();
                    if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                        if (ShizukuUtils.hasShizukuPermission()) {
                            if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                                if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                    if (profile == null)
                                        //noinspection UnusedAssignment
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    else {
                                        if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                                (profile._deviceNetworkTypeSIM2 != 0))
                                            //noinspection UnusedAssignment
                                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    }

                                    if (phoneCount > 1) {
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    } else {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                                    }
                                } else {
                                    //noinspection UnusedAssignment
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                            }
                        } else
                        if (RootUtils.isRooted(/*fromUIThread*/)) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                        (profile._deviceNetworkTypeSIM2 != 0)
                                ) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                                if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                    if (profile == null)
                                        //noinspection UnusedAssignment
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    else {
                                        if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                                (profile._deviceNetworkTypeSIM2 != 0))
                                            //noinspection UnusedAssignment
                                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    }

                                    if (phoneCount > 1) {
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                    } else {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                                    }
                                } else {
                                    //noinspection UnusedAssignment
                                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                            }
                        } else {
                            if ((profile != null) &&
                                    ((profile._deviceNetworkTypeSIM1 != 0) ||
                                     (profile._deviceNetworkTypeSIM2 != 0))) {
                                preferenceAllowed.notAllowedShizuku = true;
                            }
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

//        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "xxx");

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_NOTIFICATION_LED;

        int value = Settings.System.getInt(appContext.getContentResolver(), "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/, -10);
        if (value != -10) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
//                Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "installed");
                if (profile != null) {
                    if (profile._notificationLed != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._notificationLed != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile._notificationLed != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
//                Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(2)");
                if ((profile != null) && (profile._notificationLed != 0)) {
                    preferenceAllowed.notAllowedPPPPS = true;
                }
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
//            Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(3)");
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_KEYGUARD(PreferenceAllowed preferenceAllowed,
            Context context) {

        Context appContext = context.getApplicationContext();

        boolean secureKeyguard;
        KeyguardManager keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            secureKeyguard = keyguardManager.isKeyguardSecure();
            if (secureKeyguard) {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
            } else
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_CONNECT_TO_SSID(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_WIFI)
            // device has Wifi
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_WIFI)
            // device has Wifi
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_BLUETOOTH)
            // device has bluetooth
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP_PREFS(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_WIFI)
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_TELEPHONY)
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.sensorManager != null) {
            boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
            //boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
            boolean hasProximity = PPApplication.proximitySensor != null;
            boolean hasLight = PPApplication.lightSensor != null;

            if (hasAccelerometer || hasProximity || hasLight)
                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_HEADS_UP_NOTIFICATIONS(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS;

        int value = Settings.Global.getInt(appContext.getContentResolver(), "heads_up_notifications_enabled", -10);
        if (value != -10) {
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._headsUpNotifications != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._headsUpNotifications != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile._headsUpNotifications != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._headsUpNotifications != 0)) {
                    //return preferenceAllowed;
                    //preferenceAllowed.notAllowedRoot = true;
                    preferenceAllowed.notAllowedG1 = true;
                }
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS(PreferenceAllowed preferenceAllowed) {

        if (PPApplication.HAS_FEATURE_TELEPHONY)
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VOLUME_ACCESSIBILITY(PreferenceAllowed preferenceAllowed/*,
            Context context*/) {

        //Context appContext = context.getApplicationContext();

        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_ALWAYS_ON_DISPLAY(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY;

            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._headsUpNotifications != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._alwaysOnDisplay != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile._alwaysOnDisplay != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._alwaysOnDisplay != 0)) {
                    //return preferenceAllowed;
                    //preferenceAllowed.notAllowedRoot = true;
                    preferenceAllowed.notAllowedG1 = true;
                }
            }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_DARK_MODE(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_SCREEN_DARK_MODE;

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._screenDarkMode != 0)
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
            }
            else
            if (RootUtils.isRooted(/*fromUIThread*/))
            {
                // device is rooted
                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._screenDarkMode != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (RootUtils.settingsBinaryExists(fromUIThread)) {
                    if (profile != null) {
                        if (profile._screenDarkMode != 0)
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else
                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                }
                else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._screenDarkMode != 0)) {
                    //return preferenceAllowed;
                    preferenceAllowed.notAllowedG1 = true;
                }
            }
        }
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_VOLUME_SPEAKER_PHONE(PreferenceAllowed preferenceAllowed,
            Context context) {

        Context appContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT < 29)
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_android_version);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_CAMERA_FLASH(PreferenceAllowed preferenceAllowed/*, Context context*/) {
        boolean flashAvailable;

        if (PPApplication.HAS_FEATURE_CAMERA_FLASH) {
            /* Hm, hm - this may require CAMERA permission - do not use this
            try {
                CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics("0");
                flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            } catch (CameraAccessException e) {
                flashAvailable = false;
            }*/
            flashAvailable = true;
        }
        else {
            flashAvailable = false;
        }
        if (flashAvailable) {
            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
        } else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS;

            //if (PPApplication.HAS_FEATURE_TELEPHONY) {
            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
//                        PPApplicationStatic.logE("[DUAL_SIM] Profile.isProfilePreferenceAllowed", "phoneCount="+phoneCount);

                if (ShizukuUtils.hasShizukuPermission()) {
                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                        if (phoneCount > 1) {
                            if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                if (profile != null) {
                                    if (!profile._deviceDefaultSIMCards.equals("0|0|0"))
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                            }
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else
                if (RootUtils.isRooted(/*fromUIThread*/)) {
                    // device is rooted
                    if (profile != null) {
                        // test if grant root is disabled
                        if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0|0|0").equals("0|0|0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                        if (phoneCount > 1) {
                            if (RootUtils.serviceBinaryExists(fromUIThread)) {
                                if (profile != null) {
                                    if (!profile._deviceDefaultSIMCards.equals("0|0|0"))
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                            }
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else {
                    if ((profile != null) && (!profile._deviceDefaultSIMCards.equals("0|0|0"))) {
                        preferenceAllowed.notAllowedShizuku = true;
                    }
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            }
            //} else {
            //    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            //    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            //}
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM(PreferenceAllowed preferenceAllowed,
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (Build.VERSION.SDK_INT >= 29) {
            if (ShizukuUtils.hasShizukuPermission()) {
                if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)) {
                    if (RootUtils.serviceBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if ((profile._deviceOnOffSIM1 != 0) ||
                                    (profile._deviceOnOffSIM2 != 0))
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            } else
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceOnOffSIM1 != 0) ||
                            (profile._deviceOnOffSIM2 != 0)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)) {
                    if (RootUtils.serviceBinaryExists(fromUIThread)) {
                        if (profile != null) {
                            if ((profile._deviceOnOffSIM1 != 0) ||
                                    (profile._deviceOnOffSIM2 != 0))
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }

                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                if ((profile != null) &&
                        ((profile._deviceOnOffSIM1 != 0) ||
                         (profile._deviceOnOffSIM2 != 0))) {
                    preferenceAllowed.notAllowedShizuku = true;
                }
            }
        } else {
            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM(PreferenceAllowed preferenceAllowed,
            Profile profile, Context context) {

        Context appContext = context.getApplicationContext();

            if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                    (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                    (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                    (PPApplication.deviceIsOnePlus)) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile != null) {
                            if ((profile._soundRingtoneChangeSIM1 != 0) ||
                                    (profile._soundRingtoneChangeSIM2 != 0))
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_by_ppp);
            }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM(PreferenceAllowed preferenceAllowed,
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context, boolean forSIM2) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

            if (((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                    (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI))) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        boolean rootRequired = false;
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI && forSIM2)
                            rootRequired = true;
                        boolean ppppsInstalled = false;
                        if (!rootRequired) {
                            ppppsInstalled = ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0;
                        }
                        if (!rootRequired && ppppsInstalled) {
                            if (profile != null) {
                                if ((profile._soundNotificationChangeSIM1 != 0) ||
                                        (profile._soundNotificationChangeSIM2 != 0))
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                        if (rootRequired && ShizukuUtils.hasShizukuPermission()) {
                            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                                if (profile != null) {
                                    if ((profile._soundNotificationChangeSIM1 != 0) ||
                                            (profile._soundNotificationChangeSIM2 != 0))
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                            }
                        } else
                        if (RootUtils.isRooted(/*fromUIThread*/)) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._soundNotificationChangeSIM1 != 0) ||
                                        (profile._soundNotificationChangeSIM2 != 0)) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                                if (profile != null) {
                                    if ((profile._soundNotificationChangeSIM1 != 0) ||
                                            (profile._soundNotificationChangeSIM2 != 0))
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                            }
                        } else {
                            if (profile != null) {
                                if (profile._soundNotificationChangeSIM1 != 0)
                                    preferenceAllowed.notAllowedPPPPS = true;
                                else
                                if (profile._soundNotificationChangeSIM2 != 0) {
                                    if (rootRequired && (!ShizukuUtils.hasShizukuPermission()))
                                        preferenceAllowed.notAllowedShizuku = true;
                                    else
                                        preferenceAllowed.notAllowedPPPPS = true;
                                }
                            }
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            if (rootRequired && (!ShizukuUtils.hasShizukuPermission()))
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            else
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
            if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                PPApplication.deviceIsOnePlus) {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported);
            }
            else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_by_ppp);
            }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS(PreferenceAllowed preferenceAllowed,
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS;

            if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                    PPApplication.deviceIsOnePlus) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0) {
                            if (profile != null) {
                                if (profile._soundSameRingtoneForBothSIMCards != 0)
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                        } else
                        if (RootUtils.isRooted(/*fromUIThread*/)) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._soundSameRingtoneForBothSIMCards != 0)) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (RootUtils.settingsBinaryExists(fromUIThread)) {
                                if (profile != null) {
                                    if (profile._soundSameRingtoneForBothSIMCards != 0)
                                        preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                            }
                        } else {
                            if ((profile != null) && (profile._soundSameRingtoneForBothSIMCards != 0)) {
                                preferenceAllowed.notAllowedPPPPS = true;
                            }
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                        }
                    } else {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported);
            }
    }

    static void isProfilePreferenceAllowed_PREF_PROFILE_LOCK_DEVICE(PreferenceAllowed preferenceAllowed,
                                Profile profile, SharedPreferences sharedPreferences) {

        preferenceAllowed.allowed = PREFERENCE_ALLOWED;

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_LOCK_DEVICE;

        boolean requiresRoot = false;
        if (profile != null) {
            requiresRoot = (profile._lockDevice == 2);
        } else if (sharedPreferences != null) {
            String preferenceValue = sharedPreferences.getString(preferenceKey, "0");
            requiresRoot = preferenceValue.equals("2");
        }

        if (requiresRoot) {
            if (RootUtils.isRooted(/*fromUIThread*/)) {
                // shizuku is not granted but device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    /*if ((profile._deviceWiFi == 6) ||
                            (profile._deviceWiFi == 7) ||
                            (profile._deviceWiFi == 8)) {*/
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                    }
                    //}
                } else
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        //String preferenceValue = sharedPreferences.getString(preferenceKey, "0");
                        /*if (preferenceValue.equals("6") ||
                                preferenceValue.equals("7") ||
                                preferenceValue.equals("8")) {*/
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                            preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                        }
                        //}
                    }
            } else {
                preferenceAllowed.allowed = PREFERENCE_NOT_ALLOWED;
                preferenceAllowed.notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                if ((profile != null) && (profile._deviceWiFi != 0)) {
                    preferenceAllowed.notAllowedRoot = true;
                }
            }
        }
    }

}
