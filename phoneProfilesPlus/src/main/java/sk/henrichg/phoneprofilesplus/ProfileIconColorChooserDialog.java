package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.gridlayout.widget.GridLayout;

public class ProfileIconColorChooserDialog extends DialogFragment implements View.OnClickListener {

    private ProfileIconPreference profileIconPreference;
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    private int[] mColors;
    private int defaultColor;

    public ProfileIconColorChooserDialog() {
    }

    public ProfileIconColorChooserDialog(AppCompatActivity activity, ProfileIconPreference preference)
    {
        profileIconPreference = preference;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.colorChooser_pref_dialog_title), null);
            //dialogBuilder.setTitle(R.string.colorChooser_pref_dialog_title);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
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

            //noinspection resource
            final TypedArray ta = activity.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
            int length = ta.length();
            mColors = new int[length];
            int preselect = -1;
            for (int i = 0; i < length; i++) {
                mColors[i] = ta.getColor(i, 0);
                if (profileIconPreference.useCustomColor && (mColors[i] == profileIconPreference.customColor))
                    preselect = i;
            }
            ta.recycle();

            this.defaultColor = ProfileStatic.getIconDefaultColor(profileIconPreference.imageIdentifier/*, prefContext*/);

            // default icon color --------------------------------

            final FrameLayout defaultColorLayout = layout.findViewById(R.id.dialog_color_chooser_default_color);

            //noinspection DataFlowIssue
            defaultColorLayout.setTag(-1);
            defaultColorLayout.setOnClickListener(this);

            ImageView check = (ImageView) defaultColorLayout.getChildAt(0);

            if(Color.red(defaultColor) +
                    Color.green(defaultColor) +
                    Color.blue(defaultColor) < 300)
                check.setImageResource(R.drawable.ic_check);
            else
                check.setImageResource(R.drawable.ic_check_dark);

            check.setVisibility((!profileIconPreference.useCustomColor) && (preselect == -1) ? View.VISIBLE : View.GONE);

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
            //noinspection DataFlowIssue
            defaultColorLabel.setTag(-1);
            defaultColorLabel.setOnClickListener(this);

            //--------------------------------------
            // custom icon color -------------------

            // use custom color but it is not any from preselected
            if (profileIconPreference.useCustomColor && (preselect == -1))
                preselect = -2;

            final FrameLayout customColorLayout = layout.findViewById(R.id.dialog_color_chooser_custom_color);

            //noinspection DataFlowIssue
            customColorLayout.setTag(-2);
            customColorLayout.setOnClickListener(this);

            check = (ImageView) customColorLayout.getChildAt(0);

            int customColor = profileIconPreference.customColor;
            if (customColor == 0) {
                customColor = defaultColor;
                // set it for custom color dialog changer
                profileIconPreference.customColor = customColor;
            }

            if(Color.red(customColor) +
                    Color.green(customColor) +
                    Color.blue(customColor) < 300)
                check.setImageResource(R.drawable.ic_check);
            else
                check.setImageResource(R.drawable.ic_check_dark);

            check.setVisibility(preselect == -2 ? View.VISIBLE : View.GONE);

            selector = createSelector(customColor);
            states = new int[][]{
                    new int[]{-android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_pressed}
            };
            colors = new int[]{
                    shiftColor(customColor),
                    customColor
            };
            rippleColors = new ColorStateList(states, colors);
            setBackgroundCompat(customColorLayout, new RippleDrawable(rippleColors, selector, null));

            final TextView customColorLabel = layout.findViewById(R.id.dialog_color_chooser_custom_color_label);
            //noinspection DataFlowIssue
            customColorLabel.setTag(-2);
            customColorLabel.setOnClickListener(this);

            //--------------------------------------

            final GridLayout list = layout.findViewById(R.id.dialog_color_chooser_grid);

            //noinspection DataFlowIssue
            int count = list.getChildCount();
            for (int i = 0; i < count; i++) {
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
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();
            if (index == -2) {
                // custom color
                ColorChooserCustomColorDialog dialog = new ColorChooserCustomColorDialog(
                        activity, mDialog,
                        null, profileIconPreference, null
                );
                if ((getActivity() != null) && (!getActivity().isFinishing()))
                    dialog.showDialog();
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
        /*int position = -1;
        if (color != defaultColor) {
            for (int i = 0; i < mColors.length; i++) {
                if (mColors[i] == color) {
                    position = i;
                    break;
                }
            }
        }*/

        String applicationTheme = ApplicationPreferences.applicationTheme(activity, true);

//        final String COLOR1 = "#6E6E6E";
//        final String COLOR2 = "#AEAEAE";

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        //noinspection IfStatementWithIdenticalBranches
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
//            coloredCircle.setStroke(1, Color.parseColor(COLOR1));
            coloredCircle.setStroke(1, activity.getColor(R.color.pppColorChooserColor1));
        } else {
            //if (position == 0) // white color
            //    coloredCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
//            coloredCircle.setStroke(1, Color.parseColor(COLOR1));
            coloredCircle.setStroke(1, activity.getColor(R.color.pppColorChooserColor1));
        }
        GradientDrawable darkerCircle = new GradientDrawable();
        darkerCircle.setColor(shiftColor(color));
        darkerCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
//            coloredCircle.setStroke(2, Color.parseColor(COLOR1));
            coloredCircle.setStroke(2, activity.getColor(R.color.pppColorChooserColor1));
        } else {
            //if (position == 0) // white color
            //    darkerCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
//            darkerCircle.setStroke(2, Color.parseColor(COLOR2));
            darkerCircle.setStroke(2, activity.getColor(R.color.pppColorChooserColor2));
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            show(activity.getSupportFragmentManager(), "PROFILE_ICON_COLOR_CHOOSER_DIALOG");
        }
    }

}
