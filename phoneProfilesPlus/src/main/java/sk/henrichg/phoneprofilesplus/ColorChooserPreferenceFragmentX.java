package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.view.ChromaColorView;

@SuppressWarnings("WeakerAccess")
public class ColorChooserPreferenceFragmentX extends PreferenceDialogFragmentCompat
                                                implements View.OnClickListener {

    private ColorChooserPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        preference = (ColorChooserPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_color_chooser, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        int preselect = -1;
        for (int i = 0; i < preference.mColors.length; i++) {
            if (preference.mColors[i] == Integer.parseInt(preference.value)) {
                preselect = i;
                break;
            }
        }

        // custom icon color -------------------

        // use custom color but it is not any from preselected

        final FrameLayout customColorLayout = view.findViewById(R.id.dialog_color_chooser_custom_color);

        customColorLayout.setTag(-2);
        customColorLayout.setOnClickListener(this);

        ImageView check = (ImageView) customColorLayout.getChildAt(0);

        int customColor = Integer.parseInt(preference.value);

        if(Color.red(customColor) +
                Color.green(customColor) +
                Color.blue(customColor) < 300)
            check.setImageResource(R.drawable.ic_check);
        else
            check.setImageResource(R.drawable.ic_check_dark);

        check.setVisibility(preselect == -1 ? View.VISIBLE : View.GONE);

        Drawable selector = preference.createSelector(customColor);
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_pressed},
                new int[]{android.R.attr.state_pressed}
        };
        int[] colors = new int[]{
                preference.shiftColor(customColor),
                customColor
        };
        ColorStateList rippleColors = new ColorStateList(states, colors);
        preference.setBackgroundCompat(customColorLayout, new RippleDrawable(rippleColors, selector, null));

        final TextView customColorLabel = view.findViewById(R.id.dialog_color_chooser_custom_color_label);
        customColorLabel.setTag(-2);
        customColorLabel.setOnClickListener(this);

        //--------------------------------------

        final GridLayout list = view.findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            selector = preference.createSelector(preference.mColors[i]);
            states = new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            };
            colors = new int[]{
                    preference.shiftColor(preference.mColors[i]),
                    preference.mColors[i]
            };
            rippleColors = new ColorStateList(states, colors);
            preference.setBackgroundCompat(child, new RippleDrawable(rippleColors, selector, null));
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();
            if (index == -2) {
                if (getActivity() != null) {
                    // custom color
                    //AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(preference.context);
                    dialogBuilder.setTitle(R.string.colorChooser_pref_dialog_title);
                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setNegativeButton(android.R.string.cancel, null);

                    //LayoutInflater inflater = getLayoutInflater();
                    // WARNING - use this for get transparent beckround of EditText celector handler
                    LayoutInflater inflater = LayoutInflater.from(preference.context);
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.dialog_custom_color_preference, null);
                    dialogBuilder.setView(layout);

                    final ChromaColorView chromaColorView = layout.findViewById(R.id.custom_color_chroma_color_view);
                    //PPApplication.logE("CustomColorDialogPreferenceFragmentX.onBindDialogView", "preference.value="+preference.value);
                    chromaColorView.setCurrentColor(Integer.parseInt(preference.value));
                    chromaColorView.setColorMode(ColorMode.values()[0]);
                    chromaColorView.setIndicatorMode(IndicatorMode.values()[1]);

                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                        int color = chromaColorView.getCurrentColor();
                        preference.value = String.valueOf(color);
                        preference.persistValue();
                        dismiss();
                    });
                    dialogBuilder.setNegativeButton(R.string.alert_button_no, null);

                    AlertDialog dialog = dialogBuilder.create();
                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
            }
            else {
                preference.value = String.valueOf(preference.mColors[index]);
                preference.persistValue();
                dismiss();
            }
        }
    }

}
