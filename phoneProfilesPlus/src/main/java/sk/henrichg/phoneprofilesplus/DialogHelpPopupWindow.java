package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

class DialogHelpPopupWindow extends GuiInfoPopupWindow {

    private final AlertDialog dialog;

    private DialogHelpPopupWindow(final Activity activity, final AlertDialog _dialog, String helpString) {
        super(R.layout.dialog_help_popup_window, activity);

        dialog = _dialog;

        // Disable default animation
        setAnimationStyle(0);

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

    static void showPopup(ImageView helpIcon, Activity activity, final AlertDialog dialog, String helpString) {
        DialogHelpPopupWindow popup = new DialogHelpPopupWindow(activity, dialog, helpString);

        View contentView = popup.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = contentView.getMeasuredWidth();
        int popupHeight = contentView.getMeasuredHeight();
        PPApplication.logE("DialogHelpPopupWindow.showPopup","popupWidth="+popupWidth);
        PPApplication.logE("DialogHelpPopupWindow.showPopup","popupHeight="+popupHeight);

        ViewGroup activityView = activity.findViewById(android.R.id.content);
        int activityHeight = activityView.getHeight();
        //int activityWidth = activityView.getWidth();
        PPApplication.logE("DialogHelpPopupWindow.showPopup","activityHeight="+activityHeight);

        //int[] activityLocation = new int[2];
        //_eventStatusView.getLocationOnScreen(location);
        //activityView.getLocationInWindow(activityLocation);

        int[] locationHelpIcon = new int[2];
        helpIcon.getLocationOnScreen(locationHelpIcon); // must be used this in dialogs.
        //helpIcon.getLocationInWindow(locationHelpIcon);
        PPApplication.logE("DialogHelpPopupWindow.showPopup","locationHelpIcon[0]="+locationHelpIcon[0]);
        PPApplication.logE("DialogHelpPopupWindow.showPopup","locationHelpIcon[1]="+locationHelpIcon[1]);

        int x = 0;
        int y = 0;

        if (locationHelpIcon[0] + helpIcon.getWidth() - popupWidth < 0)
            x = -(locationHelpIcon[0] + helpIcon.getWidth() - popupWidth);

        if ((locationHelpIcon[1] + popupHeight) > activityHeight)
            y = -(locationHelpIcon[1] - (activityHeight - popupHeight));

        PPApplication.logE("DialogHelpPopupWindow.showPopup","x="+x);
        PPApplication.logE("DialogHelpPopupWindow.showPopup","y="+y);

        popup.setClippingEnabled(false); // disabled for draw outside activity
        popup.showOnAnchor(helpIcon, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
    }

    static void showPopup(ImageView helpIcon, Activity activity, final AlertDialog dialog, int helpTextResource) {
        String helpString = activity.getString(helpTextResource);
        showPopup(helpIcon, activity, dialog, helpString);
    }
}
