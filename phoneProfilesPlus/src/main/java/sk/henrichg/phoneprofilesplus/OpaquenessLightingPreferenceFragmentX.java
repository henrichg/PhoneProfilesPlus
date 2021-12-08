package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class OpaquenessLightingPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    OpaquenessLightingPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (OpaquenessLightingPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_opaqueness_lighting_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.opaqueness_lighting_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> doOnItemSelected(position));

        OpaquenessLightingPreferenceAdapterX opaquenessLightingPreferenceAdapter = new OpaquenessLightingPreferenceAdapterX(preference.fragment, prefContext, preference.value);
        listView.setAdapter(opaquenessLightingPreferenceAdapter);
        int position = preference.getPosition(preference.value);
        if (position != -1)
            listView.setSelection(position);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    void doOnItemSelected(int position)
    {
        if (preference.showLighting)
            preference.setValue(String.valueOf(preference.lightingValues[position]));
        else
            preference.setValue(String.valueOf(preference.opaquenessValues[position]));
        dismiss();
    }

}
