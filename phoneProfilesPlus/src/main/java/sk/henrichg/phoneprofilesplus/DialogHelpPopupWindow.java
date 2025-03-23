package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

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
            //noinspection DataFlowIssue
            textView.setText(StringFormatUtils.fromHtml(helpString, true,  false, 0, 0, true));
            textView.setClickable(true);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        else
            //noinspection DataFlowIssue
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

    static void showPopup(AppCompatImageButton helpIcon,
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

//            Log.e("DialogHelpPopupWindow.showPopup", "popupWidth="+popupWidth);
//            Log.e("DialogHelpPopupWindow.showPopup", "popupHeight="+popupHeight);

            /*
            ViewGroup activityView = activity.findViewById(android.R.id.content);
            //noinspection DataFlowIssue
            int activityHeight = activityView.getHeight();
            //int activityWidth = activityView.getWidth();
            */
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            //int activityWidth = metrics.widthPixels;
            int activityHeight = metrics.heightPixels;
//            Log.e("DialogHelpPopupWindow.showPopup", "activityHeight="+activityHeight);

            //int[] activityLocation = new int[2];
            //_eventStatusView.getLocationOnScreen(location);
            //activityView.getLocationInWindow(activityLocation);

            int[] locationHelpIcon = new int[2];
            helpIcon.getLocationOnScreen(locationHelpIcon); // must be used this in dialogs.
            //helpIcon.getLocationInWindow(locationHelpIcon);

//            Log.e("DialogHelpPopupWindow.showPopup", "locationHelpIcon[0]="+locationHelpIcon[0]);
//            Log.e("DialogHelpPopupWindow.showPopup", "locationHelpIcon[1]="+locationHelpIcon[1]);

            int x = 0;
            int y = 0;

            if (locationHelpIcon[0] + helpIcon.getWidth() - popupWidth < 0)
                x = -(locationHelpIcon[0] + helpIcon.getWidth() - popupWidth);

            if ((locationHelpIcon[1] + popupHeight) > activityHeight)
                y = -(locationHelpIcon[1] - (activityHeight - popupHeight));

//            Log.e("DialogHelpPopupWindow.showPopup", "x="+x);
//            Log.e("DialogHelpPopupWindow.showPopup", "y="+y);

            GlobalGUIRoutines.lockScreenOrientation(activity);

            popup.setClippingEnabled(false); // disabled for draw outside activity
            popup.showOnAnchor(helpIcon, VerticalPosition.ALIGN_TOP,
                    HorizontalPosition.ALIGN_RIGHT, x, y, false);
            GlobalGUIRoutines.dimBehindPopupWindow(popup);
        }
    }

    /** @noinspection SameParameterValue*/
    static void showPopup(AppCompatImageButton helpIcon,
                          int titleStringId,
                          Activity activity,
                          /*final Dialog dialog,*/
                          int helpTextResource,
                          @SuppressWarnings("SameParameterValue") boolean helpIsHtml) {
        String helpString = activity.getString(helpTextResource);
        showPopup(helpIcon, titleStringId, activity, /*dialog,*/ helpString, helpIsHtml);
    }
}
