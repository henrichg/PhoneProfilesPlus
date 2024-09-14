package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class SearchStringPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private SearchStringPreference preference;

    private EditText editText;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (SearchStringPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_search_string_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(R.id.search_string_pref_dlg_editText);
        //noinspection DataFlowIssue
        editText.setBackgroundTintList(ContextCompat.getColorStateList(prefContext, R.color.highlighted_spinner_all));
        editText.setText(preference.value);

        final ImageView helpIcon = view.findViewById(R.id.search_string_pref_dlg_helpIcon);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> {
            String helpString = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                               getString(R.string.pref_dlg_info_about_wildcards_1) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.pref_dlg_info_about_wildcards_5) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                    getString(R.string.calendar_pref_dlg_info_about_wildcards) + " " +
                    getString(R.string.pref_dlg_info_about_wildcards_6) + ", " +
                    getString(R.string.pref_dlg_info_about_wildcards_3) + StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.pref_dlg_info_about_wildcards_4) +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            DialogHelpPopupWindow.showPopup(helpIcon, R.string.menu_help, (Activity) prefContext, /*getDialog(),*/ helpString, true);
        });

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue(editText.getText().toString());
        }

        preference.fragment = null;
    }

}
