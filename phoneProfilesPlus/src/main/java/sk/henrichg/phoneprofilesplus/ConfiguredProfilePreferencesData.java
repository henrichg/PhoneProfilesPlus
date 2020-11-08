package sk.henrichg.phoneprofilesplus;

class ConfiguredProfilePreferencesData {

    int preferenceIcon;
    int preferenceIcon2;
    String preferenceString;
    String preferenceDecription;

    // constructor is required for GSon !!!
    @SuppressWarnings("unused")
    ConfiguredProfilePreferencesData() {
    }

    ConfiguredProfilePreferencesData(int preferenceIcon, int preferenceIcon2, String preferenceString, String preferenceDecription)
    {
        this.preferenceIcon = preferenceIcon;
        this.preferenceIcon2 = preferenceIcon2;
        this.preferenceString = preferenceString;
        this.preferenceDecription = preferenceDecription;
    }

}
