package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class DialogHelpPopupWindow extends GuiInfoPopupWindow {

    private DialogHelpPopupWindow(Context context, String helpString) {
        super(R.layout.dialog_help_popup_window, context);

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        TextView textView = popupView.findViewById(R.id.dialog_help_popup_window_text);
        textView.setText(helpString);
    }

    static void showPopup(MaterialDialog dialog, ImageView helpIcon, Context context, String helpString) {
        DialogHelpPopupWindow popup = new DialogHelpPopupWindow(context, helpString);

        View contentView = popup.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredWPopup = contentView.getMeasuredWidth();
        int measuredHPopup = contentView.getMeasuredHeight();

        int[] location = new int[2];
        helpIcon.getLocationOnScreen(location);

        int x = 0;
        int y = 0;

        if (location[0] + helpIcon.getWidth() - measuredWPopup < 0)
            x = -(location[0] + helpIcon.getWidth() - measuredWPopup);

        /*
        try {
            //noinspection ConstantConditions
            int yDialog = dialog.getWindow().getDecorView().getTop();
            int hDialog = dialog.getWindow().getDecorView().getHeight();

            if ((location[1] + helpIcon.getHeight() + measuredHPopup) > (yDialog + hDialog))
                y = -(location[1] - helpIcon.getHeight()
                        - ((yDialog + hDialog) - measuredHPopup));
        } catch (Exception ignored) {}
        */

        popup.setClippingEnabled(false);
        popup.showOnAnchor(helpIcon, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
    }

    static void showPopup(MaterialDialog dialog, ImageView helpIcon, Context context, int helpTextResource) {
        String helpString = context.getString(helpTextResource);
        showPopup(dialog, helpIcon, context, helpString);
    }
}
