package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ColorChooserPreferenceFragmentX extends PreferenceDialogFragmentCompat
                                                implements View.OnClickListener {

    ColorChooserPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        preference = (ColorChooserPreferenceX) getPreference();

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_color_chooser, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        int preselect = 0;
        for (int i = 0; i < preference.mColors.length; i++) {
            if (preference.mColors[i] == Integer.valueOf(preference.value)) {
                preselect = i;
                break;
            }
        }

        //noinspection ConstantConditions
        final GridLayout list = view.findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            Drawable selector = preference.createSelector(preference.mColors[i]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[][]{
                        new int[]{-android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_pressed}
                };
                int[] colors = new int[]{
                        preference.shiftColor(preference.mColors[i]),
                        preference.mColors[i]
                };
                ColorStateList rippleColors = new ColorStateList(states, colors);
                preference.setBackgroundCompat(child, new RippleDrawable(rippleColors, selector, null));
            } else {
                preference.setBackgroundCompat(child, selector);
            }
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();

            preference.value = String.valueOf(preference.mColors[index]);
            preference.persistValue();

            dismiss();
        }
    }

}
