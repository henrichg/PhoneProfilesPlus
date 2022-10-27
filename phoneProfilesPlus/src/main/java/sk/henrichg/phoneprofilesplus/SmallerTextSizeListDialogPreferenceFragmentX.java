package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class SmallerTextSizeListDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private SmallerTextSizeListDialogPreferenceX preference;

    //private SmallerTextSizeListPreferenceAdapterX listAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (SmallerTextSizeListDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_smaller_text_size_list_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.smaller_text_size_list_pref_dlg_listview);

        SmallerTextSizeListPreferenceAdapterX listAdapter = new SmallerTextSizeListPreferenceAdapterX(prefContext, preference);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            preference.value = preference.entryValues[position].toString();
            //listAdapter.notifyDataSetChanged();
            preference.persistValue();
            dismiss();
        });

        listView.setAdapter(listAdapter);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

}
