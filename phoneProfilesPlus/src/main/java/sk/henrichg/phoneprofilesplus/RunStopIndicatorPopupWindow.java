package sk.henrichg.phoneprofilesplus;


import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    RunStopIndicatorPopupWindow(int titleStringId, final DataWrapper dataWrapper, final Activity activity) {
        super(R.layout.run_stop_indicator_popup_window, titleStringId, activity);

        // Disable default animation
        setAnimationStyle(0);

        final TextView textView = popupView.findViewById(R.id.run_stop_indicator_popup_window_important_info);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLaunch = new Intent(activity, ImportantInfoActivity.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, 0);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SCROLL_TO, R.id.activity_info_notification_event_not_started);
                activity.startActivity(intentLaunch);

                dismiss();
            }
        });

        final SwitchCompat checkBox = popupView.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(Event.getGlobalEventsRunning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (dataWrapper != null)
                    dataWrapper.runStopEventsWithAlert(activity, checkBox, isChecked);
            }
        });
    }

}
