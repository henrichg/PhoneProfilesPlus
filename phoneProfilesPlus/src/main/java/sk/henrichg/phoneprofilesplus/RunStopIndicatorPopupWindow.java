package sk.henrichg.phoneprofilesplus;


import android.animation.Animator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class RunStopIndicatorPopupWindow extends RelativePopupWindow {

    RunStopIndicatorPopupWindow(DataWrapper dataWrapper, Activity activity) {
        View view = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.run_stop_indicator_popup_window, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        final DataWrapper _dataWrapper = dataWrapper;
        final Activity _activity = activity;

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(GlobalData.getGlobalEventsRuning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (_dataWrapper != null) {
                    _dataWrapper.runStopEvents();
                    if (_activity instanceof EditorProfilesActivity) {
                        _activity.invalidateOptionsMenu();
                        ((EditorProfilesActivity) _activity).refreshGUI(false, true);
                    } else if (_activity instanceof ActivateProfileActivity) {
                        ((ActivateProfileActivity) _activity).refreshGUI(false);
                    }
                }
            }
        });
    }

    @Override
    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos, int x, int y) {
        super.showOnAnchor(anchor, vertPos, horizPos, x, y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circularReveal(anchor);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularReveal(@NonNull final View anchor) {
        final View contentView = getContentView();
        contentView.post(new Runnable() {
            @Override
            public void run() {
                final int[] myLocation = new int[2];
                final int[] anchorLocation = new int[2];
                contentView.getLocationOnScreen(myLocation);
                anchor.getLocationOnScreen(anchorLocation);
                final int cx = anchorLocation[0] - myLocation[0] + anchor.getWidth()/2;
                final int cy = anchorLocation[1] - myLocation[1] + anchor.getHeight()/2;

                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                final int dx = Math.max(cx, contentView.getMeasuredWidth() - cx);
                final int dy = Math.max(cy, contentView.getMeasuredHeight() - cy);
                final float finalRadius = (float) Math.hypot(dx, dy);
                Animator animator = ViewAnimationUtils.createCircularReveal(contentView, cx, cy, 0f, finalRadius);
                animator.setDuration(500);
                animator.start();
            }
        });
    }

}
