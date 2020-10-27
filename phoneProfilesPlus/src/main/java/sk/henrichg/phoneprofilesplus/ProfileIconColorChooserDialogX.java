package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.gridlayout.widget.GridLayout;

import com.kunzisoft.androidclearchroma.IndicatorMode;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.view.ChromaColorView;

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
        View layout = inflater.inflate(R.layout.dialog_profile_icon_color_chooser, null);
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

        // default icon color --------------------------------

        final FrameLayout defaultColorLayout = layout.findViewById(R.id.dialog_color_chooser_default_color);

        defaultColorLayout.setTag(-1);
        defaultColorLayout.setOnClickListener(this);

        ImageView check = (ImageView) defaultColorLayout.getChildAt(0);

        if(Color.red(profileIconPreference.customColor) +
            Color.green(profileIconPreference.customColor) +
            Color.blue(profileIconPreference.customColor) < 300)
            check.setImageResource(R.drawable.ic_check);
        else
            check.setImageResource(R.drawable.ic_check_dark);

        check.setVisibility((!preference.useCustomColor) && (preselect == -1) ? View.VISIBLE : View.GONE);

        Drawable selector = createSelector(defaultColor);
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

        final TextView defaultColorLabel = layout.findViewById(R.id.dialog_color_chooser_default_color_label);
        defaultColorLabel.setTag(-1);
        defaultColorLabel.setOnClickListener(this);

        //--------------------------------------
        // custom icon color -------------------

        // use custom color but it is not any from preselected
        if (preference.useCustomColor && (preselect == -1))
            preselect = -2;

        final FrameLayout customColorLayout = layout.findViewById(R.id.dialog_color_chooser_custom_color);

        customColorLayout.setTag(-2);
        customColorLayout.setOnClickListener(this);

        check = (ImageView) customColorLayout.getChildAt(0);

        if(Color.red(profileIconPreference.customColor) +
            Color.green(profileIconPreference.customColor) +
            Color.blue(profileIconPreference.customColor) < 300)
            check.setImageResource(R.drawable.ic_check);
        else
            check.setImageResource(R.drawable.ic_check_dark);

        check.setVisibility(preselect == -2 ? View.VISIBLE : View.GONE);

        selector = createSelector(profileIconPreference.customColor);
        states = new int[][]{
                new int[]{-android.R.attr.state_pressed},
                new int[]{android.R.attr.state_pressed}
        };
        colors = new int[]{
                shiftColor(profileIconPreference.customColor),
                profileIconPreference.customColor
        };
        rippleColors = new ColorStateList(states, colors);
        setBackgroundCompat(customColorLayout, new RippleDrawable(rippleColors, selector, null));

        final TextView customColorLabel = layout.findViewById(R.id.dialog_color_chooser_custom_color_label);
        customColorLabel.setTag(-2);
        customColorLabel.setOnClickListener(this);

        //--------------------------------------

        final GridLayout list = layout.findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            selector = createSelector(mColors[i]);
            states = new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            };
            colors = new int[]{
                    shiftColor(mColors[i]),
                    mColors[i]
            };
            rippleColors = new ColorStateList(states, colors);
            setBackgroundCompat(child, new RippleDrawable(rippleColors, selector, null));
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();
            if (index == -2) {
                // custom color
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(R.string.colorChooser_pref_dialog_title);
                dialogBuilder.setCancelable(true);
                dialogBuilder.setNegativeButton(android.R.string.cancel, null);

                LayoutInflater inflater = activity.getLayoutInflater();
                @SuppressLint("InflateParams")
                View layout = inflater.inflate(R.layout.dialog_custom_color_preference, null);
                dialogBuilder.setView(layout);

                final ChromaColorView chromaColorView = layout.findViewById(R.id.custom_color_chroma_color_view);
                //PPApplication.logE("CustomColorDialogPreferenceFragmentX.onBindDialogView", "preference.value="+preference.value);
                chromaColorView.setCurrentColor(profileIconPreference.customColor);
                chromaColorView.setColorMode(ColorMode.values()[0]);
                chromaColorView.setIndicatorMode(IndicatorMode.values()[1]);

                dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                    int color = chromaColorView.getCurrentColor();
                    profileIconPreference.setCustomColor(true, color);
                    mDialog.dismiss();
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);

                AlertDialog dialog = dialogBuilder.create();
                if (!activity.isFinishing())
                    dialog.show();
            }
            else {
                int color = defaultColor;
                if (index > -1)
                    color = mColors[index];
                profileIconPreference.setCustomColor(index > -1, color);
                mDialog.dismiss();
            }
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
