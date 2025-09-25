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
    int preferenceAllowed;
    int notAllowedReason;
    String notAllowedReasonDetail;
    boolean notAllowedRoot;
    boolean notAllowedG1;
    boolean notAllowedPPPPS;
    boolean notAllowedShizuku;
    boolean notInstalledDelta;

    private int _isRooted = -1;
    private int _serviceBinaryExists = -1;
    private int _settingsBinaryExists = -1;
    private int _isPPPPSInstalled = -1;
    private int _isShiuzkuAvailable = -1;
    private int _isShiuzkuGranted = -1;
    private int _canExploitWifiTethering = -1;
    private int _canExploitWifiTethering30 = -1;

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
    static final int PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_DELTA = 18;

    void copyFrom(PreferenceAllowed preferenceAllowed) {
        this.preferenceAllowed = preferenceAllowed.preferenceAllowed;
        notAllowedReason = preferenceAllowed.notAllowedReason;
        notAllowedReasonDetail = preferenceAllowed.notAllowedReasonDetail;
        notAllowedRoot = preferenceAllowed.notAllowedRoot;
        notAllowedG1 = preferenceAllowed.notAllowedG1;
        notAllowedPPPPS = preferenceAllowed.notAllowedPPPPS;
        notAllowedShizuku = preferenceAllowed.notAllowedShizuku;
        notInstalledDelta = preferenceAllowed.notInstalledDelta;
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
            case PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_DELTA: return context.getString(R.string.preference_not_allowed_reason_not_installed_delta);
            default: return "";
        }
    }

    private int isRooted() {
        if (_isRooted == -1) {
            if (RootUtils.isRooted(/*fromUIThread*/))
                _isRooted = 1;
            else
                _isRooted = 0;
        }
        return _isRooted;
    }

    private int serviceBinaryExists(boolean fromUIThread) {
        if (_serviceBinaryExists == -1) {
            if (RootUtils.serviceBinaryExists(fromUIThread))
                _serviceBinaryExists = 1;
            else
                _serviceBinaryExists = 0;
        }
        return _serviceBinaryExists;
    }

    private int settingsBinaryExists(boolean fromUIThread) {
        if (_settingsBinaryExists == -1) {
            if (RootUtils.settingsBinaryExists(fromUIThread))
                _settingsBinaryExists = 1;
            else
                _settingsBinaryExists = 0;
        }
        return _settingsBinaryExists;
    }

    private int isPPPPSInstalled(Context context) {
        if (_isPPPPSInstalled == -1) {
            if (ActivateProfileHelper.isPPPPutSettingsInstalled(context) >= PPApplication.VERSION_CODE_PPPPS_REQUIRED)
                _isPPPPSInstalled = 1;
            else
                _isPPPPSInstalled = 0;
        }
        return _isPPPPSInstalled;
    }

    private int isShiuzkuAvailable() {
        if (_isShiuzkuAvailable == -1) {
            if (ShizukuUtils.shizukuAvailable())
                _isShiuzkuAvailable = 1;
            else
                _isShiuzkuAvailable = 0;
        }
        return _isShiuzkuAvailable;
    }

    private int isShiuzkuGranted(boolean checkAvailability) {
        int available = 1;
        if (checkAvailability)
            available = isShiuzkuAvailable();
        if (available == 1) {
            if (_isShiuzkuGranted == -1) {
                if (ShizukuUtils.hasShizukuPermission())
                    _isShiuzkuGranted = 1;
                else
                    _isShiuzkuGranted = 0;
            }
            return _isShiuzkuGranted;
        } else
            return 0;
    }

    private int canExploitWifiTethering(Context context) {
        if (_canExploitWifiTethering == -1) {
            if (WifiApManager.canExploitWifiTethering(context))
                _canExploitWifiTethering = 1;
            else
                _canExploitWifiTethering = 0;
        }
        return _canExploitWifiTethering;
    }

    private int canExploitWifiTethering30(Context context) {
        if (_canExploitWifiTethering30 == -1) {
            if (WifiApManager.canExploitWifiTethering30(context))
                _canExploitWifiTethering30 = 1;
            else
                _canExploitWifiTethering30 = 0;
        }
        return _canExploitWifiTethering30;
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_AIRPLANE_MODE(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE;

        boolean assistantParameter = true;
        if (profile != null) {
            assistantParameter = profile._deviceAirplaneMode >= 4;
        } else if (sharedPreferences != null) {
            assistantParameter = Integer.parseInt(sharedPreferences.getString(preferenceKey, "0")) >= 4;
        }

        if ((!assistantParameter) && (isShiuzkuGranted(true) == 1)) {
            if (settingsBinaryExists(fromUIThread) == 1) {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
        }
        else
        if ((!assistantParameter) && (isRooted() == 1)) {
            // device is rooted

            if (profile != null) {
                // test if grant root is disabled
                if (profile._deviceAirplaneMode < 4) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        return;
                    }
                }
            } else
            //noinspection ConstantValue
            if (sharedPreferences != null) {
                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (settingsBinaryExists(fromUIThread) == 1) {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        } else {
            if (assistantParameter) {
                // check if default Assistent is set to PPP
                if (ActivateProfileHelper.isPPPSetAsDefaultAssistant(context)) {
                    preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT;
                    //if ((profile != null) && (profile._deviceAirplaneMode != 0)) {
                    //    notAllowedRoot = true;
                    //}
                }
            } else {
                if (profile != null) {
                    if (profile._deviceAirplaneMode != 0) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_AIRPLANE_MODE", "(1) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        notAllowedShizuku = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_AIRPLANE_MODE", "(2) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI(
                  Profile profile, SharedPreferences sharedPreferences/*, boolean fromUIThread*/) {

        if (PPApplication.HAS_FEATURE_WIFI) {
            // device has Wifi
            preferenceAllowed = PREFERENCE_ALLOWED;

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
                if (isShiuzkuGranted(true) == 1) {
                    preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                if (isRooted() == 1) {
                    // shizuku is not granted but device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        /*if ((profile._deviceWiFi == 6) ||
                                (profile._deviceWiFi == 7) ||
                                (profile._deviceWiFi == 8)) {*/
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
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
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                            }
                            //}
                        }
                } else {
                    if (profile != null) {
                        if (profile._deviceWiFi != 0) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_BLUETOOTH() {

        if (PPApplication.HAS_FEATURE_BLUETOOTH)
            // device has bluetooth
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA(
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, /*boolean fromUIThread,*/ Context context) {
//        PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "*******************");

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
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
            }
            else
            if (isShiuzkuGranted(true) == 1) {
                // not needed, used is "svc data enable/disable"
                /*if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (serviceBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._deviceMobileData != 0)
                                allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        allowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }
                } else {
                    allowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }*/

                preferenceAllowed = PREFERENCE_ALLOWED;

                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);

//                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM1="+hasSIMCardData.hasSIM1);
//                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM2="+hasSIMCardData.hasSIM2);

                    boolean sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    if (!sim0Exists) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceMobileData != 0)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                // not needed, used is "svc data enable/disable"
                /*if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (serviceBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._deviceMobileData != 0)
                                allowed = PREFERENCE_ALLOWED;
                        }
                        else
                            allowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        allowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                    }
                } else {
                    allowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }*/

                preferenceAllowed = PREFERENCE_ALLOWED;

                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
//                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);

//                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM1="+hasSIMCardData.hasSIM1);
//                    PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "hasSIM2="+hasSIMCardData.hasSIM2);

//                    boolean sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    boolean sim0Exists = telephonyManager.getPhoneCount() > 0;
                    if (!sim0Exists) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else {
                if (profile != null) {
                    if (profile._deviceMobileData != 0) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "(1) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        notAllowedShizuku = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA", "(2) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        }
        else {
            //Log.d("Profile.isProfilePreferenceAllowed", "mobile data not supported");
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    /*
    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA_DUAL_SIM(
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
                                allowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed.allowed = PREFERENCE_ALLOWED;
                } else if (isRooted() == 1) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if ((profile._deviceMobileDataSIM1 != 0) ||
                                (profile._deviceMobileDataSIM2 != 0)) {
                            if (applicationNeverAskForGrantRoot) {
                                allowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                allowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                        if (serviceBinaryExists(fromUIThread) == 1) {
                            if (profile != null) {
                                if ((profile._deviceMobileDataSIM1 != 0) ||
                                        (profile._deviceMobileDataSIM2 != 0))
                                    allowed = PREFERENCE_ALLOWED;
                            } else
                                allowed = PREFERENCE_ALLOWED;
                        } else {
                            allowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        }
                    } else {
                        allowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            allowed = PREFERENCE_ALLOWED;
                            //if (!sim1Exists) {
                            //    allowed = PREFERENCE_NOT_ALLOWED;
                            //    notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            //}
                            //if (!sim2Exists) {
                            //    allowed = PREFERENCE_NOT_ALLOWED;
                            //    notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            //}
                        } else {
                            allowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        allowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else {
                    allowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                    if ((profile != null) &&
                            ((profile._deviceMobileDataSIM1 != 0) ||
                             (profile._deviceMobileDataSIM2 != 0))) {
                        notAllowedRoot = true;
                    }
                }
            } else {
                //Log.d("Profile.isProfilePreferenceAllowed", "mobile data not supported");
                allowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
    }
    */

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS() {
        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            preferenceAllowed = PREFERENCE_ALLOWED;
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_GPS(
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
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else if (isRooted() == 1) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._deviceGPS != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                return;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._deviceGPS != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                }
                /*else
                if (ActivateProfileHelper.canExploitGPS(appContext))
                {
                    allowed = PREFERENCE_ALLOWED;
                }*/
                else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                    if ((profile != null) && (profile._deviceGPS != 0)) {
                        //return preferenceAllowed;
                        //notAllowedRoot = true;
                        notAllowedG1 = true;
                    }
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        } else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_LOCATION_MODE(
            Profile profile, Context context) {

        Context appContext = context.getApplicationContext();

        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            if (profile != null) {
                if (profile._deviceLocationMode != 0)
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed = PREFERENCE_ALLOWED;
        }
            /*else
            if ((isRooted() == 1))
            {
                // device is rooted - NOT WORKING

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._deviceLocationMode != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            allowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            //return preferenceAllowed;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            allowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return preferenceAllowed;
                        }
                    }
                }

                if (PPApplication.settingsBinaryExists(fromUIThread))
                    allowed = PREFERENCE_ALLOWED;
                else {
                    allowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }*/
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            if ((profile != null) && (profile._deviceLocationMode != 0)) {
                //return preferenceAllowed;
                //notAllowedRoot = true;
                notAllowedG1 = true;
            }
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NFC(
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
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else if (isRooted() == 1) {

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._deviceNFC != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (profile != null) {
                    if (profile._deviceNFC != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._deviceNFC != 0)) {
                    //return preferenceAllowed;
                    //notAllowedRoot = true;
                    notAllowedG1 = true;
                }
            }
        }
        else
        {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }

    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_WIFI_AP;

        //if (Build.VERSION.SDK_INT < 30) {
            if (PPApplication.HAS_FEATURE_WIFI) {
                // device has Wifi
                if (Build.VERSION.SDK_INT < 28) {
                    if (canExploitWifiTethering(appContext) == 1) {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                    if (PPApplication.rootMutex.transactionCode_setWifiApEnabled != -1) {
                        if (isShiuzkuGranted(true) == 1) {
                            if (ActivateProfileHelper.wifiServiceExists(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                                if (serviceBinaryExists(fromUIThread) == 1) {
                                    if (profile != null) {
                                        if (profile._deviceWiFiAP != 0)
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                    return;
                                }
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                                return;
                            }

                            boolean sim0Exists = false;
//                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                            sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null)
                                sim0Exists = telephonyManager.getPhoneCount() > 0;
                            if (!sim0Exists) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        } else if (isRooted() == 1) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if (profile._deviceWiFiAP != 0) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        return;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (ActivateProfileHelper.wifiServiceExists(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                                if (serviceBinaryExists(fromUIThread) == 1) {
                                    if (profile != null) {
                                        if (profile._deviceWiFiAP != 0)
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                    return;
                                }
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                                return;
                            }

                            boolean sim0Exists = false;
//                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                            sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null)
                                sim0Exists = telephonyManager.getPhoneCount() > 0;
                            if (!sim0Exists) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }

                        } else {
                            if (profile != null) {
                                if (profile._deviceWiFiAP != 0) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP", "(1) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                //noinspection ConstantConditions
                                if (sharedPreferences != null) {
                                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP", "(2) Shizuku not granted");
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                        notAllowedShizuku = true;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                }
                            }
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
                //else if (Build.VERSION.SDK_INT < 30) { // Android 10 fix for not granted TETHER_PRIVILEGED
                else if (Build.VERSION.SDK_INT < 29) {
                    if (canExploitWifiTethering(appContext) == 1) {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;

                        boolean sim0Exists = false;
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null)
                            sim0Exists = telephonyManager.getPhoneCount() > 0;
                        if (!sim0Exists) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    } else {
                        //if ((profile != null) && (profile._deviceWiFiAP != 0)) {
                        //    notAllowedRoot = true;
                        //}
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                } else
                if (Build.VERSION.SDK_INT >= 36) {
                    if (isShiuzkuGranted(true) == 1) {
                        if (ActivateProfileHelper.isDeltaInstalled(context)) {
                            if (profile != null) {
                                if (profile._deviceWiFiAP != 0)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            if (profile != null) {
                                if (profile._deviceWiFiAP != 0) {
                                    notInstalledDelta = true;
                                }
                            }
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_DELTA;
                            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        }
                    } else if (isRooted() == 1) {
                        // device is rooted
                        if (ActivateProfileHelper.isDeltaInstalled(context)) {
                            if (profile != null) {
                                // test if grant root is disabled
                                if (profile._deviceWiFiAP != 0) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        return;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }
                            boolean sim0Exists = false;
//                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                            sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null)
                                //noinspection deprecation
                                sim0Exists = telephonyManager.getPhoneCount() > 0;
                            if (!sim0Exists) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        } else {
                            if (profile != null) {
                                if (profile._deviceWiFiAP != 0) {
                                    notInstalledDelta = true;
                                }
                            }
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_DELTA;
                            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        }
                    } else {
                        if (profile != null) {
                            if (profile._deviceWiFiAP != 0) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP", "(1) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            //noinspection ConstantConditions
                            if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP", "(2) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            }
                        }
                    }
                } else {
                    // this must be called first, because WifiApManager.canExploitWifiTethering30(appContext) requires SIM inserted
                    boolean sim0Exists = false;
//                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                    sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null)
                        //noinspection deprecation
                        sim0Exists = telephonyManager.getPhoneCount() > 0;
                    if (!sim0Exists) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    } else {
                        if (canExploitWifiTethering30(appContext) == 1) {
                            if (profile != null) {
                                if (profile._deviceWiFiAP != 0)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            //if ((profile != null) && (profile._deviceWiFiAP != 0)) {
                            //    notAllowedRoot = true;
                            //}
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        }
                    }
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        //}
        //else {
        //    allowed = PREFERENCE_NOT_ALLOWED;
        //    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
        //    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_android_version);
        //}

    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_WHEN_RINGING(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING;

        if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) || PPApplication.deviceIsOnePlus) {
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._vibrateWhenRinging != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile._vibrateWhenRinging != 0) {
                            boolean enabled = false;
                            if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 4))
                                enabled = true;
                            if (profile._volumeRingerMode == 5) {
                                if ((profile._volumeZenMode == 1) || (profile._volumeZenMode == 2))
                                    enabled = true;
                            }
                            if (enabled) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_WHEN_RINGING", "(1) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            String value = sharedPreferences.getString(preferenceKey, "0");
                            if (!value.equals("0")) {
                                String ringerMode = sharedPreferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                                boolean enabled = false;
                                if (ringerMode.equals("1") || ringerMode.equals("4"))
                                    enabled = true;
                                if (ringerMode.equals("5")) {
                                    String zenMode = sharedPreferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
                                    if (zenMode.equals("1") || zenMode.equals("2"))
                                        enabled = true;
                                }
                                if (enabled) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_WHEN_RINGING", "(2) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._vibrateWhenRinging != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._vibrateWhenRinging != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }
            else if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile._vibrateWhenRinging != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
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
                        notAllowedPPPPS = true;
                    }
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            if (profile != null) {
                if (profile._vibrateWhenRinging != 0)
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed = PREFERENCE_ALLOWED;
        }
    }


    void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_NOTIFICATIONS(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {

            if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                    (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                    (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                    PPApplication.deviceIsOnePlus) {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            }
            else {

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS;

                if (isShiuzkuAvailable() == 1) {
                    if (isShiuzkuGranted(false) == 1) {
                        if (settingsBinaryExists(fromUIThread) == 1) {
                            if (profile != null) {
                                if (profile._vibrateNotifications != 0)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                        }
                    } else {
                        if (profile != null) {
                            if (profile._vibrateNotifications != 0) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_NOTIFICATIONS", "(1) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            //noinspection ConstantConditions
                            if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATE_NOTIFICATIONS", "(2) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            }
                        }
                    }
                } else
                if (isRooted() == 1) {
                    // device is rooted

                    if (profile != null) {
                        // test if grant root is disabled
                        if (profile._vibrateNotifications != 0) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                return;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._vibrateNotifications != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else
                if (isPPPPSInstalled(context) == 1) {
                    if (profile != null) {
                        if (profile._vibrateNotifications != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    if ((profile != null) && (profile._vibrateNotifications != 0)) {
                        notAllowedPPPPS = true;
                    }
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                }
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    void isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(
            Context context) {

        Context appContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT < 29) {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);

        } else
        if ((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
            (PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT < 33)) ||
            (PPApplication.deviceIsOnePlus && (Build.VERSION.SDK_INT < 31))) {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        } else {
            preferenceAllowed = PREFERENCE_ALLOWED;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_RINGING(
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(this);
        _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);
        if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile.getVibrationIntensityRingingChange())
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile.getVibrationIntensityRingingChange())
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile.getVibrationIntensityRingingChange()) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_RINGING", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING;
                            String value = sharedPreferences.getString(preferenceKey, "0");
                            if (ProfileStatic.getVibrationIntensityChange(value)) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_RINGING", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityRingingChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityRingingChange())
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityRingingChange()) {
                    notAllowedPPPPS = true;
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        } else {
            copyFrom(_preferenceAllowed);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS(
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(this);
        _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);
//        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS", "_preferenceAllowed.allowed="+_preferenceAllowed.allowed);
        if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile.getVibrationIntensityNotificationsChange())
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile.getVibrationIntensityNotificationsChange())
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile.getVibrationIntensityNotificationsChange()) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS;
                            String value = sharedPreferences.getString(preferenceKey, "0");
                            if (ProfileStatic.getVibrationIntensityChange(value)) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityNotificationsChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityNotificationsChange())
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityNotificationsChange()) {
                    notAllowedPPPPS = true;
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            copyFrom(_preferenceAllowed);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION(
            Profile profile, SharedPreferences sharedPreferences,
            boolean fromUIThread, Context context) {

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.copyFrom(this);
        _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);
        if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile.getVibrationIntensityTouchInteractionChange())
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile.getVibrationIntensityTouchInteractionChange())
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile.getVibrationIntensityTouchInteractionChange()) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION;
                            String value = sharedPreferences.getString(preferenceKey, "0");
                            if (ProfileStatic.getVibrationIntensityChange(value)) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                String preferenceKey = Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION;

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile.getVibrationIntensityTouchInteractionChange()) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (ProfileStatic.getVolumeChange(sharedPreferences.getString(preferenceKey, "-1|1"))) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile.getVibrationIntensityTouchInteractionChange())
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && profile.getVibrationIntensityTouchInteractionChange()) {
                    notAllowedPPPPS = true;
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
            copyFrom(_preferenceAllowed);
        }
    }

    /*
    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS(
            SharedPreferences sharedPreferences, boolean fromUIThread) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (isRooted() == 1) {
            // device is rooted

            if (sharedPreferences != null) {
                String value = sharedPreferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
                if (ProfileStatic.getDeviceBrightnessChange(value) && ProfileStatic.getDeviceBrightnessAutomatic(value)) {
                    if (applicationNeverAskForGrantRoot) {
                        allowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (settingsBinaryExists(fromUIThread) == 1) {
                allowed = PREFERENCE_ALLOWED;
            }
            else {
                allowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        } else {
            allowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
        }
    }
    */

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_POWER_SAVE_MODE(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE;

        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            if (profile != null) {
                if (profile._devicePowerSaveMode != 0)
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed = PREFERENCE_ALLOWED;
        }
        else
        if (isRooted() == 1) {
            // device is rooted

            if (profile != null) {
                // test if grant root is disabled
                if (profile._devicePowerSaveMode != 0) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        return;
                    }
                }
            }
            else
            if (sharedPreferences != null) {
                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        // not needed to test all parameters
                        return;
                    }
                }
            }

            if (settingsBinaryExists(fromUIThread) == 1) {
                if (profile != null) {
                    if (profile._devicePowerSaveMode != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
            if ((profile != null) && (profile._devicePowerSaveMode != 0)) {
                //notAllowedRoot = true;
                notAllowedG1 = true;
            }
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE(
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {

                final int phoneType = telephonyManager.getPhoneType();
                if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                    if (isShiuzkuGranted(true) == 1) {
                        if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (serviceBinaryExists(fromUIThread) == 1) {
                                if (profile == null)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                                else {
                                    if (profile._deviceNetworkType != 0)
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                }
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                return;
                            }
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            return;
                        }

                        boolean sim0Exists;
//                        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE", "called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        sim0Exists = telephonyManager.getPhoneCount() > 0;
                        if (!sim0Exists) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    } else
                    if (isRooted() == 1) {
                        // device is rooted

                        if (profile != null) {
                            // test if grant root is disabled
                            if ((profile._deviceNetworkType != 0)) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    return;
                                }
                            }
                        }
                        else
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                if (applicationNeverAskForGrantRoot) {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                    // not needed to test all parameters
                                    return;
                                }
                            }
                        }

                        if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (serviceBinaryExists(fromUIThread) == 1) {
                                if (profile == null)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                                else {
                                    if (profile._deviceNetworkType != 0)
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                }
                            }
                            else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                return;
                            }
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            return;
                        }

                        boolean sim0Exists;
//                        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE", "called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        sim0Exists = telephonyManager.getPhoneCount() > 0;
                        if (!sim0Exists) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    } else {
                        if (profile != null) {
                            if (profile._deviceNetworkType != 0) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE", "(1) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            //noinspection ConstantConditions
                            if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE", "(2) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            }
                        }
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_DUAL_SIM(
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
                        if (isShiuzkuGranted(true) == 1) {
                            if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                                if (serviceBinaryExists(fromUIThread) == 1) {
                                    if (profile == null)
                                        //noinspection UnusedAssignment
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                    else {
                                        if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                                (profile._deviceNetworkTypeSIM2 != 0))
                                            //noinspection UnusedAssignment
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    }

                                    if (phoneCount > 1) {
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                                    }
                                } else {
                                    //noinspection UnusedAssignment
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            }
                        } else
                        if (isRooted() == 1) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                        (profile._deviceNetworkTypeSIM2 != 0)
                                ) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        return;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                                if (serviceBinaryExists(fromUIThread) == 1) {
                                    if (profile == null)
                                        //noinspection UnusedAssignment
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                    else {
                                        if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                                (profile._deviceNetworkTypeSIM2 != 0))
                                            //noinspection UnusedAssignment
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    }

                                    if (phoneCount > 1) {
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                                    }
                                } else {
                                    //noinspection UnusedAssignment
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                }
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                            }
                        } else {
                            if (profile != null) {
                                if ((profile._deviceNetworkTypeSIM1 != 0) ||
                                        (profile._deviceNetworkTypeSIM2 != 0)) {
//                                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_DUAL_SIM", "(1) Shizuku not granted");
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                    notAllowedShizuku = true;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                //noinspection ConstantConditions
                                if (sharedPreferences != null) {
                                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_DUAL_SIM", "(2) Shizuku not granted");
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                        notAllowedShizuku = true;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                }
                            }
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

//        Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "xxx");

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_NOTIFICATION_LED;

        int value = Settings.System.getInt(appContext.getContentResolver(), "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/, -10);
        if (value != -10) {
            if (isPPPPSInstalled(context) == 1) {
//                Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "installed");
                if (profile != null) {
                    if (profile._notificationLed != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._notificationLed != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile._notificationLed != 0) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._notificationLed != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._notificationLed != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
//                Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(2)");
                if ((profile != null) && (profile._notificationLed != 0)) {
                    notAllowedPPPPS = true;
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else {
//            Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_NOTIFICATION_LED", "(3)");
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_KEYGUARD(
            Context context) {

        Context appContext = context.getApplicationContext();

        boolean secureKeyguard;
        KeyguardManager keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            secureKeyguard = keyguardManager.isKeyguardSecure();
            if (secureKeyguard) {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
            } else
                preferenceAllowed = PREFERENCE_ALLOWED;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_CONNECT_TO_SSID() {

        if (PPApplication.HAS_FEATURE_WIFI)
            // device has Wifi
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING() {

        if (PPApplication.HAS_FEATURE_WIFI)
            // device has Wifi
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING() {

        if (PPApplication.HAS_FEATURE_BLUETOOTH)
            // device has bluetooth
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_WIFI_AP_PREFS() {

        if (PPApplication.HAS_FEATURE_WIFI)
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING() {

        if (PPApplication.HAS_FEATURE_TELEPHONY)
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING() {

        if (PPApplication.sensorManager != null) {
            boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
            //boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
            boolean hasProximity = PPApplication.proximitySensor != null;
            boolean hasLight = PPApplication.lightSensor != null;

            if (hasAccelerometer || hasProximity || hasLight)
                preferenceAllowed = PREFERENCE_ALLOWED;
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_HEADS_UP_NOTIFICATIONS(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS;

        int value = Settings.Global.getInt(appContext.getContentResolver(), "heads_up_notifications_enabled", -10);
        if (value != -10) {
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._headsUpNotifications != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._headsUpNotifications != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._headsUpNotifications != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                    else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._headsUpNotifications != 0)) {
                    //return preferenceAllowed;
                    //notAllowedRoot = true;
                    notAllowedG1 = true;
                }
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS() {

        if (PPApplication.HAS_FEATURE_TELEPHONY)
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VOLUME_ACCESSIBILITY(/*,
            Context context*/) {

        //Context appContext = context.getApplicationContext();

        preferenceAllowed = PREFERENCE_ALLOWED;
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_ALWAYS_ON_DISPLAY(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY;

            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._headsUpNotifications != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._alwaysOnDisplay != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._alwaysOnDisplay != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._alwaysOnDisplay != 0)) {
                    //return preferenceAllowed;
                    //notAllowedRoot = true;
                    notAllowedG1 = true;
                }
            }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_DARK_MODE(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_SCREEN_DARK_MODE;

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._screenDarkMode != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            }
            else
            if (isRooted() == 1)
            {
                // device is rooted
                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._screenDarkMode != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                }
                else
                if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._screenDarkMode != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                    else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._screenDarkMode != 0)) {
                    //return preferenceAllowed;
                    notAllowedG1 = true;
                }
            }
        }
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_old_android);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT(
                            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {
        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT;

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
            //allowed = PREFERENCE_NOT_ALLOWED;
            //notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;

            if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile._screenNightLight != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._screenNightLight != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile._screenNightLight != 0) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._screenNightLight != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._screenNightLight != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if ((profile != null) && (profile._screenNightLight != 0)) {
                    notAllowedPPPPS = true;
                }
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
            }
        }
        else
        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
            if (isShiuzkuAvailable() == 1) {
                if (isShiuzkuGranted(false) == 1) {
                    if (settingsBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if (profile._screenNightLight != 0)
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                } else {
                    if (profile != null) {
                        if (profile._screenNightLight != 0) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(3) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(4) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            }
            else if (isRooted() == 1) {
                // shizuku is not granted but device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._screenNightLight != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._screenNightLight != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                if (profile != null) {
                    if (profile._screenNightLight != 0) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(5) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        notAllowedShizuku = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT", "(6) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        }
        else
        if (!((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) /*||
                (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy)*/)) {
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                if (profile != null) {
                    if (profile._screenNightLight != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else if (isRooted() == 1) {
                // device is rooted
                if (profile != null) {
                    // test if grant root is disabled
                    if (profile._screenNightLight != 0) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (settingsBinaryExists(fromUIThread) == 1) {
                    if (profile != null) {
                        if (profile._screenNightLight != 0)
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION;
                if ((profile != null) && (profile._screenNightLight != 0)) {
                    //return preferenceAllowed;
                    notAllowedG1 = true;
                }
            }
        } else {
            if (profile != null) {
                if (profile._screenNightLight != 0)
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
                preferenceAllowed = PREFERENCE_ALLOWED;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS() {
        if ((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                PPApplication.deviceIsOnePlus) {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
            preferenceAllowed = PREFERENCE_ALLOWED;
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_VOLUME_SPEAKER_PHONE(
            Context context) {

        Context appContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT < 29)
            preferenceAllowed = PREFERENCE_ALLOWED;
        else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_android_version);
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_CAMERA_FLASH(/*, Context context*/) {
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
            preferenceAllowed = PREFERENCE_ALLOWED;
        } else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS(
            Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        String preferenceKey = Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS;

            //if (PPApplication.HAS_FEATURE_TELEPHONY) {
            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
//                        PPApplicationStatic.logE("[DUAL_SIM] Profile.isProfilePreferenceAllowed", "phoneCount="+phoneCount);

                if (isShiuzkuGranted(true) == 1) {
                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                        if (phoneCount > 1) {
                            if (serviceBinaryExists(fromUIThread) == 1) {
                                if (profile != null) {
                                    if (!profile._deviceDefaultSIMCards.equals("0|0|0"))
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                return;
                            }
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                            return;
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        return;
                    }

                    boolean sim0Exists;
//                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                    sim0Exists = hasSIMCardData.simCount > 1;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    sim0Exists = telephonyManager.getPhoneCount() > 0;
                    if (!sim0Exists) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                    }
                } else
                if (isRooted() == 1) {
                    // device is rooted
                    if (profile != null) {
                        // test if grant root is disabled
                        if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                return;
                            }
                        }
                    } else if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0|0|0").equals("0|0|0")) {
                            if (applicationNeverAskForGrantRoot) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                // not needed to test all parameters
                                return;
                            }
                        }
                    }

                    if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                        if (phoneCount > 1) {
                            if (serviceBinaryExists(fromUIThread) == 1) {
                                if (profile != null) {
                                    if (!profile._deviceDefaultSIMCards.equals("0|0|0"))
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                                return;
                            }
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                            return;
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        return;
                    }

                    boolean sim0Exists;
//                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                    sim0Exists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    sim0Exists = telephonyManager.getPhoneCount() > 0;
                    if (!sim0Exists) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }

                } else {
                    if (profile != null) {
                        if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS", "(1) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        //noinspection ConstantConditions
                        if (sharedPreferences != null) {
                            if (!sharedPreferences.getString(preferenceKey, "0").equals("0|0|0")) {
//                                PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS", "(2) Shizuku not granted");
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                notAllowedShizuku = true;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                    }
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            }
            //} else {
            //    allowed = PREFERENCE_NOT_ALLOWED;
            //    notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            //}
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM(
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences, boolean fromUIThread, Context context) {

        Context appContext = context.getApplicationContext();

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        if (Build.VERSION.SDK_INT >= 29) {
            if (isShiuzkuGranted(true) == 1) {
                if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)) {
                    if (serviceBinaryExists(fromUIThread) == 1) {
                        //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(0) PREFERENCE_ALLOWED");
                        if (profile != null) {
                            if ((profile._deviceOnOffSIM1 != 0) ||
                                    (profile._deviceOnOffSIM2 != 0))
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(1) PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND");
                        return;
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            preferenceAllowed = PREFERENCE_ALLOWED;
                            //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(2) PREFERENCE_ALLOWED");
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                            //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(3) PREFERENCE_NOT_ALLOWED_NO_HARDWARE");
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                        //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(4) PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM");
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(5) PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM");
                }
            } else
            if (isRooted() == 1) {
                // device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceOnOffSIM1 != 0) ||
                            (profile._deviceOnOffSIM2 != 0)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }

                if (ActivateProfileHelper.telephonyServiceExists(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)) {
                    if (serviceBinaryExists(fromUIThread) == 1) {
                        if (profile != null) {
                            if ((profile._deviceOnOffSIM1 != 0) ||
                                    (profile._deviceOnOffSIM2 != 0))
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                    else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        return;
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            preferenceAllowed = PREFERENCE_ALLOWED;
                        } else {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }

                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            } else {
                if (profile != null) {
                    if ((profile._deviceOnOffSIM1 != 0) ||
                            (profile._deviceOnOffSIM2 != 0)) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(1) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        notAllowedShizuku = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(2) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        } else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
            //Log.e("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_ONOFF_SIM", "(6) PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM");
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM(
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
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        }
                        else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_by_ppp);
            }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM(
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
                            ppppsInstalled = isPPPPSInstalled(context) == 1;
                        }
                        if (isShiuzkuAvailable() == 1) {
                            if (isShiuzkuGranted(false) == 1) {
                                if (settingsBinaryExists(fromUIThread) == 1) {
                                    if (profile != null) {
                                        if ((profile._soundNotificationChangeSIM1 != 0) ||
                                                (profile._soundNotificationChangeSIM2 != 0))
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                                }
                            } else {
                                if (profile != null) {
                                    if ((profile._soundNotificationChangeSIM1 != 0) ||
                                            (profile._soundNotificationChangeSIM2 != 0)) {
//                                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM", "(1) Shizuku not granted");
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                        notAllowedShizuku = true;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    //noinspection ConstantConditions
                                    if (sharedPreferences != null) {
                                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM", "(2) Shizuku not granted");
                                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                            notAllowedShizuku = true;
                                        } else
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    }
                                }
                            }
                        } else
                        if (isRooted() == 1) {
                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._soundNotificationChangeSIM1 != 0) ||
                                        (profile._soundNotificationChangeSIM2 != 0)) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        return;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            // device is rooted
                            if (settingsBinaryExists(fromUIThread) == 1) {
                                if (profile != null) {
                                    if ((profile._soundNotificationChangeSIM1 != 0) ||
                                            (profile._soundNotificationChangeSIM2 != 0))
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                            }
                        } else {
                            if (rootRequired) {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                            } else {
                                if (ppppsInstalled) {
                                    if (profile != null) {
                                        if ((profile._soundNotificationChangeSIM1 != 0) ||
                                                (profile._soundNotificationChangeSIM2 != 0))
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                }
                                else {
                                    if (profile != null) {
                                        if ((profile._soundNotificationChangeSIM1 != 0) ||
                                                (profile._soundNotificationChangeSIM2 != 0)) {
                                            notAllowedPPPPS = true;
                                        }
                                    }
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                                }
                            }
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
            if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                PPApplication.deviceIsOnePlus) {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported);
            }
            else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported_by_ppp);
            }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS(
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
                        if (isPPPPSInstalled(context) == 1) {
                            if (profile != null) {
                                if (profile._soundSameRingtoneForBothSIMCards != 0)
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else
                                preferenceAllowed = PREFERENCE_ALLOWED;
                        } else
                        if (isShiuzkuAvailable() == 1) {
                            if (isShiuzkuGranted(true) == 1) {
                                if (settingsBinaryExists(fromUIThread) == 1) {
                                    if (profile != null) {
                                        if (profile._soundSameRingtoneForBothSIMCards != 0)
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                    notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                                }
                            } else {
                                if (profile != null) {
                                    if (profile._soundSameRingtoneForBothSIMCards != 0) {
//                                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS", "(1) Shizuku not granted");
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                        notAllowedShizuku = true;
                                    } else
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else {
                                    //noinspection ConstantConditions
                                    if (sharedPreferences != null) {
                                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS", "(2) Shizuku not granted");
                                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                                            notAllowedShizuku = true;
                                        } else
                                            preferenceAllowed = PREFERENCE_ALLOWED;
                                    }
                                }
                            }
                        } else
                        if (isRooted() == 1) {
                            // device is rooted

                            if (profile != null) {
                                // test if grant root is disabled
                                if ((profile._soundSameRingtoneForBothSIMCards != 0)) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        return;
                                    }
                                }
                            } else if (sharedPreferences != null) {
                                if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                                    if (applicationNeverAskForGrantRoot) {
                                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                                        // not needed to test all parameters
                                        return;
                                    }
                                }
                            }

                            if (settingsBinaryExists(fromUIThread) == 1) {
                                if (profile != null) {
                                    if (profile._soundSameRingtoneForBothSIMCards != 0)
                                        preferenceAllowed = PREFERENCE_ALLOWED;
                                } else
                                    preferenceAllowed = PREFERENCE_ALLOWED;
                            } else {
                                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                            }
                        } else {
                            if ((profile != null) && (profile._soundSameRingtoneForBothSIMCards != 0)) {
                                notAllowedPPPPS = true;
                            }
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                        }
                    } else {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    }
                } else {
                    preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_not_supported);
            }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_LOCK_DEVICE(
                                Profile profile, SharedPreferences sharedPreferences) {

        preferenceAllowed = PREFERENCE_ALLOWED;

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
            if (isRooted() == 1) {
                // shizuku is not granted but device is rooted

                if (profile != null) {
                    // test if grant root is disabled
                    /*if ((profile._deviceWiFi == 6) ||
                            (profile._deviceWiFi == 7) ||
                            (profile._deviceWiFi == 8)) {*/
                    if (applicationNeverAskForGrantRoot) {
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
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
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                        }
                        //}
                    }
            } else {
                preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                if ((profile != null) && (profile._deviceWiFi != 0)) {
                    notAllowedRoot = true;
                }
            }
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SCREEN_ON_OFF() {
        preferenceAllowed = PREFERENCE_ALLOWED;
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_SCREEN_TIMEOUT(
            String preferenceKey, Profile profile, SharedPreferences sharedPreferences/*, boolean fromUIThread*/, Context context) {
//        PPApplicationStatic.logE("[DUAL_SIM] PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_SCREEN_TIMEOUT", "*******************");

//        Context appContext = context.getApplicationContext();

        if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme ||
                (PPApplication.deviceIsOnePlus && (Build.VERSION.SDK_INT < 34))) {
            if (isPPPPSInstalled(context) == 1) {
                if (profile != null) {
                    if (profile.getVibrationIntensityNotificationsChange())
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else
            if (isShiuzkuGranted(true) == 1) {
                preferenceAllowed = PREFERENCE_ALLOWED;
            } else if (isRooted() == 1) {
                // device is rooted

                boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceScreenTimeout != 0)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else if (sharedPreferences != null) {
                    if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                        }
                    }
                } else
                    preferenceAllowed = PREFERENCE_ALLOWED;
            } else {
                if (profile != null) {
                    if (profile._deviceScreenTimeout != 0) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_SCREEN_TIMEOUT", "(1) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                        notAllowedPPPPS = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (!sharedPreferences.getString(preferenceKey, "0").equals("0")) {
//                            PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_SCREEN_TIMEOUT", "(2) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS;
                            notAllowedPPPPS = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        } else {
            preferenceAllowed = PREFERENCE_ALLOWED;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_SEND_SMS() {
        //if (PPApplication.HAS_FEATURE_TELEPHONY_MESSAGING)
        //{
            preferenceAllowed = PREFERENCE_ALLOWED;
        //}
        //else {
        //    allowed = PREFERENCE_NOT_ALLOWED;
        //    notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        //}
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED(Context context) {
        if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false)) {
            preferenceAllowed = PREFERENCE_ALLOWED;
        } else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
        }
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION(
                String preferenceKey, Profile profile, SharedPreferences sharedPreferences) {

        boolean applicationNeverAskForGrantRoot = ApplicationPreferences.applicationNeverAskForGrantRoot;

        boolean rootRequired = false;
        if (profile != null)
            rootRequired = profile._deviceForceStopApplicationChange == 2;
        else
        if ((sharedPreferences != null) && (preferenceKey != null))
            rootRequired = sharedPreferences.getString(preferenceKey, "0").equals("2");

        if (rootRequired) {
            if (isShiuzkuGranted(true) == 1) {
                if (profile == null)
                    preferenceAllowed = PREFERENCE_ALLOWED;
                else {
                    if (profile._deviceNetworkType != 0)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
            } else if (isRooted() == 1) {
                // device is rooted
                if (profile != null) {
                    // test if grant root is disabled
                    if ((profile._deviceForceStopApplicationChange == 2)) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            return;
                        }
                    }
                } else
                //noinspection ConstantValue
                if (sharedPreferences != null) {
                    if (sharedPreferences.getString(preferenceKey, "0").equals("2")) {
                        if (applicationNeverAskForGrantRoot) {
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED;
                            // not needed to test all parameters
                            return;
                        }
                    }
                }
                if (profile == null)
                    preferenceAllowed = PREFERENCE_ALLOWED;
                else {
                    if (profile._deviceForceStopApplicationChange == 2)
                        preferenceAllowed = PREFERENCE_ALLOWED;
                }
            } else {
                if (profile != null) {
                    if (profile._deviceForceStopApplicationChange == 2) {
//                    PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION", "(1) Shizuku not granted");
                        preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                        notAllowedShizuku = true;
                    } else
                        preferenceAllowed = PREFERENCE_ALLOWED;
                } else {
                    //noinspection ConstantConditions
                    if (sharedPreferences != null) {
                        if (sharedPreferences.getString(preferenceKey, "0").equals("2")) {
//                        PPApplicationStatic.logE("PreferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION", "(2) Shizuku not granted");
                            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED;
                            notAllowedShizuku = true;
                        } else
                            preferenceAllowed = PREFERENCE_ALLOWED;
                    }
                }
            }
        } else
            preferenceAllowed = PREFERENCE_ALLOWED;
    }

    void isProfilePreferenceAllowed_PREF_PROFILE_PLAY_MUSIC(Context context) {
        if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false)) {
            preferenceAllowed = PREFERENCE_ALLOWED;
        } else {
            preferenceAllowed = PREFERENCE_NOT_ALLOWED;
        }
    }

}
