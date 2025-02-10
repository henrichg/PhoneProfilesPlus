package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class PPEditTextDialogPreferenceFragment extends PreferenceDialogFragmentCompat
{

    private PPEditTextDialogPreference preference;

    EditText editText;
    TextView textView;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        preference = (PPEditTextDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_edittext_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(R.id.dialog_edittext_preference_edit);
        //noinspection DataFlowIssue
        editText.setText(preference.editTextValue);

        textView = view.findViewById(R.id.dialog_edittext_preference_label);
        //noinspection DataFlowIssue
        textView.setVisibility(View.GONE);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.editTextValue = editText.getText().toString();
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }
        preference.fragment = null;
    }

}
