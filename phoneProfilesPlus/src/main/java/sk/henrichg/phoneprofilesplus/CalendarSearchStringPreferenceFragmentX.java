package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class CalendarSearchStringPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    CalendarSearchStringPreferenceX preference;

    private EditText editText;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (CalendarSearchStringPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_calendar_search_string_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(R.id.calendar_search_string_pref_dlg_editText);
        editText.setText(preference.value);

        final ImageView helpIcon = view.findViewById(R.id.calendar_search_string_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString =
                        "\u2022 " + getString(R.string.pref_dlg_info_about_wildcards_1) + "\n" +
                                "\u2022 " + getString(R.string.pref_dlg_info_about_wildcards_5) + "\n" +
                                "\u2022 " + getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                                getString(R.string.calendar_pref_dlg_info_about_wildcards) + " " +
                                getString(R.string.pref_dlg_info_about_wildcards_6) + ", " +
                                getString(R.string.pref_dlg_info_about_wildcards_3) + "\n" +
                                "\u2022 " + getString(R.string.pref_dlg_info_about_wildcards_4)
                        ;
                DialogHelpPopupWindowX.showPopup(helpIcon, (Activity)prefContext, getDialog(), helpString);
            }
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
