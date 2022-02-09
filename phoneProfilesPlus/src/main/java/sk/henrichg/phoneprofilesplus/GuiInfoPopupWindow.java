package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;

class GuiInfoPopupWindow extends RelativePopupWindow {

    final View popupView;
    //private final Activity activity;

    GuiInfoPopupWindow(int layoutId, int titleStringId, Activity _activity) {
        //activity = _activity;
        popupView = LayoutInflater.from(_activity).inflate(layoutId, null);
        setContentView(popupView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setAnimationStyle(R.style.popup_window_animation);

//        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();
//        applyDim(root);

        /*
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        */

        TextView titleText = popupView.findViewById(R.id.popup_window_title);
        if (titleText != null) {
            titleText.setText(titleStringId);
        }

        AppCompatImageButton closeButton = popupView.findViewById(R.id.popup_window_close);
        if (closeButton != null) {
            TooltipCompat.setTooltipText(closeButton, closeButton.getContentDescription());
            closeButton.setOnClickListener(v -> dismiss());
        }

//        setOnDismissListener(() -> {
//            ViewGroup root1 = (ViewGroup) activity.getWindow().getDecorView().getRootView();
//            clearDim(root1);
//        });
    }

//    static void applyDim(@NonNull ViewGroup parent){
//        Drawable dim = new ColorDrawable(Color.BLACK);
//        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
//        dim.setAlpha((int) (255 * 0.5));
//
//        ViewGroupOverlay overlay = parent.getOverlay();
//        overlay.add(dim);
//    }
//
//    static void clearDim(@NonNull ViewGroup parent) {
//        ViewGroupOverlay overlay = parent.getOverlay();
//        overlay.clear();
//    }

}
