package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ActivityLogHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_help);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        Toolbar toolbar = findViewById(R.id.activity_log_help_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.activity_log_help_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        TextView infoTextView = findViewById(R.id.activity_log_help_text);

        StringBuilder _value = new StringBuilder();

        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getString(R.string.activity_log_help_message_colors)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);

        int color = ContextCompat.getColor(this, R.color.altypeProfileColor);
        String colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_profile_activation)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeEventStartColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_start)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeEventEndColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_end)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeRestartEventsColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_restart_events)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeEventDelayStartEndColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_event_delay_start_end)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeErrorColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_error)).append(StringConstants.TAG_BREAK_HTML);

        color = ContextCompat.getColor(this, R.color.altypeOtherColor);
        colorString = String.format(StringConstants.STR_FORMAT_INT, color).substring(2); // !!strip alpha value!!
        _value.append(String.format(StringConstants.TAG_FONT_COLOR_HTML, colorString, StringConstants.CHAR_SQUARE_HTML));
        _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(getString(R.string.activity_log_help_message_colors_others));

        _value.append(StringConstants.TAG_DOUBLE_BREAK_HTML);
        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getString(R.string.activity_log_help_message)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_DOUBLE_BREAK_HTML);

        _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
        _value.append("\"").append(getString(R.string.altype_mergedProfileActivation)).append(": X")
                .append(StringConstants.CHAR_HARD_SPACE_HTML).append("[").append(StringConstants.CHAR_HARD_SPACE_HTML).append("Y").append(StringConstants.CHAR_HARD_SPACE_HTML).append("]\":")
                .append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_mergedProfileActivation)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

        _value.append(StringConstants.TAG_BREAK_HTML);
        _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
        _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
        _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
        _value.append("\"").append(getString(R.string.altype_profileActivation)).append("\":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_profileName)).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_displayedInGUI)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

        _value.append(StringConstants.TAG_BREAK_HTML);
        _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
        _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
        _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
        _value.append("\"").append(getString(R.string.altype_mergedProfileActivation)).append("\":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_profileNameEventName)).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_displayedInGUI)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

        _value.append(StringConstants.TAG_BREAK_HTML);
        _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
        _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
        _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
        _value.append(getString(R.string.activity_log_help_message_data_otherProfileDataTypes)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_profileName_otherDataTypes)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

        _value.append(StringConstants.TAG_BREAK_HTML);
        _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(StringConstants.TAG_BOLD_START_HTML).append(" \"").append(getString(R.string.activity_log_header_data)).append("\" ");
        _value.append(getString(R.string.activity_log_help_message_data_for)).append(" ");
        _value.append("\"").append(getString(R.string.activity_log_header_data_type)).append("\"=");
        _value.append(getString(R.string.activity_log_help_message_data_otherEventDataTypes)).append(":").append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.TAG_BREAK_HTML);
        _value.append(getString(R.string.activity_log_help_message_data_eventName_otherDataTypes)).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);

        //noinspection DataFlowIssue
        infoTextView.setText(StringFormatUtils.fromHtml(_value.toString(), true, false, 0, 0, true));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
