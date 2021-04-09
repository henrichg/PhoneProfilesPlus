package sk.henrichg.phoneprofilesplus;

class RootMutex {
    boolean rootChecked;
    boolean rooted;
    //boolean grantRootChecked;
    //boolean rootGranted;
    boolean settingsBinaryChecked;
    boolean settingsBinaryExists;
    //boolean isSELinuxEnforcingChecked;
    //boolean isSELinuxEnforcing;
    //String suVersion;
    //boolean suVersionChecked;
    boolean serviceBinaryChecked;
    boolean serviceBinaryExists;
    Object serviceManagerPhone;
    Object serviceManagerWifi;
    Object serviceManagerIsub;

    int transactionCode_setUserDataEnabled;
    int transactionCode_setDataEnabled;
    int transactionCode_setPreferredNetworkType;
    int transactionCode_setDefaultVoiceSubId;
    int transactionCode_setDefaultSmsSubId;
    int transactionCode_setDefaultDataSubId;
    int transactionCode_setSubscriptionEnabled;
    int transactionCode_setWifiApEnabled;

}
