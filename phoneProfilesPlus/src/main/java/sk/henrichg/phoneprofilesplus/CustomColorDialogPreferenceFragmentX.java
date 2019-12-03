package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.view.ChromaColorView;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class CustomColorDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context context;
    private CustomColorDialogPreferenceX preference;

    ChromaColorView chromaColorView;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (CustomColorDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_custom_color_pref_dialog, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        chromaColorView = view.findViewById(R.id.custom_color_chroma_color_view);
        PPApplication.logE("CustomColorDialogPreferenceFragmentX.onBindDialogView", "preference.value="+preference.value);
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
