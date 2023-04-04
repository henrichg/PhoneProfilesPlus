package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class DialogHelpPopupWindow extends GuiInfoPopupWindow {

    //private final Dialog dialog;

    private DialogHelpPopupWindow(int titleStringId,
                                  final Activity activity,
                                    /*final Dialog _dialog,*/
                                   String helpString,
                                  boolean helpIsHtml) {
        super(R.layout.popup_window_dialog_help, titleStringId, activity);

        //dialog = _dialog;

        // Disable default animation
        //setAnimationStyle(0);

//        if (dialog.getWindow() != null) {
//            ViewGroup root = (ViewGroup) dialog.getWindow().getDecorView().getRootView();
//            applyDim(root);
//        }

        TextView textView = popupView.findViewById(R.id.dialog_help_popup_window_text);
        if (helpIsHtml) {
            textView.setText(StringFormatUtils.fromHtml(helpString, true, false, false, 0, 0, true));
            textView.setClickable(true);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else
            textView.setText(helpString);

//        setOnDismissListener(() -> {
//            if (dialog.getWindow() != null) {
//                ViewGroup root = (ViewGroup) dialog.getWindow().getDecorView().getRootView();
//                clearDim(root);
//            }
//            ViewGroup root = (ViewGroup) activity.getWindow().getDecorView().getRootView();
//            clearDim(root);
//        });
    }

    static void showPopup(ImageView helpIcon,
                          int titleStringId,
                          Activity activity,
                          /*final Dialog dialog,*/
                          String helpString,
                          boolean helpIsHtml) {
        if (!activity.isFinishing()) {
            DialogHelpPopupWindow popup = new DialogHelpPopupWindow(
                    titleStringId,
                    activity,
                    /*dialog,*/
                    helpString,
                    helpIsHtml);

            View contentView = popup.getContentView();
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupWidth = contentView.getMeasuredWidth();
            int popupHeight = contentView.getMeasuredHeight();

            ViewGroup activityView = activity.findViewById(android.R.id.content);
            int activityHeight = activityView.getHeight();
            //int activityWidth = activityView.getWidth();

            //int[] activityLocation = new int[2];
            //_eventStatusView.getLocationOnScreen(location);
            //activityView.getLocationInWindow(activityLocation);

            int[] locationHelpIcon = new int[2];
            helpIcon.getLocationOnScreen(locationHelpIcon); // must be used this in dialogs.
            //helpIcon.getLocationInWindow(locationHelpIcon);

            int x = 0;
            int y = 0;

            if (locationHelpIcon[0] + helpIcon.getWidth() - popupWidth < 0)
                x = -(locationHelpIcon[0] + helpIcon.getWidth() - popupWidth);

            if ((locationHelpIcon[1] + popupHeight) > activityHeight)
                y = -(locationHelpIcon[1] - (activityHeight - popupHeight));

            popup.setClippingEnabled(false); // disabled for draw outside activity
            popup.showOnAnchor(helpIcon, VerticalPosition.ALIGN_TOP,
                    HorizontalPosition.ALIGN_RIGHT, x, y, false);
        }
    }

    static void showPopup(ImageView helpIcon,
                          int titleStringId,
                          Activity activity,
                          /*final Dialog dialog,*/
                          int helpTextResource,
                          @SuppressWarnings("SameParameterValue") boolean helpIsHtml) {
        String helpString = activity.getString(helpTextResource);
        showPopup(helpIcon, titleStringId, activity, /*dialog,*/ helpString, helpIsHtml);
    }
}
