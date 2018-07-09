package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;

class ProfileIconColorChooserDialog implements View.OnClickListener {

    private final ProfileIconPreference profileIconPreference;
    final MaterialDialog mDialog;
    private final Context context;

    private final int[] mColors;
    private final int defaultColor;

    ProfileIconColorChooserDialog(Context context, ProfileIconPreference preference, boolean useCustomColor, int selectedColor, int defaultColor)
    {
        profileIconPreference = preference;
        this.context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.colorChooser_pref_dialog_title)
                //.disableDefaultFonts()
                .customView(R.layout.profile_icon_color_chooser, false)
                .negativeText(android.R.string.cancel);

        mDialog = dialogBuilder.build();

        /*
        MDButton negative = mDialog.getActionButton(DialogAction.NEGATIVE);
        if (negative != null) negative.setAllCaps(false);
        MDButton  neutral = mDialog.getActionButton(DialogAction.NEUTRAL);
        if (neutral != null) neutral.setAllCaps(false);
        MDButton  positive = mDialog.getActionButton(DialogAction.POSITIVE);
        if (positive != null) positive.setAllCaps(false);
        */

        View layout = mDialog.getCustomView();

        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        mColors = new int[ta.length()];
        int preselect = -1;
        for (int i = 0; i < ta.length(); i++) {
            mColors[i] = ta.getColor(i, 0);
            if (useCustomColor && (mColors[i] == selectedColor))
                preselect = i;
        }
        ta.recycle();

        this.defaultColor = defaultColor;

        //noinspection ConstantConditions
        final FrameLayout defaultColorLayout = layout.findViewById(R.id.dialog_color_chooser_default_color);

        defaultColorLayout.setTag(-1);
        defaultColorLayout.setOnClickListener(this);
        defaultColorLayout.getChildAt(0).setVisibility(preselect == -1 ? View.VISIBLE : View.GONE);

        Drawable selector = createSelector(defaultColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            };
            int[] colors = new int[]{
                    shiftColor(defaultColor),
                    defaultColor
            };
            ColorStateList rippleColors = new ColorStateList(states, colors);
            setBackgroundCompat(defaultColorLayout, new RippleDrawable(rippleColors, selector, null));
        } else {
            setBackgroundCompat(defaultColorLayout, selector);
        }



        final GridLayout list = layout.findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            selector = createSelector(mColors[i]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[][]{
                        new int[]{-android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_pressed}
                };
                int[] colors = new int[]{
                        shiftColor(mColors[i]),
                        mColors[i]
                };
                ColorStateList rippleColors = new ColorStateList(states, colors);
                setBackgroundCompat(child, new RippleDrawable(rippleColors, selector, null));
            } else {
                setBackgroundCompat(child, selector);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();
            int color = defaultColor;
            if (index > -1)
                color = mColors[index];
            profileIconPreference.setCustomColor(index > -1, color);
            mDialog.dismiss();
        }
    }

    private void setBackgroundCompat(View view, Drawable d) {
        view.setBackground(d);
    }

    private int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    private Drawable createSelector(int color) {
        int position = -1;
        if (color != defaultColor) {
            for (int i = 0; i < mColors.length; i++) {
                if (mColors[i] == color) {
                    position = i;
                    break;
                }
            }
        }

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        if (ApplicationPreferences.applicationTheme(context).equals("dark")) {
            if (position == 2) // dark gray color
                coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
        }
        else {
            if (position == 0) // white color
                coloredCircle.setStroke(2, Color.parseColor("#AEAEAE"));
        }
        GradientDrawable darkerCircle = new GradientDrawable();
        darkerCircle.setColor(shiftColor(color));
        darkerCircle.setShape(GradientDrawable.OVAL);
        if (ApplicationPreferences.applicationTheme(context).equals("dark")) {
            if (position == 2) // dark gray color
                coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
        }
        else {
            if (position == 0) // white color
                darkerCircle.setStroke(2, Color.parseColor("#AEAEAE"));
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }

    public void show() {
        mDialog.show();
    }

}
