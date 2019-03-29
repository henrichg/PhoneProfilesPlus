package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

import androidx.annotation.NonNull;

class GuiInfoPopupWindow extends RelativePopupWindow {

    final View popupView;
    private final Activity activity;

    GuiInfoPopupWindow(int layoutId, Activity _activity) {
        activity = _activity;
        popupView = LayoutInflater.from(activity).inflate(layoutId, null);
        setContentView(popupView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();
        applyDim(root);

        /*
        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }
        */

        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();
                clearDim(root);
            }
        });
    }

    static void applyDim(@NonNull ViewGroup parent){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * 0.5));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    /*
    @Override
    public void showOnAnchor(@NonNull View anchor, int verticalPos, int horizontalPos, int x, int y) {
        super.showOnAnchor(anchor, verticalPos, horizontalPos, x, y);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    circularReveal(anchor);
        //}
    }
    */

    /*
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
                        //contentView.getLocationOnScreen(myLocation);
                        contentView.getLocationInWindow(myLocation);
                        //anchor.getLocationOnScreen(anchorLocation);
                        anchor.getLocationInWindow(anchorLocation);
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
    */


}
