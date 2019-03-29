package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

class CalendarSearchStringPreference extends DialogPreference {

    private final Context context;

    private AlertDialog mDialog;
    private EditText editText;

    private String value = "";

    public CalendarSearchStringPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void showDialog(Bundle state) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    value = editText.getText().toString();
                    persistString(value);
                    setSummaryCSSP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_calendar_search_string_pref_dialog, null);
        dialogBuilder.setView(layout);

        editText = layout.findViewById(R.id.calendar_search_string_pref_dlg_editText);

        final ImageView helpIcon = layout.findViewById(R.id.calendar_search_string_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpString =
                        "\u2022 " + context.getString(R.string.pref_dlg_info_about_wildcards_1) + "\n" +
                        "\u2022 " + context.getString(R.string.pref_dlg_info_about_wildcards_5) + "\n" +
                        "\u2022 " + context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                                    context.getString(R.string.calendar_pref_dlg_info_about_wildcards) + " " +
                                    context.getString(R.string.pref_dlg_info_about_wildcards_6) + ", " +
                                    context.getString(R.string.pref_dlg_info_about_wildcards_3) + "\n" +
                        "\u2022 " + context.getString(R.string.pref_dlg_info_about_wildcards_4)
                        ;
                DialogHelpPopupWindow.showPopup(helpIcon, (Activity)context, mDialog, helpString);
            }
        });

        mDialog = dialogBuilder.create();

        /*
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                CalendarSearchStringPreference.this.onShow();
            }
        });
        */

        getValueCSSP();

        editText.setText(value);

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);


        mDialog.setOnDismissListener(this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueCSSP();
        }
        else {
            // set state
            value = "";
            persistString(value);
        }
        setSummaryCSSP();
    }

    private void getValueCSSP()
    {
        value = getPersistedString(value);
        //editText.setText(value);
    }

    private void setSummaryCSSP() {
        setSummary(value);
    }

}
