package sk.henrichg.phoneprofilesplus;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class GuiInfoPopupWindow extends RelativePopupWindow {

    final View view;

    GuiInfoPopupWindow(int layoutId, Context context) {
        view = LayoutInflater.from(context).inflate(layoutId, null);
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
                if (ViewCompat.isAttachedToWindow(anchor)) {
                //if (anchor.isAttachedToWindow()) {
                    try {
                        final int[] myLocation = new int[2];
                        final int[] anchorLocation = new int[2];
                        contentView.getLocationOnScreen(myLocation);
                        anchor.getLocationOnScreen(anchorLocation);
                        final int cx = anchorLocation[0] - myLocation[0] + anchor.getWidth() / 2;
                        final int cy = anchorLocation[1] - myLocation[1] + anchor.getHeight() / 2;

                        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        final int dx = Math.max(cx, contentView.getMeasuredWidth() - cx);
                        final int dy = Math.max(cy, contentView.getMeasuredHeight() - cy);
                        final float finalRadius = (float) Math.hypot(dx, dy);
                        Animator animator = ViewAnimationUtils.createCircularReveal(contentView, cx, cy, 0f, finalRadius);
                        animator.setDuration(500);
                        animator.start();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

}
