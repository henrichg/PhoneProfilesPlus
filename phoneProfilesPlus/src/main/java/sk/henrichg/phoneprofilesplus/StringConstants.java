package sk.henrichg.phoneprofilesplus;

class StringConstants {
    static final String PHONE_PROFILES_PLUS = "PhoneProfilesPlus";
    static final String AUTHOR_EMAIL = "henrich.gron@gmail.com";

    static final String CHAR_HARD_SPACE = "\u00A0";
    static final String CHAR_HARD_SPACE_HTML = "&nbsp;";
    static final String CHAR_SQUARE_HTML = "&#x25a0;";
    static final String CHAR_QUOTE_HTML = "&quot;";
    static final String CHAR_ARROW = "»";
    static final String STR_DOUBLE_ARROW = "»»";
    static final String STR_ARROW_INDICATOR = "[»]";
    static final String STR_DOUBLE_ARROW_INDICATOR = "[»»]";
    static final String CHAR_BULLET = "•";
    static final String STR_BULLET = " "+ CHAR_BULLET +" ";
    //static final String STR_BULLET_HTML = " <b>"+ CHAR_BULLET +"</b> ";
    static final String STR_HARD_SPACE_DOUBLE_ARROW = CHAR_HARD_SPACE + STR_DOUBLE_ARROW;
    static final String STR_HARD_SPACE_DOUBLE_ARROW_HTML = CHAR_HARD_SPACE_HTML + STR_DOUBLE_ARROW;
    static final String STR_HARD_SPACE_DASH = CHAR_HARD_SPACE + "-" + CHAR_HARD_SPACE;
    static final String STR_COLON_WITH_SPACE = ": ";
    //static final String STR_ROOT = "(R) ";
    //static final String STR_PPPPS = "(S) ";
    //static final String STR_PPPPS_ROOT = "(S)(R) ";
    static final String STR_PPPPS_SHIZUKU_ROOT = "(S)(Z)(R) ";
    static final String STR_SHIZUKU_ROOT = "(Z)(R) ";
    //static final String STR_INTERFACE = "(I) ";
    //static final String STR_G1 = "(G1) ";
    static final String CHAR_NEW_LINE = "\n";
    static final String STR_NEWLINE_WITH_SPACE = " \n";
    static final String STR_NEWLINE_WITH_COLON = ":\n";
    static final String STR_DOUBLE_NEWLINE = "\n\n";
    static final String STR_DOUBLE_NEWLINE_WITH_SPACE = " \n\n";
    static final String STR_DOUBLE_NEWLINE_WITH_DOT = ".\n\n";

    //∙∙∙∙∙
    //◦◦◦◦◦
    //•••••
    static final String STR_SEPARATOR_LINE = "\n◦ ◦ ◦ ◦ ◦\n";
    //static final String STR_SEPARATOR_WITH_SPACE = " \n◦ ◦ ◦ ◦ ◦\n";
    static final String STR_SEPARATOR_WITH_DOT = ".\n◦ ◦ ◦ ◦ ◦\n";

    static final String STR_MANUAL = "[M]";
    static final String STR_MANUAL_SPACE = "[M] ";
    static final String STR_FORMAT_INT = "%X";
    static final String STR_SPLIT_REGEX = "\\|";
    static final String STR_SPLIT_CONTACTS_REGEX = "~#~";
    static final String TAG_BREAK_HTML = "<br>";
    static final String TAG_DOUBLE_BREAK_HTML = "<br><br>";

    static final String TAG_SEPARATOR_BREAK_HTML = "<br>◦ ◦ ◦ ◦ ◦<br>";

    static final String TAG_BOLD_START_HTML = "<b>";
    static final String TAG_BOLD_END_HTML = "</b>";
    static final String TAG_BOLD_END_WITH_SPACE_HTML = "</b> ";
    static final String TAG_FONT_COLOR_HTML = "<font color=\"#%s\">%s</font>";
    static final String TAG_LIST_START_HTML = "<ul>";
    static final String TAG_LIST_END_HTML = "</ul>";
    static final String TAG_LIST_START_FIRST_ITEM_HTML = "<ul><li>";
    static final String TAG_LIST_END_LAST_ITEM_HTML = "</li></ul>";
    //static final String TAG_NUMBERED_LIST_START_HTML = "<ol>";
    //static final String TAG_NUMBERED_LIST_END_HTML = "</ol>";
    static final String TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML = "<ol><li>";
    static final String TAG_NUMBERED_LIST_END_LAST_ITEM_HTML = "</li></ol>";
    static final String TAG_LIST_ITEM_START_HTML = "<li>";
    static final String TAG_LIST_ITEM_END_HTML = "</li>";
    static final String TAG_URL_LINK_START_HTML = "<a href='";
    static final String TAG_URL_LINK_START_URL_END_HTML = "'>";
    static final String TAG_URL_LINK_END_HTML = "</a>";
    static final String INTENT_DATA_MAIL_TO = "mailto";
    static final String INTENT_DATA_MAIL_TO_COLON = "mailto:";

    static final String CONNECTTOSSID_JUSTANY = "^just_any^";
    //static final String CONNECTTOSSID_SHAREDPROFILE = "^default_profile^";
    static final String PROFILE_ICON_DEFAULT = "ic_profile_default";
    static final String PROFILE_ICON_RESTART_EVENTS = "ic_profile_restart_events";

    static final String MIME_TYPE_EXTRA = "mimeType";
    static final String MIME_TYPE_IMAGE = "image/*";
    static final String MINE_TYPE_ALL = "*/*";

    static final String TRUE_STRING = "true";
    static final String FALSE_STRING = "false";

    static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    static final String PHONE_PACKAGE_NAME = "com.android.phone";
    static final String SETTINGS_BATTERY_SAVER_CLASS_NAME = "com.android.settings.Settings$BatterySaverSettingsActivity";

    static final String SHORTCUT_ID = "(s)";
    static final String INTENT_ID = "(i)";

    static final String EXTRA_ACTIVATOR = "activator";
    static final String EXTRA_EDITOR = "editor";
    static final String EXTRA_SWITCH_PROFILES = "switch_profiles";

    static final String RINGTONE_CONTENT_INTERNAL = "content://media/internal";
    static final String RINGTONE_CONTENT_EXTERNAL = "content://media/external";

    static final String CONTACTS_FILTER_DATA_ALL = "[all]";

    //⏲⧴⤇⤆⍈⍇⧗⧖⭆⭅￫￩
    static final String DURATION_END = "⧗";
    static final String END_OF_ACTIVATION_TIME_END = "⧗";
    static final String END_OF_ACTIVATION_DURATION = "⧴";
    static final String END_OF_ACTIVATION_TIME = "⧗";
    static final String EVENT_DEALY_START = "⭆";
    static final String EVENT_DEALY_END = "⭅";
    static final String EVENT_START_TIME = "⧗";
    static final String EVENT_END_TIME = "⧗";

    //static final String CHAR_SQUARE = "■";
    static final String INSTALLED_STORE_INDICATOR = "•"; //"⭆";

    private StringConstants() {
        // private constructor to prevent instantiation
    }

    /*
    static String getPPPPSBySDK() {
        if (Build.VERSION.SDK_INT >= 34)
            return STR_SHIZUKU_ROOT;
        else
            return STR_PPPPS_SHIZUKU_ROOT;
    }
    */
}
