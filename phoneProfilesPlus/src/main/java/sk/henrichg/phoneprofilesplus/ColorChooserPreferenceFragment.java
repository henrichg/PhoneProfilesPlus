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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ColorChooserPreferenceFragment extends PreferenceDialogFragmentCompat
                                                implements View.OnClickListener {

    private ColorChooserPreference preference;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        preference = (ColorChooserPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_color_chooser, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        int preselect = -1;
        for (int i = 0; i < preference.mColors.length; i++) {
            if (preference.mColors[i] == ColorChooserPreference.parseValue(preference.value)) {
                preselect = i;
                break;
            }
        }

        // custom icon color -------------------

        // use custom color but it is not any from preselected

        final FrameLayout customColorLayout = view.findViewById(R.id.dialog_color_chooser_custom_color);

        //noinspection DataFlowIssue
        customColorLayout.setTag(-2);
        customColorLayout.setOnClickListener(this);

        ImageView check = (ImageView) customColorLayout.getChildAt(0);

        int customColor = ColorChooserPreference.parseValue(preference.value);

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
        //noinspection DataFlowIssue
        customColorLabel.setTag(-2);
        customColorLabel.setOnClickListener(this);

        //--------------------------------------

        final GridLayout list = view.findViewById(R.id.dialog_color_chooser_grid);

        //noinspection DataFlowIssue
        int count = list.getChildCount();
        for (int i = 0; i < count; i++) {
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
                    ColorChooserCustomColorDialog dialog = new ColorChooserCustomColorDialog(
                            (AppCompatActivity) getActivity(), getDialog(),
                            preference, null, null
                    );
                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.showDialog();
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
