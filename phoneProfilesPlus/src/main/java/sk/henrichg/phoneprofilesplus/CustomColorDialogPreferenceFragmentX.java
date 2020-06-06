package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.view.ChromaColorView;

@SuppressWarnings("WeakerAccess")
public class CustomColorDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private CustomColorDialogPreferenceX preference;

    ChromaColorView chromaColorView;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        preference = (CustomColorDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_custom_color_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        chromaColorView = view.findViewById(R.id.custom_color_chroma_color_view);
        //PPApplication.logE("CustomColorDialogPreferenceFragmentX.onBindDialogView", "preference.value="+preference.value);
        chromaColorView.setCurrentColor(preference.value);
        chromaColorView.setColorMode(ColorMode.values()[preference.chromaColorMode]);
        chromaColorView.setIndicatorMode(IndicatorMode.values()[preference.chromaIndicatorMode]);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            preference.value = chromaColorView.getCurrentColor();
            preference.persistValue();
        }
        else {
            //preference.value = chromaColorView.getCurrentColor();
            preference.resetSummary();
        }

        preference.fragment = null;
    }

}
