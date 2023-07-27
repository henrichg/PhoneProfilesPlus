package sk.henrichg.phoneprofilesplus;

class StringConstants {

    //TODO zmen vsade, kde sa pouizva na toto, pohladaj aj HTML verziu
    static final String CHAR_HARD_SPACE = "\u00A0";
    static final String CHAR_HARD_SPACE_HTML = "&nbsp;";
    static final String CHAR_SQUARE_HTML = "&#x25a0;";
    static final String CHAR_ARROW = "»";
    static final String STR_DOUBLE_ARROW = "»»";
    static final String STR_ARROW_INDICATOR = "[»]";
    static final String STR_DOUBLE_ARROW_INDICATOR = "[»»]";
    static final String CHAR_DOT = "•";
    static final String STR_DOT = " "+CHAR_DOT+" ";
    static final String STR_HARD_SPACE_DOUBLE_ARROW = CHAR_HARD_SPACE + STR_DOUBLE_ARROW;
    static final String STR_HARD_SPACE_DOUBLE_ARROW_HTML = CHAR_HARD_SPACE_HTML + STR_DOUBLE_ARROW;
    static final String STR_ROOT = "(R) ";
    //static final String STR_PPPPS = "(S) ";
    static final String STR_PPPPS_ROOT = "(S)(R) ";
    //static final String STR_INTERFACE = "(I) ";
    //static final String STR_G1 = "(G1) ";
    static final String STR_MANUAL = "[M]";
    static final String STR_MANUAL_SPACE = "[M] ";
    static final String STR_FORMAT_INT = "%X";
    static final String STR_SPLIT_REGEX = "\\|";
    static final String TAG_BREAK_HTML = "<br>";
    static final String TAG_DOUBLE_BREAK_HTML = "<br><br>";
    static final String TAG_BOLD_START_HTML = "<b>";
    static final String TAG_BOLD_END_HTML = "</b>";
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
    static final String TAG_LINK_START_HTML = "<a href=";
    static final String TAG_LINK_END_HTML = "</a>";
    static final String TAG_MAIL_TO_HTML = "mailto:";

}
