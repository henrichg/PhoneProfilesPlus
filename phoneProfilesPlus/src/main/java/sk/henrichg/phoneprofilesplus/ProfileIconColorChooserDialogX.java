package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.gridlayout.widget.GridLayout;

class ProfileIconColorChooserDialogX implements View.OnClickListener {

    private final ProfileIconPreferenceX profileIconPreference;
    private final AlertDialog mDialog;
    private final Activity activity;

    private final int[] mColors;
    private final int defaultColor;

    ProfileIconColorChooserDialogX(Activity activity, ProfileIconPreferenceX preference)
    {
        profileIconPreference = preference;
        this.activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.colorChooser_pref_dialog_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.profile_icon_color_chooser, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

//        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        final TypedArray ta = activity.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        mColors = new int[ta.length()];
        int preselect = -1;
        for (int i = 0; i < ta.length(); i++) {
            mColors[i] = ta.getColor(i, 0);
            if (preference.useCustomColor && (mColors[i] == preference.customColor))
                preselect = i;
        }
        ta.recycle();

        this.defaultColor = ProfileIconPreferenceAdapterX.getIconColor(preference.imageIdentifier/*, prefContext*/);

        final FrameLayout defaultColorLayout = layout.findViewById(R.id.dialog_color_chooser_default_color);

        defaultColorLayout.setTag(-1);
        defaultColorLayout.setOnClickListener(this);
        defaultColorLayout.getChildAt(0).setVisibility(preselect == -1 ? View.VISIBLE : View.GONE);

        Drawable selector = createSelector(defaultColor);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        //} else {
        //    setBackgroundCompat(defaultColorLayout, selector);
        //}



        final GridLayout list = layout.findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            selector = createSelector(mColors[i]);
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                /*int[][]*/ states = new int[][]{
                        new int[]{-android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_pressed}
                };
                /*int[]*/ colors = new int[]{
                        shiftColor(mColors[i]),
                        mColors[i]
                };
                /*ColorStateList*/ rippleColors = new ColorStateList(states, colors);
                setBackgroundCompat(child, new RippleDrawable(rippleColors, selector, null));
            //} else {
            //    setBackgroundCompat(child, selector);
            //}
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

        String applicationTheme = ApplicationPreferences.applicationTheme(activity, true);

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals("dark")) {
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
        if (applicationTheme.equals("dark")) {
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
        if (!activity.isFinishing())
            mDialog.show();
    }

}
