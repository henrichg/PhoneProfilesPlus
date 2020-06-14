package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class DialogHelpPopupWindowX extends GuiInfoPopupWindow {

    private final Dialog dialog;

    private DialogHelpPopupWindowX(int titleStringId, final Activity activity, final Dialog _dialog, String helpString) {
        super(R.layout.popup_window_dialog_help, titleStringId, activity);

        dialog = _dialog;

        // Disable default animation
        //setAnimationStyle(0);

        if (dialog.getWindow() != null) {
            ViewGroup root = (ViewGroup) dialog.getWindow().getDecorView().getRootView();
            applyDim(root);
        }

        TextView textView = popupView.findViewById(R.id.dialog_help_popup_window_text);
        textView.setText(helpString);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (dialog.getWindow() != null) {
                    ViewGroup root = (ViewGroup) dialog.getWindow().getDecorView().getRootView();
                    clearDim(root);
                }
                ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();
                clearDim(root);
            }
        });
    }

    static void showPopup(ImageView helpIcon, int titleStringId, Activity activity, final Dialog dialog, String helpString) {
        if (!activity.isFinishing()) {
            DialogHelpPopupWindowX popup = new DialogHelpPopupWindowX(titleStringId, activity, dialog, helpString);

            View contentView = popup.getContentView();
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupWidth = contentView.getMeasuredWidth();
            int popupHeight = contentView.getMeasuredHeight();
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "popupWidth=" + popupWidth);
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "popupHeight=" + popupHeight);
            }*/

            ViewGroup activityView = activity.findViewById(android.R.id.content);
            int activityHeight = activityView.getHeight();
            //int activityWidth = activityView.getWidth();
            //PPApplication.logE("DialogHelpPopupWindowX.showPopup","activityHeight="+activityHeight);

            //int[] activityLocation = new int[2];
            //_eventStatusView.getLocationOnScreen(location);
            //activityView.getLocationInWindow(activityLocation);

            int[] locationHelpIcon = new int[2];
            helpIcon.getLocationOnScreen(locationHelpIcon); // must be used this in dialogs.
            //helpIcon.getLocationInWindow(locationHelpIcon);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "locationHelpIcon[0]=" + locationHelpIcon[0]);
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "locationHelpIcon[1]=" + locationHelpIcon[1]);
            }*/

            int x = 0;
            int y = 0;

            if (locationHelpIcon[0] + helpIcon.getWidth() - popupWidth < 0)
                x = -(locationHelpIcon[0] + helpIcon.getWidth() - popupWidth);

            if ((locationHelpIcon[1] + popupHeight) > activityHeight)
                y = -(locationHelpIcon[1] - (activityHeight - popupHeight));

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "x=" + x);
                PPApplication.logE("DialogHelpPopupWindowX.showPopup", "y=" + y);
            }*/

            popup.setClippingEnabled(false); // disabled for draw outside activity
            popup.showOnAnchor(helpIcon, VerticalPosition.ALIGN_TOP,
                    HorizontalPosition.ALIGN_RIGHT, x, y, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static void showPopup(ImageView helpIcon, int titleStringId, Activity activity, final Dialog dialog, int helpTextResource) {
        String helpString = activity.getString(helpTextResource);
        showPopup(helpIcon, titleStringId, activity, dialog, helpString);
    }
}
