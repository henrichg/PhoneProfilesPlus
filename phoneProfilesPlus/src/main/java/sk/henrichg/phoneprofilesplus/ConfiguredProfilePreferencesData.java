package sk.henrichg.phoneprofilesplus;

class ConfiguredProfilePreferencesData {

    int preferenceIcon;
    int preferenceIcon2;
    boolean preferenceIconDisabled;
    boolean preferenceIcon2Disabled;
    String preferenceString;
    String preferenceDecription;

    // constructor is required for GSon !!!
    @SuppressWarnings("unused")
    ConfiguredProfilePreferencesData() {
    }

    ConfiguredProfilePreferencesData(int preferenceIcon, int preferenceIcon2,
                                     boolean preferenceIconDisabled, boolean preferenceIcon2Disabled,
                                     String preferenceString, String preferenceDecription)
    {
        this.preferenceIcon = preferenceIcon;
        this.preferenceIcon2 = preferenceIcon2;
        this.preferenceIconDisabled = preferenceIconDisabled;
        this.preferenceIcon2Disabled = preferenceIcon2Disabled;
        this.preferenceString = preferenceString;
        this.preferenceDecription = preferenceDecription;
    }

}
