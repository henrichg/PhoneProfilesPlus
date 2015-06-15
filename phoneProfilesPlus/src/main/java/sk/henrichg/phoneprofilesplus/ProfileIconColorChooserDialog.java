package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;

public class ProfileIconColorChooserDialog implements View.OnClickListener {

    int selectedColor;

    private ProfileIconPreference profileIconPreference;
    private Context _context;
    private MaterialDialog mDialog;

    private int[] mColors;

    public ProfileIconColorChooserDialog(Context context, ProfileIconPreference preference, boolean useCustomColor, int selectedColor, int defaultColor)
    {
        profileIconPreference = preference;

        _context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.colorChooser_pref_dialog_title)
                        //.disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.dialog_color_chooser, false);

        dialogBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ProfileIconColorChooserDialog.this.onShow(dialog);
            }
        })
            /*.cancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            })*/
            .dismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    ProfileIconColorChooserDialog.this.onDismiss(dialog);
                }
            });

        mDialog = dialogBuilder.build();

        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        mColors = new int[ta.length()];
        int preselect = 0;
        for (int i = 0; i < ta.length(); i++) {
            if (i == 0)
                mColors[i] = defaultColor;
            else {
                mColors[i] = ta.getColor(i, 0);
                if (useCustomColor && (mColors[i] == selectedColor))
                    preselect = i;
            }
        }
        ta.recycle();
        final GridLayout list = (GridLayout) mDialog.getCustomView().findViewById(R.id.dialog_color_chooser_grid);

        for (int i = 0; i < list.getChildCount(); i++) {
            FrameLayout child = (FrameLayout) list.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
            child.getChildAt(0).setVisibility(preselect == i ? View.VISIBLE : View.GONE);

            Drawable selector = createSelector(mColors[i]);
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

    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            Integer index = (Integer) v.getTag();
            //mCallback.onColorSelection(index, mColors[index], shiftColor(mColors[index]));
            boolean custom = index > 0;
            profileIconPreference.setCustomColor(custom, mColors[index]);
            mDialog.dismiss();
        }
    }

    private void setBackgroundCompat(View view, Drawable d) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(d);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(d);
        }
    }

    private int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    private Drawable createSelector(int color) {
        ShapeDrawable coloredCircle = new ShapeDrawable(new OvalShape());
        coloredCircle.getPaint().setColor(color);
        ShapeDrawable darkerCircle = new ShapeDrawable(new OvalShape());
        darkerCircle.getPaint().setColor(shiftColor(color));

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }

    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mDialog.show();
    }

}
